# Automatic Pedro Quickstart updates

For projects made from this template, see [Template updates](template-updates.md).
They receive updates from FTC-Template through their own draft PRs.

GitHub Actions checks Pedro-Pathing/Quickstart's `master` branch daily at 11:30 UTC
(6:30 a.m. Eastern in winter, 7:30 a.m. in summer). Run it manually from
Actions → Pedro Quickstart updates → Run workflow.

The workflow uses ordinary Git merge and GitHub pull requests. There is no custom
sync program or dependency-update service.

- Each proposal starts from the current default branch and merges upstream using
  shared Git history. Existing custom commits are retained. There is no reset,
  force push, or automatic choice of an upstream version during a conflict.
- If Git reports a conflict, the run fails with the conflicting paths in its
  Actions summary. Nothing is pushed. Resolve those changes manually.
- A successful merge becomes a draft PR. It never updates `main` automatically.
  Review the diff, build, and test on the robot before accepting: a clean Git merge
  can still introduce incompatible APIs or behavior, including changes in a file
  you also customized.
- Accept with **Create a merge commit**, rather than squash or rebase, so the
  upstream ancestry remains available to future update checks.
- An open update PR is left untouched, including any edits you make. Merge or
  close it to allow the next proposal. Closing without merging permits another
  proposal on the next run.
- Local uncommitted work is never uploaded or changed by this workflow. Commit
  your custom work before pulling merged updates into your local checkout.

The workflow is `.github/workflows/pedro-updates.yml`. It uses the built-in
`GITHUB_TOKEN` with contents and pull-request write permissions. Repository Actions
settings must allow Actions to create pull requests; no personal token is needed.

GitHub may delay scheduled runs, and may disable schedules in public repositories
after 60 days without repository activity. Re-enable the workflow in Actions if
that happens. See [GitHub's schedule documentation](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#schedule).
