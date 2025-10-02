package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageReductionWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Custom Damage Reduction")
@Description("""
	Gets a custom damage reduction.
	Damage Reductions contain data that attribute to:
		- What damage types can be being blocked
		- The base amount of damage to block when blocking one of the damage types
		- The factor amount of damage to block when blocking one of the damage types
		- The angle at which the item can block when blocking one of the damage types
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_reduction} to a custom damage reduction:
		set the reduction angle to 90
		set the reduction base to 40
		set the reduction factor to 1
		set the reduction damage types to magic and explosion
	add {_reduction} to the damage reductions of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprSecReduction extends SectionExpression<DamageReductionWrapper> implements BlockingExperimentalSyntax {

	private static class BlankReductionSectionEvent extends Event {

		private final DamageReductionWrapper wrapper;

		public BlankReductionSectionEvent(DamageReductionWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public DamageReductionWrapper getWrapper() {
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
			SyntaxInfo.Expression.builder(ExprSecReduction.class, DamageReductionWrapper.class)
				.addPatterns("a [custom] damage reduction")
				.supplier(ExprSecReduction::new)
				.build()
		);
		EventValues.registerEventValue(BlankReductionSectionEvent.class, DamageReductionWrapper.class, BlankReductionSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			trigger = SectionUtils.loadLinkedCode("custom damage reduction", (beforeLoading, afterLoading) ->
				loadCode(node, "custom damage reduction", beforeLoading, afterLoading, BlankReductionSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected DamageReductionWrapper @Nullable [] get(Event event) {
		DamageReductionWrapper wrapper = new DamageReductionWrapper();
		if (trigger != null) {
			BlankReductionSectionEvent sectionEvent = new BlankReductionSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new DamageReductionWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<DamageReductionWrapper> getReturnType() {
		return DamageReductionWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a custom damage reduction";
	}

}
