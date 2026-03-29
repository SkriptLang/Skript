package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Dispense Forth")
@Description("""
    Whether the item may be dispensed forth by a dispenser.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("grant {_item} to be dispensed forth")
@Example("""
    set {_component} to the equippable component of {_item}
    forbid {_component} from being dispensed forth
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompDispensable extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompDispensable.class)
			.addPatterns(
				"grant %equippablecomponents% to be dispensed forth",
				"make %equippablecomponents% dispensable",
				"let %equippablecomponents% be dispensed forth",
				"(forbid|prevent|deny) %equippablecomponents% from being dispensed forth",
				"make %equippablecomponents% not dispensable"
			)
			.supplier(EffEquipCompDispensable::new)
			.build()
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean dispensable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		dispensable = matchedPattern < 3;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.dispensable(dispensable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (dispensable)
			return "allow " + wrappers.toString(event, debug) + " to be dispensed";
		return "prevent " + wrappers.toString(event, debug) + " from being dispensed";
	}

}
