package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Is Damageable")
@Description("Whether an item can be damaged when the wearer gets injured. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("""
	if {_item} will lose durability when hurt:
		add "Damageable on injury" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} won't lose durability on injury:
		make {_component} lose durability when injured
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompDamage extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		Skript.registerCondition(CondEquipCompDamage.class, ConditionType.PROPERTY,
			"%equippablecomponents% will lose durability (on injury|when (hurt|injured))",
			"%equippablecomponents% (will not|won't) lose durability (on injury|when (hurt|injured))"
		);
	}

	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		setNegated(matchedPattern == 1);
		setExpr(wrappers);
		return true;
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().isDamageOnHurt();
	}

	@Override
	protected String getPropertyName() {
		return "lose durability when injured";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return wrappers.toString(event, debug) + (isNegated() ? " will not " : " will ")
			+ "lose durability when injured";
	}

}
