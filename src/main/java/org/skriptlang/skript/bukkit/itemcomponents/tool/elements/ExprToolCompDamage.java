package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

@Name("Tool Component - Damage Per Block")
@Description("The damage the tool/item receives when it breaks a block.")
@Example("set the damage per block of {_item} to 10")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolCompDamage extends SimplePropertyExpression<ToolWrapper, Integer> implements ToolExperiment {

	static {
		registerDefault(ExprToolCompDamage.class, Integer.class, "damage per block", "toolcomponents");
	}

	@Override
	public @Nullable Integer convert(ToolWrapper wrapper) {
		return wrapper.getComponent().damagePerBlock();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, REMOVE, ADD -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int damage = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (mode == ChangeMode.SET)
			damage = Math2.fit(0, damage, Integer.MAX_VALUE);

		for (ToolWrapper wrapper : getExpr().getArray(event)) {
			int newDamage;
			if (mode == ChangeMode.ADD) {
				newDamage = Math2.fit(0, wrapper.getComponent().damagePerBlock() + damage, Integer.MAX_VALUE);
			} else if (mode == ChangeMode.REMOVE) {
				newDamage = Math2.fit(0, wrapper.getComponent().damagePerBlock() + damage, Integer.MAX_VALUE);
			} else {
				newDamage = damage;
			}
			wrapper.editBuilder(toolBuilder -> toolBuilder.damagePerBlock(newDamage));
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "damage per block";
	}

}
