# Skript Testing System
Configuration and actual test scripts for Skript's integration test system can
found here.

## Contributing to Tests
We're not very strict about what can be added to our test suite.
Tests don't have to be good code - often, testing edge cases pretty
much requires weird code. That being said, there are a couple of things
to keep in mind:

* Use tabs for indentation
* Write descriptive assert messages and write comments
* Ensure your tests pass with all supported Skript versions
  * Use standard condition for checking MC version
* When writing tests for your pull requests, please ensure that manipulations are cleaned up afterwards. This includes actions such as removing mobs that were manually spawned and resetting manipulated blocks to their original state. Also, try to avoid spawning hostile mobs, as this can cause issues (e.g. a spawned zombie catching fire from the sun).

Note: The test world generate as a default super flat. Bedrock, dirt, grass, air, air...+ A good practice is to be using `spawn of world "world"` as a homing location, or anywhere near 0, 0, 0 is reasonable.

## Test Categories
Scripts under <code>tests</code> are run on environments. Most of them are
stored in subdirectories to help keep them organized.

### Syntax tests
Under <code>syntaxes</code>, there are tests for individual expressions,
effects and commands. Again, each in their own subdirectories. Names of files
should follow names of respective syntax implementations in Java
(e.g. <code>ExprTool.sk</code>).

Contributors can add tests for expressions that do not yet have them, or
improve existing tests.

### Regression tests
Under <code>regressions</code>, there are regression tests. Such tests are
created when bugs are fixed to ensure they do not come back in future.
File names should contain respective issue number and its title. If no issue
is available, PR number and title can be used in place of them.

For example, <code>2381-multiplication in list index.sk</code>. Use only
lower case letters in names, because issue titles are not consistent with
their capitalization.

Contributors should not add regression tests unless they also fix bugs in
Skript. Those who do fix bugs *should* write regression tests.

### Miscellaneous tests
All other tests go in this subdirectory. Contributions for generic tests
will need to meet a few criteria:

* They must not be duplicates of other tests
  * Similar tests are ok
* They must currently pass
* They should not rely on bugs in Skript to pass

Aside these things, pretty much anything goes.

## Testing Syntaxes
Test scripts have all normal Skript syntaxes available. In addition to that,
some syntaxes for test development are available.

* Minecraft version condition <code>running [(1¦below)] minecraft %string%</code>
  * Example: <code>if running minecraft "1.15":</code>
* Event test cases: <code>test %string% [when <.+>]</code>
  * Example: <code>test "test name" when running minecraft "1.18.1":</code>
  * Contents of tests are not parsed when conditions are not met.
  * Typically the condition isn't required.
  * Required to start a test script.
* Assertions are available as effects: <code>assert <.+> [(1¦to fail)] with [error] %string%[, expected [value] %object%, [and] (received|got) [value] %object%]</code>
  * Example: <code>assert {_entity} is a zombie with "failure message"</code> will error if it's not a zombie.
  * The optional 'expected' and 'got' values are used in the error report to show what the assertion expected and what it actually got.
  * Assertions using some conditions, like CondCompare and CondIsSet, may automatically fill in the expected/got values.
  * If the tag `to fail` is defined, it will assume the condition is to fail. If it's successful the string is printed.
* Take a look at existing tests for examples https://github.com/SkriptLang/Skript/tree/master/src/test/skript/tests
  <code>misc/dummy.sk</code> is useful for beginners
* case_equals Function. Returns boolean. Useful to check that all string values equal the same. Examples:
	* <code>case_equals("hi", "Hi") = false</code>
	* <code>case_equals("text", "text", "text") = true</code>
	* <code>case_equals({some list variable::*})</code>

## Test Development
Use Gradle to launch a test development server:

```
gradlew clean skriptTestDev --console=plain
```

Note: adding the tag `clean` will clear the build directory, making Skript generate a new server each time.
Don't include the `clean` tag if you want to keep the same server folder around each test.

The server launched will be running at localhost:25565. You can use console
as normal, though there is some lag due to Gradle. If you're having trouble,
try without <code>--console=plain</code>.

Server files are located at <code>build/test_runners</code>.

To run individual test files, use <code>/sk test \<file\></code>. To run last
used file again, just use <code>/sk test</code>.
