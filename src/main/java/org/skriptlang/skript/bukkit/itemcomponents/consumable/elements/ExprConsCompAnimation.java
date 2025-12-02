package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Consumable Component - Animation")
@Description("""
	The animation that plays when the item is being consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_anim} to the consumption animation of {_item}")
@Example("set the consumption animation of {_item} to drink animation")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsCompAnimation extends SimplePropertyExpression<ConsumableWrapper, ItemUseAnimation> implements ConsumableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprConsCompAnimation.class, ItemUseAnimation.class, "consum(e|ption) animation", "consumablecomponents", true)
				.supplier(ExprConsCompAnimation::new)
				.build()
		);
	}

	@Override
	public @Nullable ItemUseAnimation convert(ConsumableWrapper wrapper) {
		return wrapper.getComponent().animation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(ItemUseAnimation.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		ItemUseAnimation animation = (ItemUseAnimation) delta[0];

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> builder.animation(animation)));
	}

	@Override
	public Class<ItemUseAnimation> getReturnType() {
		return ItemUseAnimation.class;
	}

	@Override
	protected String getPropertyName() {
		return "consume animation";
	}

}
