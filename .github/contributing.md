# Contributing to Skript
Skript is an open source project, and you're encouraged to contribute to it.
Both reporting issues and writing code help us. However, please review the
following guidelines *before* doing either of these. Properly created issues
and pull requests are often resolved faster than those that ignore them.

## Behavior
Please treat others with respect in the issue tracker and comments of pull
requests. We hope that you are a decent person and do this without telling.
Failing that, issues where inappropriate behavior is observed may be ignored
closed or even deleted. Repeated or particularly egregious behavior will get
you banned from issue trackers of SkriptLang organization.

Access to Skript's source code is a right that everyone with a binary release
of it has. Access to our communications platforms is a *privilege* that will
be taken away if misused.

## Issues
Issues are usually used to report bugs and request improvements or new features.

Script writers should **not** use issue tracker to ask why their code is broken,
*unless* they think it might be a bug. Correct places for getting scripting advise
are SkUnity forums and Discord (see README again).

Don't be scared to report real bugs, though. We won't be angry if we receive
invalid reports; it is just that you're unlikely to get help with those here.

### Reporting Bugs

First, please make sure you have **latest** Skript version available. This means
the latest stable (non-prerelease) version available under the releases page.
If you are reporting a bug specific to a prerelease version, please make sure you
are using the latest prerelease. If the bug is already fixed in latest version,
please do not report it.

Second, test without addons. We won't help you with addon issues here.
Issues that are not tested without addons are more likely to be ignored by the core team.

If the issue still persists, search the issue tracker for similar
errors and check if your issue might have been already reported.
Only if you can't find anything, open a new issue. If you are unsure,
open the issue anyway; we can always close it if needed.

When opening an issue, pick a template for a bug report and fill it.
We may ignore or close issues that are not made with correct templates.
Please provide the full output of `/sk info` as requested in the template.

## Pull Requests

### Pull Request Cheat Sheet
A tl;dr version of the guidelines for contributing code:
- Use the correct branch: `dev/feature` for new features or breaking changes, `dev/patch` for bug fixes and documentation changes.
- Follow the code conventions. Disclose any AI assistance used.
- Test your changes thoroughly, run the `quickTest` gradle task. Add more tests if possible.
- Fill out the pull request template.
- Respond to review comments in a timely and respectful manner.
- Request re-reviews when you have addressed requested changes.
- Feel free to politely ask for updates if your pull request has been open for a week or two without response.
- Your contribution will be merged if it is ready prior to a release, so no need to worry.

### Choosing What to Work On
You can find issues tagged with "good first issue" on tracker to see if there is
something for you. If you want to take over one of these, just leave a comment
so other contributors don't accidentally pick same issue to work on. You can also
offer your help to any other issue you want to work on!

If you did not pick an existing issue to work on, you should perhaps ask if your
change is wanted. Some changes may have already been considered and rejected, or
may not fit into Skript's design goals. Opening an issue to discuss your idea is a good
way to avoid wasted effort.

Then, a few words of warning: Skript codebase will not be pleasant, easy or
that sane to work around with. You will need some Java skills to create anything
useful in sane amount of time. Skript is not a good programming/Java learning
project!

### Getting Started

We welcome contributions from everyone. To get started, please fork the repository 
and clone it to your local machine. If you are unfamiliar with Git, please refer to the
[Git documentation](https://git-scm.com/learn).
```
git clone https://github.com/SkriptLang/Skript
```

Create a new branch for your changes. Use a descriptive name for the branch,
such as `feature/your-feature-name` or `patch/fix-issue-number`. We request that 
you base your changes on `dev/feature` branch for new features or breaking changes,
 and `dev/patch` branch for bug fixes and documentation changes.
```
git checkout -b feature/your-feature-name dev/feature
git checkout -b patch/fix-issue-number dev/patch
```

We recommend using an IDE such as IntelliJ (recommended) or Eclipse. 
Please also make sure to follow our [code conventions](https://github.com/SkriptLang/Skript/blob/master/code-conventions.md).

Building Skript requires Gradle. You can use the Gradle wrapper included in the
repository to build Skript without installing Gradle globally.
```
./gradlew clean build
```

Finally, we request that you write descriptive commit messages that summarize the changes.
We do not enforce any specific format or style for commit messages, but we ask that they
generally consist of more than `Fix bug`, `Add feature`, or `Updated FileClass.java`.

### AI assistance notice

> [!IMPORTANT]
>
> If you are using **any kind of AI assistance** to contribute to Skript,
> it must be disclosed in the pull request.

If you relied on AI assistance to make a pull request, you must disclose it in the
pull request, together with the extent of the usage. For example, if you used
AI to generate docs or tests, you must say it.
An example disclosure:

- > This PR was written primarily by Claude Code.
- > I consulted ChatGPT to understand the codebase but the solution
  > was fully authored manually by myself.

Providing this information helps reviewers understand the context of the
pull request and apply the right level of scrutiny, ensuring a smoother
and more efficient review process.

AI assistance isn't always perfect, even when used with the utmost care.

Please be respectful to maintainers and disclose AI assistance. Failure to do so
may lead to rejection of the pull request or removal of your ability to contribute
to the repository.

### After Programming
Test your changes. Actually, test more than your changes: if you think that you
might have broken something unrelated, better to test that too. Nothing is more
annoying than breaking existing features. Please run the `quickTest` gradle task prior
to submitting your changes to ensure that existing tests pass.
```
./gradlew clean quickTest
```

**After manually testing, try to write some automated
[test scripts](https://github.com/SkriptLang/Skript/blob/master/src/test/skript/README.md)
if possible**. Remember that not everything can be tested this way, though.

When you are ready to submit a pull request, please follow the template. Don't
be scared, usually everything goes well and your pull request will be present
in next Skript release, whenever that happens.

You should target the `dev/feature` branch for changes that add features or break existing functionality. \
For bug fixes and documentation changes, target the `dev/patch` branch.

Good luck!

### Submitting a Contribution

Having submitted your contribution it will enter a public review phase.

Other contributors and users may make comments or ask questions about your contribution. \
You are encouraged to respond to these - people may have valuable feedback!

Developers may request changes to the content of your pull request. These are valuable 
since they may concern unreleased content. Please respect our team's wishes - changes 
are requested when we see room for improvement or necessary issues that need to be addressed.
Change requests are not an indictment of your code quality.

Developers may also request changes to the formatting of your code and attached files. 
These are important to help maintain the consistent standard and readability of our code base.

**You must respond to these requests**. You can disagree, or propose alternatives, but you must
engage in the discussion respectfully and in a timely manner. We will close pull requests
that do not address requested changes within 6 months, though this is at our discretion, 
and we may leave them open for more or less time depending on the situation. 

Once you have made the requested changes (or if you require clarification or assistance) 
you can request a re-review from the developer. Please make sure to resolve any requested 
changes you addressed.

You don't need to keep your pull request fork up-to-date with Skript's master branch - 
we can update it automatically and notify you if there are any problems, but you must
allow edits from maintainers on your pull request.

### Merging a Contribution

Pull requests may be left un-merged until an appropriate time (e.g. before a suitable release.) 
We will try to merge contributions at a reasonable pace, but larger or more complex contributions
may be left open for longer to allow for more thorough review.

All pull requests that are ready to be merged prior to a release will be included in that release, 
so don't worry if your contribution is not merged immediately. If your pr has been open for a week 
without a response, feel free to politely ask for an update. We may be busy, but we will get to it.

For a contribution to be merged it requires at least two approving reviews from the core development 
team. It will then require a senior member to merge it.

You do not need to 'bump' your contribution if it is un-merged; we may be waiting for a more 
suitable release to include it.

If you have been waiting for a response to a question or change for a significant time 
please re-request our reviews or contact us. **Don't be shy about requesting reviews!** It's
one of the easiest ways for us to see what needs our attention.

In exceptional situations, pull requests may be merged regardless of review status 
by one of the organisation admins.

## Peaceful Resolution

Please respect our maintainers, developers, contributors and users. \
Our contributors come from a wide variety of backgrounds and countries - 
you may need to explain issues and requests if they are misunderstood.

Please refer disrespectful and unpleasant behaviour to our tracker team. 
For concerns about abuse, please contact the organisation directly.

## Acknowledgements
We would like to acknowledge the [biomejs](https://github.com/biomejs/biome/blob/main/CONTRIBUTING.md#ai-assistance-notice) 
team as the source and inspiration for our AI assistance notice.
