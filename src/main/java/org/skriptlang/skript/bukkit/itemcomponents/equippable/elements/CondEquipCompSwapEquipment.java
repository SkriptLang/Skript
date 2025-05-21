package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("Equippable Component - Can Swap Equipment")
@Description({
	"Whether an item can swap equipment by right clicking with it in your hand.",
	"The item will swap places of the set 'equipment slot' of the item. If an equipment slot is not set, defaults to helmet.",
	"Note that equippable component elements are experimental making them subject to change and may not work as intended."
})
@Example("""
	if {_item} can swap equipment:
		add "Swappable" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} can not be equipped when right clicked:
		make {_component} swappable
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompSwapEquipment extends PropertyCondition<EquippableWrapper> implements EquippableExperiment {

	static {
		Skript.registerCondition(CondEquipCompSwapEquipment.class, ConditionType.PROPERTY,
			"%equippablecomponents% can swap equipment [on right click|when right clicked]",
			"%equippablecomponents% (can not|can't) swap equipment [on right click|when right clicked]"
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
		return wrapper.getComponent().isSwappable();
	}

	@Override
	protected String getPropertyName() {
		return "swappable";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(wrappers, "can");
		if (isNegated())
			builder.append("not");
		builder.append("swap equipment");
		return builder.toString();
	}

}
