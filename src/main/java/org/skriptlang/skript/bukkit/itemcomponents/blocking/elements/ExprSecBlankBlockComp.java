package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExprSecBlankBlockComp extends SectionExpression<BlockingWrapper> implements BlockingExperimentalSyntax {

	private static class BlankBlockingSectionEvent extends Event {

		private final BlockingWrapper wrapper;

		public BlankBlockingSectionEvent(BlockingWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public BlockingWrapper getWrapper() {
			return wrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecBlankBlockComp.class, BlockingWrapper.class, ExpressionType.SIMPLE,
			"a (blank|empty) blocking component");
		EventValues.registerEventValue(BlankBlockingSectionEvent.class, BlockingWrapper.class, BlankBlockingSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			trigger = SectionUtils.loadLinkedCode("blank blocking component", (beforeLoading, afterLoading) ->
				loadCode(node, "blank blocking component", beforeLoading, afterLoading, BlankBlockingSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected BlockingWrapper @Nullable [] get(Event event) {
		BlockingWrapper wrapper = BlockingWrapper.newInstance();
		if (trigger != null) {
			BlankBlockingSectionEvent sectionEvent = new BlankBlockingSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new BlockingWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<BlockingWrapper> getReturnType() {
		return BlockingWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a blank blocking component";
	}

}
