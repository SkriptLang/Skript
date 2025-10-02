package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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

@Name("Equippable Component - Dispense")
@Description("""
	Whether the item can be dispensed by a dispenser.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("allow {_item} to be dispensed")
@Example("""
	set {_component} to the equippable component of {_item}
	prevent {_component} from being dispensed
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompDispensable extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompDispensable.class)
			.addPatterns(
				"allow %equippablecomponents% to be dispensed",
				"make %equippablecomponents% dispensable",
				"let %equippablecomponents% be dispensed",
				"(block|prevent|disallow) %equippablecomponents% from being dispensed",
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
