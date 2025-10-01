package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Blank Consumable Component")
@Description("""
	Gets a blank consumable component.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to a blank consumable component:
		set the consumption animation to drink animation
		add (a consume effect to clear all potion effects) to the consume effects
		set the consumption time to 5 seconds
		set the consumption sound to "ui.toast.out"
		enable the consumption particles for event-consumable component
	set the consumable component of {_item} to {_component}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
public class ExprSecBlankConsComp extends SectionExpression<ConsumableWrapper> implements ConsumableExperimentSyntax {

	private static class BlankConsumableSectionEvent extends Event {

		private final ConsumableWrapper wrapper;

		public BlankConsumableSectionEvent(ConsumableWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public ConsumableWrapper getWrapper() {
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
			SyntaxInfo.Expression.builder(ExprSecBlankConsComp.class, ConsumableWrapper.class)
				.addPatterns("a (blank|empty) consumable component")
				.supplier(ExprSecBlankConsComp::new)
				.build()
		);
		EventValues.registerEventValue(BlankConsumableSectionEvent.class, ConsumableWrapper.class, BlankConsumableSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			trigger = SectionUtils.loadLinkedCode("blank consumable component", (beforeLoading, afterLoading) ->
				loadCode(node, "blank consumable component", beforeLoading, afterLoading, BlankConsumableSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected ConsumableWrapper @Nullable [] get(Event event) {
		ConsumableWrapper wrapper = ConsumableWrapper.newInstance();
		if (trigger != null) {
			BlankConsumableSectionEvent sectionEvent = new BlankConsumableSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new ConsumableWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ConsumableWrapper> getReturnType() {
		return ConsumableWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a blank consumable component";
	}

}
