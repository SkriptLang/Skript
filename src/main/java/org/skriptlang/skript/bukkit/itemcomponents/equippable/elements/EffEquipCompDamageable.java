package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Lose Durability")
@Description("If the item should take damage when the wearer gets injured. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("make {_item} lose durability when hurt")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} will lose durability when injured:
		make {_component} lose durability on injury
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class EffEquipCompDamageable extends Effect implements EquippableExperiment {

	static {
		Skript.registerEffect(EffEquipCompDamageable.class,
			"make %equippablecomponents% lose durability (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured))",
			"(allow|force) %equippablecomponents% to lose durability (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured))",
			"make %equippablecomponents% not lose durability (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured))",
			"(disallow|prevent) %equippablecomponents% from losing durability (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured))"
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean loseDurability;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		loseDurability = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editComponent(component -> component.setDamageOnHurt(loseDurability)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + wrappers.toString(event, debug) + (loseDurability ? " " : " not ")
			+ "lose durability when injured";
	}

}
