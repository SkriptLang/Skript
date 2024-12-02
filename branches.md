# Branches
Skript utilizes Git branches to keep development more manageable.
See [here](https://github.com/SkriptLang/Skript/blob/master/CLOCKWORK_RELEASE_MODEL.md) for detailed information about our branch structure.

## `master`
The master branch always reflects the state of the most modern released version.

During a release, either the `dev/feature` or `dev/patch` branches are merged into master. The release is then signed from the master branch.
Commits are never made directly to the master branch. Only the organisation admins have write access to this branch.

## `dev/feature`
The feature branch is for contributions that add enhancements, new features, or make breaking changes.
Pull requests adding any of these should target this branch (or use it as its base).
Pull requests targeting this branch require maintainer approval before being merged.
Core maintainers have unsupervised write access to this branch.

## `dev/patch`
The patch branch is for contributions that fix bugs or add tests & documentation that would not impact the user experience.
Pull requests adding any of these should target this branch (or use it as its base).
Pull requests targeting this branch require maintainer approval before being merged.
Core maintainers have unsupervised write access to this branch.

The patch branch is "upstream" of `dev/feature`, i.e. patch gets merged into feature semi-regularly.

## Specific feature branches
Core developers are able to create `feature/*` branches, to work on large-scale or collaborative code changes.
All team members have write access to feature branches.
There are no approval requirements for pull requests targeting feature branches, 
however, feature branches are merged into `dev/feature` via pull requests, which require regular approval.

## Version branches
Upon release, versions have a 'tag' created in the git history.
If a future change is required (i.e. back-porting a critical security fix to the previous version) then a branch may be created for this version.

## Legacy branches
Older branches may remain from before the new branch structure was adopted in 2023, for example `dev/2.6`.
These reflect historic states of the project and are not used. There is no guarantee that these branches will be kept forever.
