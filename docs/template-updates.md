# Updates for projects created from FTC-Template

New repositories created with **Use this template** inherit the **FTC Template
updates** workflow. It watches `CatsRdeBest2018/FTC-Template` on `main` daily at
12:45 UTC (7:45 a.m. Eastern in winter, 8:45 a.m. in summer). It also runs when
first installed and can be started under Actions → FTC Template updates → Run
workflow. It does not sync the template into itself.

The update chain is Pedro Quickstart → reviewed PR in FTC-Template → reviewed PR
in each project. The Pedro workflow remains disabled in generated projects.
Only changes committed and pushed to the template's `main` branch are distributed;
uncommitted work on your computer is not included.

## One-time GitHub setting for each new project

Under **Settings → Actions → General → Workflow permissions**, enable **Allow
GitHub Actions to create and approve pull requests**, then Save. The workflow
only creates draft PRs; it never approves or merges them. No personal token is
needed. Repository permissions are not copied from the template, so this setting
may need enabling in each new repository. An organization policy may control it.
Make sure Actions itself is enabled too.

See [GitHub's repository Actions settings](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/enabling-features-for-your-repository/managing-github-actions-settings-for-a-repository).

## Accepting updates

Review the draft PR, build, and test on the robot before merging. A clean Git
merge can still introduce API or behavior changes. Keep the included
`.github/template-sync-base` file: it records the last accepted template version.
Squash and ordinary merge commits both work. Commit your local custom work before
pulling the updated project.

An open update PR is never rewritten by the bot. Merge or close it to allow the
next proposal. Closing without merging allows the same update to be proposed
again. If PR creation was blocked by permissions, enable the setting above and
rerun; an unused proposal branch from the failed run may remain.

## How custom changes are preserved

GitHub template copies start with a single commit and unrelated history. On the
first update, the workflow matches that initial commit's complete file tree to
an exact historical FTC-Template snapshot. It never guesses based on creation
date or assumes the latest template is the starting version. If there is no exact
match, it stops for manual setup. Preserve the project's original Git history.

Subsequent runs use the checkpoint from the last merged update. Git applies only
the changes between that source version and the latest template using
`git apply --3way --index`. Project-only files and independent edits remain intact.
Overlapping edits, modified-file deletions, and conflicting new files stop the
run before anything is pushed. The log and run summary identify the problem;
resolve it manually. There is no automatic replacement with the template's copy,
force push, or automatic merge into your default branch.

Workflow files under `.github/workflows/` are deliberately excluded, so the bot
does not replace project automation or require a workflow-scoped personal token.
Those updates need manual integration. New workflow improvements in the template
will therefore not automatically upgrade older projects' updater workflows.

Existing projects do not acquire these files retroactively. They can opt in by
copying `.github/workflows/template-updates.yml` and
`tests/test_template_updates.py` onto their default branch and enabling the setting
above. The exact initial snapshot must exist in the template's retained history;
otherwise configure the baseline manually only after verifying the source version.

GitHub may delay scheduled runs or disable schedules in inactive public
repositories. Check Actions if updates stop arriving. See
[GitHub's schedule documentation](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#schedule).
