package ch.njol.skript.registrations;

import ch.njol.skript.SkriptAddon;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

/**
 * Experimental feature toggles as provided by Skript itself.
 */
public enum Feature implements Experiment {

	EXAMPLES("Examples",
		"examples",
		"""
			A section used to provide examples inside code.
			
			```
			example:
				kick the player due to "you are not allowed here!"
			```
			""",
			LifeCycle.STABLE),
	QUEUES("Queues",
		"queues",
		"""
			A collection that removes elements whenever they are requested.
			
			This is useful for processing tasks or keeping track of things that need to happen only once.
			
			```
			set {queue} to a new queue of "hello" and "world"
			
			broadcast the first element of {queue}
			# "hello" is now removed
			
			broadcast the first element of {queue}
			# "world" is now removed
			
			# queue is empty
			```
			
			```
			set {queue} to a new queue of all players
			
			set {player 1} to a random element out of {queue}\s
			set {player 2} to a random element out of {queue}
			# players 1 and 2 are guaranteed to be distinct
			```
			
			Queues can be looped over like a regular list.
			""",
			LifeCycle.EXPERIMENTAL),
	FOR_EACH_LOOPS("For Loops",
		"for loop",
		"""
			A new kind of loop syntax that stores the loop index and value in variables for convenience.
			
			This can be used to avoid confusion when nesting multiple loops inside each other.
			
			```
			for {_index}, {_value} in {my list::*}:
				broadcast "%{_index}%: %{_value}%"
			```
			
			```
			for each {_player} in all players:
				send "Hello %{_player}%!" to {_player}
			```
			
			All existing loop features are also available in this section.
			""",
			LifeCycle.EXPERIMENTAL,
			"for [each] loop[s]"),
	SCRIPT_REFLECTION("Script Reflection",
		"reflection",
		"""
			This feature includes:
			
			- The ability to reference a script in code.
			- Finding and running functions by name.
			- Reading configuration files and values.
			""",
			LifeCycle.EXPERIMENTAL,
			"[script] reflection"),
	CATCH_ERRORS("Runtime Error Catching",
		"catch runtime errors",
		"""
			A new catch [run[ ]time] error[s] section allows you to catch and suppress runtime errors within it and access them later with [the] last caught [run[ ]time] errors.
			
			```
			catch runtime errors:
				...
				set worldborder center of {_border} to {_my unsafe location}
				...
			if last caught runtime errors contains "Your location can't have a NaN value as one of its components":
				set worldborder center of {_border} to location(0, 0, 0)
			```
			""",
			LifeCycle.EXPERIMENTAL,
			"error catching [section]"),
	TYPE_HINTS("Type Hints",
		"type hints",
		"""
			Local variable type hints enable Skript to understand what kind of values your local variables will hold at parse time. Consider the following example:
			
			```
			set {_a} to 5
			set {_b} to "some string"
			... do stuff ...
			set {_c} to {_a} in lowercase # oops i used the wrong variable
			```
			
			Previously, the code above would parse without issue. However, Skript now understands that when it is used, {_a} could only be a number (and not a text). Thus, the code above would now error with a message about mismatched types.
			
			Please note that this feature is currently only supported by simple local variables. A simple local variable is one whose name does not contain any expressions:
			
			```
			{_var} # can use type hints
			{_var::%player's name%} # can't use type hints
			```
			""",
			LifeCycle.EXPERIMENTAL,
			"[local variable] type hints"),
	DAMAGE_SOURCE("Damage Sources",
		"damage source",
		"""
			Damage sources are a more advanced and detailed version of damage causes. Damage sources include information such as the type of damage, the location where the damage originated from, the entity that directly caused the damage, and more.
			
			Below is an example of what damaging using custom damage sources looks like:
			
			```
			damage all players by 5 using a custom damage source:
				set the damage type to magic
				set the causing entity to {_player}
				set the direct entity to {_arrow}
				set the damage location to location(0, 0, 10)
			```
			
			For more details about the syntax, visit damage source on our documentation website.
			""",
			LifeCycle.EXPERIMENTAL,
			"damage source[s]");

	private final String displayName;
	private final String codeName;
	private final String description;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;

	Feature(@NotNull String displayName, @NotNull String codeName,
			@NotNull String description, @NotNull LifeCycle phase,
			String... patterns) {
		Preconditions.checkNotNull(codeName, "codeName cannot be null");
		Preconditions.checkNotNull(displayName, "displayName cannot be null");
		Preconditions.checkNotNull(description, "description cannot be null");

		this.displayName = displayName;
		this.description = description.strip();
		this.codeName = codeName;
		this.phase = phase;
		this.compiledPattern = switch (patterns.length) {
			case 0 -> PatternCompiler.compile(codeName);
			case 1 -> PatternCompiler.compile(patterns[0]);
			default -> PatternCompiler.compile('(' + String.join("|", patterns) + ')');
		};
	}

	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (Feature value : values()) {
			manager.register(addon, value);
		}
	}

	@Override
	public @NotNull String displayName() {
		return displayName;
	}

	@Override
	public @NotNull String description() {
		return description;
	}

	@Override
	public String codeName() {
		return codeName;
	}

	@Override
	public LifeCycle phase() {
		return phase;
	}

	@Override
	public SkriptPattern pattern() {
		return compiledPattern;
	}

}
