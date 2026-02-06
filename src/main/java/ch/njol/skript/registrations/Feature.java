package ch.njol.skript.registrations;

import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.docs.Documentable;
import org.skriptlang.skript.docs.Documentation;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentRegistry;
import org.skriptlang.skript.lang.experiment.LifeCycle;

/**
 * Experimental feature toggles as provided by Skript itself.
 */
public enum Feature implements Experiment, Documentable, ch.njol.skript.doc.Documentable {

	EXAMPLES("examples",
		Documentation.of(
			"Examples",
			"A section used to provide examples inside code.",
			"2.9",
			"""
				example:
					kick the player due to "You are not allowed here!"
				"""
		),
		LifeCycle.STABLE),
	QUEUES("queues",
		Documentation.of(
			"Queues",
			"""
				A collection that removes elements whenever they are requested.
				This is useful for processing tasks or keeping track of things that need to happen only once.
				Queues can be looped over like a regular list.
				""",
			"2.10",
			"""
				set {queue} to a new queue of "hello" and "world"
				
				broadcast the first element of {queue}
				# "hello" is now removed
				
				broadcast the first element of {queue}
				# "world" is now removed
				
				# queue is empty
				""",
			"""
				set {queue} to a new queue of all players
				
				set {player 1} to a random element out of {queue}\s
				set {player 2} to a random element out of {queue}
				# players 1 and 2 are guaranteed to be distinct
				"""
		),
		LifeCycle.EXPERIMENTAL),
	FOR_EACH_LOOPS("for loop",
		Documentation.of(
			"For Loops",
			"""
				A new kind of loop syntax that stores the loop index and value in variables for convenience.
				
				This can be used to avoid confusion when nesting multiple loops inside each other.
				
				All existing loop features are also available in this section.
				""",
			"2.10",
			"""
				for {_index}, {_value} in {my list::*}:
					broadcast "%{_index}%: %{_value}%"
				""",
			"""
				for each {_player} in all players:
					send "Hello %{_player}%!" to {_player}
				"""
		),
		LifeCycle.MAINSTREAM,
		"for [each] loop[s]"),
	SCRIPT_REFLECTION("reflection",
		Documentation.of(
			"Script Reflection",
			"""
				This feature includes:
				- The ability to reference a script in code.
				- Finding and running functions by name.
				- Reading configuration files and values.
				""",
			"2.10"
		),
		LifeCycle.STABLE,
		"[script] reflection"),
	CATCH_ERRORS("catch runtime errors",
		Documentation.of(
			"Runtime Error Catching",
			"""
				A new catch runtime errors section allows you to catch and \
				suppress runtime errors within it and access them later with \
				the last caught runtime errors.
				""",
			"2.12",
			"""
				catch runtime errors:
					...
					set worldborder center of {_border} to {_my unsafe location}
					...
				if last caught runtime errors contains "Your location can't have a NaN value as one of its components":
					set worldborder center of {_border} to location(0, 0, 0)
				"""
		),
		LifeCycle.EXPERIMENTAL,
		"error catching [section]"),
	TYPE_HINTS("type hints",
		Documentation.of(
			"Type Hints",
			"""
				Local variable type hints enable Skript to understand \
				what kind of values your local variables will hold at parse time. \
				Consider the following code:
				
				```
				set {_a} to 5
				set {_b} to "some string"
				... do stuff ...
				set {_c} to {_a} in lowercase # oops i used the wrong variable
				```
				
				Previously, the code above would parse without issue. \
				However, Skript now understands that when it is used, \
				{_a} could only be a number (and not a text). \
				Thus, the code above would now error with a message about mismatched types.
				
				Please note that this feature is currently only supported by simple local variables. \
				A simple local variable is one whose name does not contain any expressions:
				
				```
				{_var} # can use type hints
				{_var::%player's name%} # can't use type hints
				```
				""",
			"2.12"
		),
		LifeCycle.EXPERIMENTAL,
		"[local variable] type hints"),
	DAMAGE_SOURCE("damage source",
		Documentation.of(
			"Damage Sources",
			"""
				Damage sources are a more advanced and detailed version of damage causes. \
				Damage sources include information such as the type of damage, \
				the location where the damage originated from, the entity that \
				directly caused the damage, and more.
				""",
			"2.12",
			"""
				damage all players by 5 using a custom damage source:
					set the damage type to magic
					set the causing entity to {_player}
					set the direct entity to {_arrow}
					set the damage location to location(0, 0, 10)
				"""
		),
		LifeCycle.EXPERIMENTAL,
		"damage source[s]"),
	EQUIPPABLE_COMPONENTS("equippable components",
		Documentation.of(
			"Equippable Components",
			"""
				Equippable components allow retrieving and changing the data of an item in the usage as equipment/armor.
				""",
			"2.13",
			"""
				set {_component} to a blank equippable component:
					set the camera overlay to "custom_overlay"
					set the allowed entities to a zombie and a skeleton
					set the equip sound to "block.note_block.pling"
					set the equipped model id to "custom_model"
					set the shear sound to "ui.toast.in"
					set the equipment slot to chest slot
					allow event-equippable component to be damage when hurt
					allow event-equippable component to be dispensed
					allow event-equippable component to be equipped onto entities
					allow event-equippable component to be sheared off
					allow event-equippable component to swap equipment
				set the equippable component of {_item} to {_component}
				"""
		),
		LifeCycle.EXPERIMENTAL, "equippable components");

	private final String codeName;
	private final Documentation documentation;
	private final LifeCycle phase;
	private final SkriptPattern compiledPattern;

	Feature(String codeName, Documentation documentation, LifeCycle phase, String... patterns) {
		this.codeName = codeName;
		this.documentation = documentation;
		this.phase = phase;
		this.compiledPattern = switch (patterns.length) {
			case 0 -> PatternCompiler.compile(codeName);
			case 1 -> PatternCompiler.compile(patterns[0]);
			default -> PatternCompiler.compile('(' + String.join("|", patterns) + ')');
		};
	}

	@ApiStatus.Internal
	public static void registerAll(SkriptAddon addon, ExperimentRegistry manager) {
		for (Feature value : values()) {
			manager.register(addon, value);
		}
	}

	@Override
	public String codeName() {
		return codeName;
	}

	@Override
	public Documentation documentation() {
		return documentation;
	}

	@Override
	public LifeCycle phase() {
		return phase;
	}

	@Override
	public SkriptPattern pattern() {
		return compiledPattern;
	}

	/**
	 * Use {@link Documentation#name()}.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	public @NotNull String displayName() {
		return documentation().name();
	}

}
