# Branches
Skript utilizes Git branches to keep development more manageable.

## Feature branches
Anyone with push access can create these. Core developers may use them to work
on code changes that other people would fork Skript for. Pull requests are used
to merge code from feature branches to anywhere else.

## Master
The master branch contains the latest release plus all commits merged after it: bug fixes, enhancements and features, all of it.
Pretty much all PRs should target the master branch.

## Development branches
All recent major releases except for the latest major will have a development branch, for example `dev/2.6`.
These branches will be updated only with bug fixes, here's how to update it:
- Create a new branch in your fork from the development branch and check it out locally
- [Cherry-pick](https://git-scm.com/docs/git-cherry-pick) the commits from master to be included (bug fixes)
  - In case there are conflicts, you can manually apply the changes, but make sure to include both the full commit hash and the PR number (#1234) in the commit message 
- Create a PR for your forked branch targeting the development branch
- Wait for approval
- Merge the PR, **not squash merge!**
