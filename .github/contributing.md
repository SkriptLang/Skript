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
So, you have found out a potential Skript bug. By reporting it correctly, you
can ensure that it will be correctly categorized as a bug and hopefully, fixed.

First, please make sure you have **latest** Skript version available. If there
are no stable (no pre-release tag) versions, this means latest dev build.
If you can find non-prerelease in downloads page of this repository, you may
also use that one. Do **not** use 2.2, 2.1 or older, since while they are
technically stable, they tend to not work reliably with Minecraft 1.9+.

Second, test without addons. No, seriously; unless you're an addon developer,
test without plugins that hook to Skript before reporting anything. We can't
help you with addon issues here, unless we get a lot of technical information
about the addon in question. Issues that are not tested without addons are
likely to be ignored by the core team.

If the issue still persists, search the issue tracker for similar
errors and check if your issue might have been already reported.
Only if you can't find anything, open a new issue.

When opening an issue, pick a template for a bug report and fill it.
We may ignore or close issues that are not made with correct templates.

## Pull Requests
Pull requests are a great way to contribute code, but there are still a few
guidelines on how to use them.

Note that these guidelines do not apply to pull requesting changes to
documentation. For that kind of pull requests, just use common sense.

### What to Contribute?
You can find issues tagged with "help wanted" on tracker to see if there is
something for you. If you want to take over one of these, just leave a comment
so other contributors don't accidentally pick same issue to work on. You can also
offer your help to any other issue, but for "help wanted" tasks, help is really
*needed*.

### Before Programming...
If you did not pick an existing issue to work on, you should perhaps ask if your
change is wanted. This can be done by opening an issue or contacting developer
directly via Discord.

Then, a few words of warning: Skript codebase will not be pleasant, easy or
that sane to work around with. You will need some Java skills to create anything
useful in sane amount of time. Skript is not a good programming/Java learning
project!

Still here? Good luck. If you did not learn how to use Git, now might be a good
time to [learn](https://help.github.com/categories/bootcamp/).

### When Programming
We recommend using an IDE; you can find some set up instructions in README.
Please also follow our [code conventions](https://github.com/SkriptLang/Skript/blob/master/code-conventions.md).

### After Programming
Test your changes. Actually, test more than your changes: if you think that you
might have broken something unrelated, better to test that too. Nothing is more
annoying than breaking existing features.

After manually testing, try to write some automated
[test scripts](https://github.com/SkriptLang/Skript/blob/master/src/test/skript/README.md)
if possible. Remember that not everything can be tested this way, though.

When you are ready to submit a pull request, please follow the template. Don't
be scared, usually everything goes well and your pull request will be present
in next Skript release, whenever that happens.

Good luck!

### Pull Request Review
Pull requests require one passing review before they can be merged. In
addition to that, code submitted by people outside of core team must be tested
by core team members. In some cases, this might be as simple as running the
automated tests.

In exceptional situations, pull requests may be merged regardless of review
status by @bensku.
