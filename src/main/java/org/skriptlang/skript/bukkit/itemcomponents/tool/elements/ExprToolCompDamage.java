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
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Tool Component - Damage Per Block")
@Description("""
	The damage the item receives when it breaks a block.
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the damage per block of {_item} to 10")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprToolCompDamage extends SimplePropertyExpression<ToolWrapper, Integer> implements ToolExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprToolCompDamage.class, Integer.class, "damage per block", "toolcomponents", true)
				.supplier(ExprToolCompDamage::new)
				.build()
		);
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
				newDamage = Math2.fitOverflowMax(0, wrapper.getComponent().damagePerBlock(), damage);
			} else if (mode == ChangeMode.REMOVE) {
				newDamage = Math2.fitOverflowMax(0, wrapper.getComponent().damagePerBlock(), -damage);
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
