# JUnit testing system
This folder is for scripts that will load and be present for the Skript JUnit tests.
This allows a test to listen for events or persist during JUnit tests.

An example would be checking the damage of an entity. You would write a test script
and have the on damage event inside that script with assertion checking.
Then in the JUnit Java class, you would perform some action that would damage the entity.
That test script then catches that event and any errors will be included in the final results.
