# Clockwork Release Model

## Table of Contents
1. [Introduction](#introduction)
   - [Preamble](#preamble)
   - [Motivations](#motivations)
   - [Goals](#goals)
   - [Non-Goals](#non-goals)
2. [Release Types](#release-types)
   - [Feature Releases](#feature-releases)
   - [Patch Releases](#patch-releases)
   - [Pre-Releases](#pre-releases)
   - [Emergency Patch Releases](#emergency-patch-releases)
3. [Timetable](#timetable)
   - [Major Version Schedule](#major-version-schedule)
   - [Pre-Release Schedule](#pre-release-schedule)
   - [Patch Schedule](#patch-schedule)
4. [Content Curation](#content-curation)
    - [Labels](#labels)
    - [Branches](#branches)
5. [Conclusion](#conclusion)
    - [Paradigm Versions](#addendum-1-paradigm-versions)
    - [Failure Standards](#addendum-2-failure-standards)

## Introduction

### Preamble

This document defines the structure of future Skript releases, the kinds of material included in releases and the outline of the dates on which releases will be published.

A 'release' is the publication of a verified and signed build artefact on the GitHub releases tab, made available for all users to download and install.

This document does *not* cover the distribution or publication of artifacts built in other ways (e.g. privately, from a nightly action) or those not published from our GitHub (e.g. test builds shared in our public testing group).

Plans for a new release model began in March 2023 and several models were discussed, with this being the final version agreed upon by the organisation's administrative group and approved by the core contributors.

### Motivations

The release cycle for the `2.7.0` version was significant in that it took an unusually long time and included an unusually-large number of additions and changes.

While it was not the first version to have taken a long time to finalise and produce, it was distinct in that a lot of time had passed since the previous public build of Skript having been marked stable.

Members of the organisation and the wider community identified several problems that resulted from this, some of which are (not exhaustively) detailed below:
- 291 days had passed since the previous release
    - Users were unable to benefit from bug fixes or new features produced during that time
    - Although beta versions were released, these were marked unstable and were not fully tested
- When the release arrived it contained a very large number of changes and additions
    - Some users were unaware of changes that could not be extensively documented in the changelog or were buried in a large list
    - Users who obtained a build elsewhere (e.g. direct download, automatic installer) may have been unaware of the scale of the changes
- Several additions were made at short notice and without sufficient testing
    - Some of these introduced problems that required fixes in a following `2.7.1` patch
- Several features could not be completed in time and had to be dropped to a future `2.8.0` version
    - One result of this was that any corrections or improvements made as part of these were not present in `2.7.0`
    - Aspects of some of these larger-scale changes had to be re-created or cherry-picked for the `2.7.0` version
- The release lacked a clear timetable or vision for what additions to include
    - The initial timetable was not adhered to; plans were made for pre-releases to begin at the end of November 2022, which was delayed to early December in order to accommodate a large feature PR (which was eventually dropped to `2.8.0`)
    - Delays persisted, and the final release took place 7 months later in September 2023
    - There was no clear cut-off point for new features; feature pull requests were being included even up to 3 days before release

Of these, the principle complaint is that the `2.7.0` version took a significant amount of time to finish and this had an adverse effect on the community and the wider ecosystem.

### Goals

Our release model has been designed to achieve the following goals:
1. To reduce the delay between finishing new features and releasing them to the public.
2. To significantly reduce the time between an issue being fixed and that fix being made public in a stable build.
3. To reduce the risk of untested changes going into a release.
4. To make the release timetable clear and accessible.
5. To prevent a version being indefinitely delayed to accommodate additional changes.

### Non-Goals

This release model is not intended to change any of the following:
- The content or feature-theme of a particular version.
- The process for reviewing or approving changes.

## Release Types

This section details the different categories of version for release.

The versioning will follow the form `A.B.C`, where `B` is a [feature version](#Feature-Releases) containing changes and additions, `C` is a [patch version](#Patch-Releases) containing only issue fixes, and `A` is reserved for major paradigmatic changes.

### Feature Releases
A 'feature' version (labelled `0.X.0`) may contain:
- Additions to the language (syntax, grammar, structures)
- Bug fixes
- Developer API additions and changes
- Breaking changes<sup>1</sup>

All content added to a feature version must pass through the typical review process.
Content must also have been included in a prior [pre-release](#Pre-Releases) for public testing.

> <sup>1</sup> Breaking changes are to be avoided where possible but may be necessary, such as in a case where a significant improvement could be made to an existing feature but only by changing its structure somehow.
> Such changes should be clearly labelled and well documented, preferably giving users ample notice.

### Patch Releases
A 'patch' version (labelled `0.0.X`) may contain:
- Bug fixes
- Non-impactful<sup>2</sup> improvements to existing features
- Changes to meta content (e.g. documentation)

There may be **very rare** occasions when a breaking change is necessary in a patch release. These may occur if and only if: either a breaking change is required in order to fix an issue, and the issue is significant enough to need fixing in a patch rather than waiting for a major release, or an issue occurred with an inclusion in the version immediately-prior to this, which must be changed or reverted in some way.

All content added to a patch version must pass through the typical review process.

> <sup>2</sup> A non-impactful change is one in which there is no apparent difference to the user in how a feature is employed or what it does but that may have a material difference in areas such as performance, efficiency or machine resource usage.

### Pre-Releases

A 'pre-release' version (labelled `0.X.0-preY`) will contain all of the content expected to be in the feature release immediately following this.

Pre-release versions are a final opportunity for testing and getting public feedback on changes before a major release, allowing time to identify and fix any issues before the proper release, rather than needing an immediate patch.

The content of a pre-release should be identical to the content of the upcoming release -- barring any bug fixes -- and new content should never be included after a pre-release.

### Emergency Patch Releases

An 'emergency patch' version will be released if a critical security vulnerability is reported that the organisation feels prevents an immediate risk to the user base, such that it cannot wait for the subsequent patch.

An emergency patch will be labelled as another patch version (`0.0.X`). It should be noted that an emergency patch will *not* disrupt the typical timetable detailed below.

These kinds of releases may be published immediately and do not have to go through the typical reviewing and testing process. \
They must never include content, additions or unnecessary changes.

The only content permitted in an emergency patch is the material needed to fix the security risk.

The exact nature of the security vulnerability (such as the means to reproduce it) should not be included in the notes surrounding the release.

## Timetable

The 'clockwork' release model follows a strict monthly cycle, with versions being released on exact dates.

A table of (expected) dates is displayed below.

| Date     | Release Type    | Example Version<br>Name |
|----------|-----------------|-------------------------|
| 1st Jan  | Pre-release     | 0.1.0-pre1              |
| 15th Jan | Feature release | 0.1.0                   |
| 1st Feb  | Patch           | 0.1.1                   |
| 1st Mar  | Patch           | 0.1.2                   |
| 1st Apr  | Patch           | 0.1.3                   |
| 1st May  | Patch           | 0.1.4                   |
| 1st Jun  | Patch           | 0.1.5                   |
| 1st Jul  | Pre-release     | 0.2.0-pre1              |
| 15th Jul | Feature release | 0.2.0                   |
| 1st Aug  | Patch           | 0.2.1                   |
| 1st Sep  | Patch           | 0.2.2                   |
| 1st Oct  | Patch           | 0.2.3                   |
| 1st Nov  | Patch           | 0.2.4                   |
| 1st Dec  | Patch           | 0.2.5                   |

An estimated 14 releases are expected per year, with 10 patches, 2 pre-releases and 2 feature-releases that immediately follow them.

Please note that the actual number may differ from this in cases such as:
- A version requiring multiple pre-releases to correct mistakes (`0.3.0-pre1`, `0.3.0-pre2`)
- An emergency patch having to be released
- No bug fixes being prepared in a month, meaning no patch is needed

There is no fixed timetable for the circulation of unpublished builds to the public testing group or the addon developers group.

### Major Version Schedule

A [feature version](#feature-releases) will be released on the **15th of January** and the **15th of July**.

This will include all finished content from the previous 6 months that was tested in the pre-release.

Any features, additions or changes that were *not* ready or approved at the time of the pre-release may **not** be included in the feature release [according to goal 3](#goals). \
The feature release must **not** be delayed to accomodate content that was not ready by the deadline [according to goal 5](#goals).

If there is no content ready at the scheduled date of a feature release, the release will be skipped and a notice published explaining this.

### Pre-Release Schedule

A [pre-release](#pre-releases) will be released on the **1st of January** and the **1st of July**, leaving two weeks before the following release for public testing to occur.

This pre-release may include all finished content from the previous 6 months.

Any features, additions or changes that have *not* passed the review/approval process by the day of the pre-release may **not** be included in the pre-release [according to goal 3](#goals). \
The pre-release must **not** be delayed to accomodate content that was not ready by the deadline [according to goal 5](#goals).

If there is no content ready at the scheduled date of a pre-release, the entire feature-release will be skipped and a notice published explaining this.

If issues are found requiring a new build be produced (e.g. the build fails to load, a core feature is non-functional, a fix was made but needs additional testing) then another version of the pre-release may be published.
There is no limit on the number of pre-releases that can be published if required.

### Patch Schedule

A [patch](#patch-releases) will be released on the **1st** of every month (except January and July) containing any fixes prepared during the previous month(s).

On the 1st of January and July the patch will be replaced by the pre-release.

A patch should include all bug fixes from the previous month that have passed the review/approval process.

Ideally, a patch build should be circulated in the public testing group prior to its release, but this is not a strict requirement.

If there are no applicable bug fixes ready by the scheduled date of the patch then the month will be skipped and the patch will not take place. A public notice is not required to explain this.

## Content Curation

To help curate content on our GitHub repository we have designed a new branch model and accompanying labels for categorising contributions.

### Labels

We shall provide issue and pull request labels to help categorise changes to prevent contributions missing a release (or slipping into the incorrect kind of release).

1. `patch-ready` \
   to denote a pull request that has:
    - passed the review/approval process
    - is of the sufficient kind to be included in a monthly patch version
2. `feature-ready` \
   to denote a pull request that has:
    - passed the review/approval process
    - should wait for a biannual feature release
    - is not suitable to be included in a patch

### Branches

We shall maintain three core branches: `dev/patch`, `dev/feature` and `master`, which function vertically<sup>3</sup>.

We may also create legacy branches where necessary. \
As an example, if a previous release, say `2.6.4` requires an emergency security update, a branch can be made from its release tag and the patch may directly target that branch (and be released).

We may also maintain other assorted branches for individual features, for the purpose of group work or for experimentation. These are not detailed below.

> <sup>3</sup> Changes are always made to the 'top' (working) branch and then this is merged downwards into the more stable branch below when required.
> 
> Branches are never merged upwards.

#### Patch

Pull requests that only address issues or are otherwise suitable for a patch release should target the **`dev/patch` branch**. These may be merged whenever appropriate (i.e. all review and testing requirements have passed).

At the time of the patch release, the **patch branch** will be merged downwards into the **master branch**, and a release will be created from the **master branch**.

When a feature release occurs and all branches are merged into the master branch, the patch branch will be rebased off the current master commit, effectively bringing it up to speed with the new changes. \
As an example, when feature version 0.5.0 releases, the patch branch will be at 0.4.5 and missing the new features, so must be rebased off the current release and catch up before changes for version 0.5.1 may be merged.

#### Feature

Pull requests that add features, make breaking changes or are otherwise unsuitable for a patch version should target the **`dev/feature` branch**. \
These should be merged whenever appropriate (i.e. all review and testing requirements have passed), so that testing builds can be created and circulated in the public testing group.

The **patch branch** may be merged downwards into the **feature branch** whenever appropriate (e.g. after changes have been made to it that may affect the state of contributions targeting the feature branch).

The feature branch should __**never**__ be merged upwards into the patch branch<sup>4</sup>.

The feature branch is only merged downwards into the master branch directly before a full feature release (i.e. after the pre-release and testing is complete.)

Pre-releases are made directly from the feature branch<sup>5</sup>. At the end of the pre-release testing period the feature branch can be merged downwards into the master branch in order for the full release to be made.

> <sup>4</sup> Merges only ever occur downwards. For the patch branch to see changes from the feature branch it must be rebased onto master branch after a feature release occurs.
> 
> <sup>5</sup> Merging the branch down for the pre-release would introduce potentially-buggy, untested changes to the master branch.

#### Master

The **`master` branch** should reflect the most recent feature release.
Pull requests should **never** directly target the master branch. Changes are made to one of the other branches (as applicable) and then that branch is merged downwards into the **master branch** only when it is time for a release.

This means that any user building from the master branch is guaranteed to receive a safe, stable build of the quality that we would release.

The master branch should never be merged upwards into the feature or patch branches. If these branches need to see changes from the master branch they must be rebased onto the latest master branch commit.

## Conclusion

It is our aim that this release model will address all of our goals and satisfy our motivations.

Setting a strict and regular schedule ought to prevent too much time passing without a release, while also helping to prevent a single release from becoming bloated and overbearing.

By including testing requirements and mandating public pre-releases we hope to solve the persistent issue of untested changes slipping into supposedly-stable versions.

Finally, by scheduling regular patches we aim to reduce the time between a bug being 'fixed' by a contributor and the userbase being able to benefit from that fix. Keeping these patches as small, controlled releases allows us to mark them as 'stable' therefore letting the version reach a wider audience.

### Addendum 1: Paradigm Versions

Paradigmatic `X.0.0` versions were deliberately excluded from this proposal. \
The reasoning behind this choice was that over 10 years have passed since the inception of major version`2.0.0` in 2012, the previous paradigmatic change.

As of writing this document there are proposals and roadmaps for a version `3.0.0` but no timetable or predicted date on the horizon.

This kind of version, were it to be released, would likely take the place of a typical feature release in the model calendar, i.e. occurring on the 15th of January or July. However, due to its potentially-significant nature it may require exceptional changes to the pre-release cycle.

As details of such a version are neither known nor easy to predict, it has been left to the discretion of the future team to be decided when required.

### Addendum 2: Failure Standards

No proposal is complete without failure standards; this model can be deemed to have failed if, in two years' time:
1. The delay between finishing new features and releasing them to the public has not been reduced.
2. The delay between an issue being fixed and that fix being made public in a stable build has not been reduced.
3. Untested features are being released in 'stable' builds.
4. The release timetable is unclear or inaccessible.
5. Versions are being indefinitely delayed to accommodate additional changes.

Additionally, if this model is considered to have put an undue burden on the core development team, to the extent that it has hampered progress in a significant and measurable way, then it can be considered to have failed.
