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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageFunctionWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Custom Item Damage Function")
@Description("""
	Gets a custom item damage function.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_damageFunction} to a custom item damage function:
		set the damage function base to 10
		set the damage function factor to 0.3
		set the damage function threshold to 50
	set the item damage function of {_item} to {_damageFunction}
	""")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprSecDamageFunction extends SectionExpression<DamageFunctionWrapper> implements BlockingExperimentalSyntax {

	private static class DamageFunctionSectionEvent extends Event {

		private final DamageFunctionWrapper wrapper;

		public DamageFunctionSectionEvent(DamageFunctionWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public DamageFunctionWrapper getWrapper() {
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
			SyntaxInfo.Expression.builder(ExprSecDamageFunction.class, DamageFunctionWrapper.class)
				.addPatterns("a [custom] [item] damage function")
				.supplier(ExprSecDamageFunction::new)
				.build()
		);
		EventValues.registerEventValue(DamageFunctionSectionEvent.class, DamageFunctionWrapper.class, DamageFunctionSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			trigger = SectionUtils.loadLinkedCode("custom item damage function", (beforeLoading, afterLoading) ->
				loadCode(node, "custom item damage function", beforeLoading, afterLoading, DamageFunctionSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected DamageFunctionWrapper @Nullable [] get(Event event) {
		DamageFunctionWrapper wrapper = new DamageFunctionWrapper();
		if (trigger != null) {
			DamageFunctionSectionEvent sectionEvent = new DamageFunctionSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new DamageFunctionWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<DamageFunctionWrapper> getReturnType() {
		return DamageFunctionWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a custom item damage function";
	}

}
