# Code Conventions

## Security
Keep in mind that your code will be running on hundreds or thousands of
servers; security matters. Following these guidelines is *vital* for all new
code. Contributors should also see the dedicated
[security document](security.md).

Our users have an expectation of security and trust; it is important we make sure our code is safe.


### Malicious Code
No contributions should contain malicious code designed to harm the user or their machine. 
Nothing should intend to damage or delete system files, the user's personal files or unrelated parts of the Minecraft server (like other plugins' files).
```java
Runtime.getRuntime().exec("rm -rf /"); // bad, don't do this
```
While we expect contributors to use common sense, a general rule would be that contributions should not modify or delete
 resources unless:
 1. They belong to or are associated with Skript (variables, Skript files)
 2. They are expected to be modified by Skript (minecraft world data, player data)

Contributions to Skript should not include code or syntax with an easy potential to be exploited for malicious purposes (e.g. syntax to run exec commands, broad access to the filesystem unrelated to the Minecraft server).
While any code has the _potential_ to be abused or cause accidental damage, we would ideally like to limit this where possible.


### Secrets and Personal Data
Contributions should **never** include secret tokens, passwords, personal api keys, GitHub tokens, etc.
This code may be run across tens of thousands of servers, giving all those users access to that private resource.

While we would expect to catch an accidental password share at the review stage, please do your best not to commit them!
Once a commit is made, the information may be publicly available forever even if deleted by us - if you _do_ commit your password, please change it as quickly as possible.

Please do not include personal or identifying data (names, emails, addresses, etc.) in contributions.
Your GitHub account will be automatically credited in the contributor record; you do not need to include your name or by-line in comments or documentation.


### Server Safety
Contributions should avoid slowing or making the server unstable.
Explicit pauses (`sleep`/`wait`) are almost never appropriate, particularly considering Skript's single-threaded design.

Easy infinite loop traps should also be avoided where possible.
Please be especially wary of syntax people might accidentally (mis)use.

I/O operations should **never** be exposed via syntax. \
This includes but is not limited to:
 - File syntax
 - Network I/O syntax
 - Remote socket syntax

These operations are natively slow and unsafe (particularly if they involve contacting another machine) 
and Skript does not currently have the tools for users to employ them safely (e.g. thread support, error catching, resource closing.)

That said, some syntax may be required to use I/O operations (e.g. logging to a file.) \
For contributions including necessary I/O please make sure:
 1. All resource streams are handled using a `try-with-resources` block or closed **safely** in a `finally` block.
 2. Output does not needlessly block the server thread.
 3. Operations respect other concurrent I/O (make sure resources are available/safe to use, be careful of concurrent access.)

Operations that access a remote machine will almost never be appropriate for Skript.
With the exception of contacting our own resources (e.g. to check for updates) contributions should include no form of automatic installation/fetching or download of third-party resources.


## Licensing

Code contributed must be licensed under GPLv3, by **you**.
We expect that any code you contribute is either owned by you or you have explicit permission to provide and license it to us.

Licenses do not need to be printed in individual files (or packages) unless the licence applying to the code in
that file (or package) deviates from the licence scope of its containing package.

Third party code (under a compatible licence) _may_ be accepted in the following cases:
 - It is part of a public, freely-available library or resource.
 - It is somehow necessary to your contribution, and you have been given permission to include it.
 - You have previously co-authored the code, the other contributors have given you permission to include it.

If we receive complaints regarding the licensing of a contribution we will forward the complaints to you the contributor.

If you have questions or complaints regarding the licensing or reproduction of a contribution you may contact us (the organisation) or the contributor of that code directly.

If, in the future, we need to re-license contributed code, we will contact all contributors involved.
If we need to remove or alter contributed code due to a licensing issue we will attempt to notify its contributor.


## Code Style

### Formatting
* Imports should be grouped together by type (e.g. all `java.lang...` imports together)
  * Following the style of existing imports in a class is encouraged, but not required
  * Wildcard `*` imports are permitted (as long as they do not interfere with existing imports), e.g. `java.lang.*`.
* Tabs, no spaces (unless in code imported from other projects)
	- No tabs/spaces in empty lines
* No trailing whitespace
* At most 120 characters per line
  	- In Javadoc/multiline comments, at most 80 characters per line
* When statements consume multiple lines, all lines but the first have two tabs of additional indentation
	- The exception to this is breaking up conditional statements (e.g. `if (x || y)`) where the
    condition starts may be aligned
* Each class begins with an empty line
* No squeezing of multiple lines of code on a single line
* Separate method declarations with empty lines
  	- Empty line after last method in a class is *not* required
  	- Otherwise, empty line before and after method is a good rule of thumb
* If fields have Javadoc, separate them with empty lines
* Use empty lines liberally inside methods to improve readability
* Use curly brackets to start and end most blocks
  	- When a block contains a single statement, they may be omitted
	    - Brackets may not be omitted in a chain of other blocks that require brackets, e.g `if ... else {}`
  	- When omitting brackets, still indent as if the code had brackets
  	- Avoid omitting brackets if it produces hard-to-read or ambiguous code
* Ternaries should be avoided where it makes the code complex or difficult to read
* Annotations for methods and classes are placed in lines before their declarations, one per line
  - Annotations for a structure go on the line before that structure
      ```java 
      @Override
      @SuppressWarnings("xyz")
      public void myMethod() {
            // Override goes above method because method is overriding
      }
      ```
    
  - Annotations for the _value_ of a thing go before that value's type declaration
      ```java 
      @Override
      public @Nullable Object myMethod() {
          // Nullable goes before Object because Object is Nullable
      }
      ```
* When there are multiple annotations, it looks nicer to place them in length order (longest last) 
but this is not strictly required:
	```java 
	@Override
	@Deprecated
	@SuppressWarnings("xyz")
	```
* When splitting Strings into multiple lines the last part of the string must be (space character included) " " +
  ```java
  String string = "example string " +
        "with more to add";
  ```
  
* When extending one of following classes: SimpleExpression, SimplePropertyExpression, Effect, Condition...
  - Put overridden methods in order
  - Put static registration before all methods
  - SimpleExpression: init -> get/getAll -> acceptChange -> change -> setTime -> getTime -> isSingle -> getReturnType -> toString
  - SimplePropertyExpression: -> init -> convert -> acceptChange -> change -> setTime -> getTime -> getReturnType -> getPropertyName
  - Effect: init ->  execute  -> toString
  - Condition: init -> check -> toString
  - PropertyCondition: (init) -> check -> (getPropertyType) -> getPropertyName
  - Section: init -> walk -> toString
  - Structure: init -> (preLoad) -> load -> (postLoad) -> unload -> (postUnload) -> (getPriority) -> toString


### Naming
* Class names are written in `UpperCamelCase`
  - The file name should match its primary class name (e.g. `MyClass` goes in `MyClass.java`.)
* Fields and methods named in `camelCase`
  - Static constant fields should be named in `UPPER_SNAKE_CASE`
* Localised messages should be named in `lower_snake_case`
  - And that is the only place where snake_case is acceptable
* Use prefixes only where their use has been already established (such as `ExprSomeRandomThing`)
  - Otherwise, use postfixes where necessary
  - Common occurrences include: Struct (Structure), Sec (Section), EffSec (EffectSection), Eff (Effect), Cond (Condition), Expr (Expression)
* Ensure variable/field names are descriptive. Avoid using shorthand names like `e`, or `c`
  - e.g. Event should be `event`, not `e`. `e` is ambiguous and could mean a number of things
  
### Comments
* Prefer to comment *why* you're doing things instead of how you're doing them
  - In case the code is particularly complex, though, do both!
* Write all comments in readable English
  - Remember the basic grammar rules
  - Fun is allowed, if that does not hurt the readability
* No matter if a comment contains a sentence or not, capitalize the first letter
* For single line comments
  - Always start with a space; it improves readability
  - You *may* put them at ends of lines, if the line doesn't get too wide due to that

Your comments should look something like these:
```java
// A single line comment

/**
 * This is a Javadoc comment.
 * @param first Description of the first parameter.
 * @param second Description of the second parameter.
 * @return Description of the return value.
 */
 
/*
 * This is a block comment.
 */
```

## Language Features

### Compatibility
* Contributions should maintain Java 17 source/binary compatibility, even though compiling Skript requires Java 21
  - Users must not need JRE newer than version 17
* Versions up to and including Java 21 should work too
  - Please avoid using unsafe reflection
* It is recommended to make fields final, if they are effectively final
* Local variables and method parameters should not be declared final unless used in anonymous classes, lambdas
or try-with-resources sections where their immutability is necessary
* Methods should be declared final only where necessary
* Use `@Override` whenever applicable
  - They may be omitted to prevent compilation errors when something overrides only
  on a version-dependent basis (e.g. in Library XYZ version 2 we override `getX()` but in version 3 it's
  gone, and we call it ourselves)

### null-ness
* We use **JetBrains** Annotations for specifying null-ness and method contracts.
  * If editing a file using a different annotation set (e.g. Javax, Eclipse Sisu, Bukkit)
  these should be replaced with their JetBrains equivalent.
  * The semantics for JetBrains Annotations are strict _and should be observed!_
    * Many IDEs have built-in compiler-level support for these, and can even be set to produce strict
    errors when an annotation is misused; do not misuse them.
  * **`@NotNull`**
    * > An element annotated with NotNull claims null value is forbidden to return (for methods), 
    pass to (parameters) and hold (local variables and fields).
	* Something is `@NotNull` iff it is never null from its inception (new X) to its garbage collection, 
    i.e. there is no point in time at which the value could ever be null.
  * **`@Nullable`**
    * > An element annotated with Nullable claims null value is perfectly valid to return (for methods), 
	  > pass to (parameters) or hold in (local variables and fields). 
	  > 
	  > By convention, this annotation applied only when the value should always be checked 
	  > against null because the developer could do nothing to prevent null from happening.
	* Something is `@Nullable` iff there is _absolutely no way of determining_ (other than checking its 
    value `!= null`) whether it is null.
    * In other words, if there is another way of knowing (e.g. you set it yourself, an `isPresent` method, etc.)
    then it should not be marked nullable.
  * **`@Contract`**
    * The contract annotation should be used to express other behaviour (e.g. null depending on parameters).
* All fields, method parameters and their return values are non-null by default
  - Exceptions: GitHub API JSON mappings, Metrics
* When ignoring warnings, use the no-inspection comment rather than a blanket suppression annotation
* Use assertions liberally: if you're sure something is not null, assert so to the compiler
  - Makes finding bugs easier for developers
* Assertions must **not** have side-effects in non-test packages - they may be skipped in real environments
* Avoid checking non-null values for nullability
  - Unless working around buggy addons, it is rarely necessary
  - This is why ignoring null-ness errors is particularly dangerous
* Annotations on array types **must** be placed properly:
  * Annotations on the array itself go before the array brackets
	```java
	@Nullable Object @NotNull []
	// a not-null array of nullable objects
	```
  * Annotations on values inside the array go before the value declaration
	```java
	@NotNull Object @Nullable []
	// a nullable array of not-null objects
	```
  * If this is not adhered to, an IDE may provide incorrect feedback.

### Assertions

Skript must run with assertations enabled; use them in your development environment. \
The JVM flag <code>-ea</code> is used to enable them.

## Code Complexity

Dense, highly-complex code should be avoided to preserve readability and to help with future maintenance, 
especially within a single method body.

There are many available metrics for measuring code complexity (for different purposes); [we have our own](https://stable.skriptlang.org/Radical_Complexity.pdf).
There are no strict limits for code complexity, but you may be encouraged (or required) to reformat or break up methods
into smaller, more manageable chunks. If in doubt, keep things simple.

## Minecraft Features

### Avoid Version-Specific Code

Contributions must **never** rely on the variable `net.minecraft.server` package.
```java
import net.minecraft.server.v1_13_1.*; // BAD
```
This makes updating between versions excessively difficult, and requires us to write different code for all supported versions.
If a feature is not accessible via the Bukkit API it should not be included in Skript.
> For features requiring 'NMS' please consider making a third-party addon.

### Support the Target Versions
The target Minecraft versions are written in our README.

Skript **must** run on these MC versions, in one way or another.
If your contribution breaks compatibility for any of these versions we cannot accept it.

Please try to make sure contributions are future-safe, to the best of your ability.
Where possible, avoid using version-specific code to target a feature (e.g. accessing different copies of an internal class via reflection for each minecraft version) as this creates additional maintenance work every time a new version releases.
Checking whether a class exists in order to target supported versions is acceptable.

### Support the Target Servers

Skript currently supports the 'Spigot' and 'Paper' server implementations.
Contributions must **not** break this cross-compatibility.
> This may change in the future as the server landscape shifts - we will note any changes here and in the README.

Paper-specific functionality and syntax are acceptable. Please make sure these contributions do not break compatibility with Spigot.

Skript may also run on other server platforms. While these are not supported, please do not deliberately break compatibility for them.

We do not support Bukkit/CraftBukkit.

### Class Use

Prefer `Aliases.javaItemType` to the `Material` enum, as this may change in future versions.
```java
Material type = Material.DIRT; // Bad
ItemType type = Aliases.javaItemType("dirt"); // Good
```

The exceptions are `Material.AIR`, which is a good way to represent "nothing"
and `Material.STONE` which can be used to get a dummy `ItemMeta`.

Prefer to avoid referencing the Biome enum directly, since it has changed between versons in the past.
