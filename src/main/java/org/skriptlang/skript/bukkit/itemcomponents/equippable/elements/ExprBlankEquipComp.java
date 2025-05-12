package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

@Name("New Equippable Component")
@Description("Gets a blank equippable component. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Examples({
	"set {_component} to a blank equippable component",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprBlankEquipComp extends SimpleExpression<EquippableWrapper> implements EquippableExperiment {

	static {
		Skript.registerExpression(ExprBlankEquipComp.class, EquippableWrapper.class, ExpressionType.SIMPLE,
			"a (blank|empty) equippable component");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected EquippableWrapper @Nullable [] get(Event event) {
		return new EquippableWrapper[] {EquippableWrapper.newComponent()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<EquippableWrapper> getReturnType() {
		return EquippableWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new equippable component";
	}

}
