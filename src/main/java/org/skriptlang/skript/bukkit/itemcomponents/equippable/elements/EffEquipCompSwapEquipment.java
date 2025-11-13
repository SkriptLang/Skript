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

@Name("Equippable Component - Swap Equipment")
@Description("""
	Whether the item can be swapped by right clicking with it in your hand.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("allow {_item} to swap equipment")
@Example("""
	set {_component} to the equippable component of {_item}
	prevent {_component} from swapping equipment on right click
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompSwapEquipment extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompSwapEquipment.class)
			.addPatterns(
				"(allow|force) %equippablecomponents% to swap equipment [on right click|when right clicked]",
				"(make|let) %equippablecomponents% swap equipment [on right click|when right clicked]",
				"(block|prevent|disallow) %equippablecomponents% from swapping equipment [on right click|when right clicked]",
				"make %equippablecomponents% not swap equipment [on right click|when right clicked]"
			)
			.supplier(EffEquipCompSwapEquipment::new)
			.build()
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		swappable = matchedPattern < 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.swappable(swappable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (swappable)
			return "allow " + wrappers.toString(event, debug) + " to swap equipment";
		return "prevent " + wrappers.toString(event, debug) + " from swapping equipment";
	}
}
