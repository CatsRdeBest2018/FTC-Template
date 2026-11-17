"""Exercise the actual workflow shell in isolated Git repositories, without GitHub writes."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import textwrap
import unittest


WORKFLOW = Path(__file__).resolve().parents[1] / '.github/workflows/template-updates.yml'


class TemplateUpdateTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.source = self.root / 'template'
        self.project = self.root / 'project'
        self.remote = self.root / 'origin.git'
        self.source.mkdir()
        self.git(self.source, 'init', '-q', '-b', 'main')
        self.identity(self.source)
        self.write(self.source, 'framework.txt', '\n'.join(f'line {i}' for i in range(20)) + '\n')
        self.write(self.source, 'settings.txt', 'template hardware\n')
        self.write(self.source, '.github/workflows/project.yml', 'original workflow\n')
        self.initial = self.commit(self.source, 'initial template')
        shutil.copytree(self.source, self.project, ignore=shutil.ignore_patterns('.git'))
        self.git(self.project, 'init', '-q', '-b', 'main')
        self.identity(self.project)
        self.commit(self.project, 'fresh GitHub template copy')
        self.git(self.root, 'init', '--bare', '-q', str(self.remote))
        self.git(self.project, 'remote', 'add', 'origin', str(self.remote))
        self.git(self.project, 'push', '-q', 'origin', 'main')
        self.git(self.project, 'config', f'url.{self.source}.insteadOf',
                 'https://github.com/CatsRdeBest2018/FTC-Template.git')
        self.bin = self.root / 'bin'
        self.bin.mkdir()
        gh = self.bin / 'gh'
        gh.write_text(textwrap.dedent('''\
            #!/usr/bin/env python3
            import json, os, pathlib, sys
            if sys.argv[1:3] == ['pr', 'list']:
                print(os.environ.get('TEST_OPEN_PRS', '0'))
            elif sys.argv[1:3] == ['pr', 'create']:
                if os.environ.get('TEST_PR_DENIED') == '1':
                    sys.exit(1)
                body = pathlib.Path(sys.argv[sys.argv.index('--body-file') + 1]).read_text()
                pathlib.Path(os.environ['TEST_CREATED_PR']).write_text(json.dumps({'args': sys.argv, 'body': body}))
                print('https://example.invalid/fixture/pull/1')
            else:
                sys.exit('Unexpected GitHub command: ' + str(sys.argv))
        '''))
        gh.chmod(0o755)
        # Extract the literal shell block; no production sync program is needed.
        marker = '        run: |\n'
        text = WORKFLOW.read_text()
        self.assertEqual(text.count(marker), 1)
        self.script = self.root / 'workflow.sh'
        self.script.write_text(textwrap.dedent(text.split(marker, 1)[1]))
        self.env = dict(os.environ, PATH=str(self.bin) + os.pathsep + os.environ['PATH'],
                        GH_REPO='fixture/project', BASE_BRANCH='main', GITHUB_RUN_ID='123',
                        GITHUB_RUN_ATTEMPT='1', GITHUB_STEP_SUMMARY=str(self.root / 'summary'),
                        RUNNER_TEMP=str(self.root), TEST_CREATED_PR=str(self.root / 'pr.json'))

    def git(self, repo, *args):
        return subprocess.check_output(['git', *args], cwd=repo,
                                       stderr=subprocess.STDOUT, text=True).strip()

    def identity(self, repo):
        self.git(repo, 'config', 'user.name', 'Test')
        self.git(repo, 'config', 'user.email', 'test@example.invalid')

    def write(self, repo, path, text):
        destination = repo / path
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(text)

    def commit(self, repo, message):
        self.git(repo, 'add', '--all')
        self.git(repo, 'commit', '-qm', message)
        return self.git(repo, 'rev-parse', 'HEAD')

    def run_sync(self, expected=0):
        before = self.git(self.remote, 'rev-parse', 'refs/heads/main')
        result = subprocess.run(['bash', str(self.script)], cwd=self.project,
                                env=self.env, capture_output=True, text=True)
        self.assertEqual(result.returncode, expected, result.stdout + result.stderr)
        self.assertEqual(self.git(self.remote, 'rev-parse', 'refs/heads/main'), before)
        return result

    def assert_no_proposal(self):
        self.assertFalse((self.root / 'pr.json').exists())
        self.assertEqual(self.git(self.remote, 'for-each-ref', '--format=%(refname)',
                                  'refs/heads/codex/'), '')

    def test_new_copy_is_noop(self):
        self.run_sync()
        self.assert_no_proposal()
        self.assertEqual(self.git(self.project, 'status', '--porcelain'), '')

    def test_updates_merge_independent_custom_edits_and_keep_project_files(self):
        original = (self.project / 'framework.txt').read_text()
        self.write(self.project, 'framework.txt', original.replace('line 1\n', 'custom line 1\n'))
        self.write(self.project, 'settings.txt', 'my robot hardware\n')
        self.write(self.project, 'MyAuto.java', 'custom autonomous\n')
        self.commit(self.project, 'custom robot')
        self.write(self.source, 'framework.txt', original.replace('line 18\n', 'new framework 18\n'))
        self.write(self.source, 'new-library.txt', 'new template file\n')
        target = self.commit(self.source, 'template update')
        self.run_sync()
        self.assertEqual((self.project / 'framework.txt').read_text(),
                         original.replace('line 1\n', 'custom line 1\n').replace('line 18\n', 'new framework 18\n'))
        self.assertEqual((self.project / 'settings.txt').read_text(), 'my robot hardware\n')
        self.assertEqual((self.project / 'MyAuto.java').read_text(), 'custom autonomous\n')
        self.assertEqual((self.project / '.github/template-sync-base').read_text().strip(), target)
        pr = json.loads((self.root / 'pr.json').read_text())
        self.assertIn('--draft', pr['args'])
        self.assertEqual(self.git(self.project, 'show', '-s', '--format=%P', 'HEAD').count(' '), 0)

    def test_overlapping_edit_stops_without_pushing(self):
        self.write(self.project, 'settings.txt', 'custom settings\n')
        self.commit(self.project, 'custom edit')
        self.write(self.source, 'settings.txt', 'new template settings\n')
        self.commit(self.source, 'conflicting update')
        self.run_sync(expected=1)
        self.assert_no_proposal()
        self.assertIn('settings.txt', (self.root / 'summary').read_text())

    def test_modified_file_is_not_deleted_by_upstream(self):
        self.write(self.project, 'settings.txt', 'custom settings\n')
        self.commit(self.project, 'custom edit')
        (self.source / 'settings.txt').unlink()
        self.commit(self.source, 'delete template file')
        self.run_sync(expected=1)
        self.assert_no_proposal()

    def test_project_deletion_is_not_silently_undone(self):
        (self.project / 'settings.txt').unlink()
        self.commit(self.project, 'remove unused hardware')
        self.write(self.source, 'settings.txt', 'updated template hardware\n')
        self.commit(self.source, 'update hardware')
        self.run_sync(expected=1)
        self.assert_no_proposal()

    def test_new_file_collision_stops(self):
        self.write(self.project, 'new.txt', 'my new file\n')
        self.commit(self.project, 'project file')
        self.write(self.source, 'new.txt', 'template new file\n')
        self.commit(self.source, 'template file')
        self.run_sync(expected=1)
        self.assert_no_proposal()

    def test_workflow_changes_are_excluded(self):
        self.write(self.project, '.github/workflows/project.yml', 'custom workflow\n')
        self.commit(self.project, 'custom CI')
        self.write(self.source, '.github/workflows/project.yml', 'new template CI\n')
        self.write(self.source, '.github/workflows/new.yml', 'new workflow\n')
        self.write(self.source, 'settings.txt', 'updated template settings\n')
        self.commit(self.source, 'template CI and code')
        self.run_sync()
        self.assertEqual((self.project / '.github/workflows/project.yml').read_text(), 'custom workflow\n')
        self.assertFalse((self.project / '.github/workflows/new.yml').exists())

    def test_unknown_initial_snapshot_stops(self):
        self.write(self.project, 'only-local.txt', 'custom initial file\n')
        self.git(self.project, 'add', '.')
        self.git(self.project, 'commit', '--amend', '--no-edit', '-q')
        self.run_sync(expected=1)
        self.assert_no_proposal()

    def test_invalid_checkpoint_stops(self):
        self.write(self.project, '.github/template-sync-base', 'not-a-commit\n')
        self.commit(self.project, 'invalid state')
        self.run_sync(expected=1)
        self.assert_no_proposal()

    def test_open_pr_is_left_alone(self):
        self.env['TEST_OPEN_PRS'] = '1'
        self.run_sync()
        self.assert_no_proposal()

    def test_dirty_checkout_stops(self):
        self.write(self.project, 'uncommitted.txt', 'local work')
        self.run_sync(expected=1)
        self.assert_no_proposal()
        self.assertEqual((self.project / 'uncommitted.txt').read_text(), 'local work')

    def test_squash_merge_checkpoint_supports_next_update(self):
        self.write(self.source, 'new.txt', 'first update\n')
        first = self.commit(self.source, 'first update')
        self.run_sync()
        proposal = self.git(self.project, 'rev-parse', 'HEAD')
        self.git(self.project, 'switch', '-q', 'main')
        self.git(self.project, 'merge', '--squash', proposal)
        self.commit(self.project, 'accept update by squash')
        self.write(self.project, 'MyAuto.java', 'keep me\n')
        self.commit(self.project, 'custom code after update')
        self.write(self.source, 'new.txt', 'second update\n')
        second = self.commit(self.source, 'second update')
        self.env['GITHUB_RUN_ID'] = '124'
        self.run_sync()
        self.assertEqual((self.project / 'new.txt').read_text(), 'second update\n')
        self.assertEqual((self.project / 'MyAuto.java').read_text(), 'keep me\n')
        self.assertEqual((self.project / '.github/template-sync-base').read_text().strip(), second)
        self.assertIn(first, json.loads((self.root / 'pr.json').read_text())['body'])

    def test_pr_permission_error_explains_setting_without_changing_main(self):
        self.write(self.source, 'new.txt', 'template update\n')
        self.commit(self.source, 'update')
        self.env['TEST_PR_DENIED'] = '1'
        result = self.run_sync(expected=1)
        self.assertIn('Allow GitHub Actions', result.stdout)
        self.assertFalse((self.root / 'pr.json').exists())


if __name__ == '__main__':
    unittest.main()
