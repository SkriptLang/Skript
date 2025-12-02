package org.skriptlang.skript.bukkit.itemcomponents.food.elements;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.food.FoodWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Blank Food Component")
@Description("""
	Gets a blank food component.
	NOTE: Food component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to a blank food component:
		set the nutritional value to 30
		set the saturation value to 20
		allow event-food component to always be eaten
	set the food component of {_item} to {_component}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprSecBlankFoodComp extends SectionExpression<FoodWrapper> implements FoodExperimentalSyntax {

	private static class BlankFoodSectionEvent extends Event {

		private final FoodWrapper wrapper;

		public BlankFoodSectionEvent(FoodWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public FoodWrapper getWrapper() {
			return wrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprSecBlankFoodComp.class, FoodWrapper.class)
				.addPatterns("[a] (blank|empty) food component")
				.supplier(ExprSecBlankFoodComp::new)
				.build()
		);
		EventValues.registerEventValue(BlankFoodSectionEvent.class, FoodWrapper.class, BlankFoodSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("blank food component", (beforeLoading, afterLoading) ->
				loadCode(node, "blank food component", beforeLoading, afterLoading, BlankFoodSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected FoodWrapper @Nullable [] get(Event event) {
		FoodWrapper wrapper = FoodWrapper.newInstance();
		if (trigger != null) {
			BlankFoodSectionEvent sectionEvent = new BlankFoodSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new FoodWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends FoodWrapper> getReturnType() {
		return FoodWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a blank food component";
	}

}
