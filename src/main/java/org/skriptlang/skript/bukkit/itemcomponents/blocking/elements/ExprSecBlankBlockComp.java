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
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Blank Blocking Component")
@Description("""
	Gets a blank blocking component.
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to a blank blocking component:
		set the blocked sound to ""
		set the disabled sound to ""
		set the damage type bypass to magic
		set the blocking delay time to 1 second
		set the disabled cooldown scale to 0.2
		add (a blank damage reduction) to the damage reductions
	set the blocking component of {_item} to {_component}
	""")
@Example("clear the blocking component of {_item}")
@Example("reset the blocking component of {_item}")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprSecBlankBlockComp extends SectionExpression<BlockingWrapper> implements BlockingExperimentalSyntax {

	private static class BlockCompSectionEvent extends Event {

		private final BlockingWrapper wrapper;

		public BlockCompSectionEvent(BlockingWrapper wrapper) {
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

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprSecBlankBlockComp.class, BlockingWrapper.class)
				.addPatterns("a (blank|empty) blocking component")
				.supplier(ExprSecBlankBlockComp::new)
				.build()
		);
		EventValues.registerEventValue(BlockCompSectionEvent.class, BlockingWrapper.class, BlockCompSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			trigger = SectionUtils.loadLinkedCode("blank blocking component", (beforeLoading, afterLoading) ->
				loadCode(node, "blank blocking component", beforeLoading, afterLoading, BlockCompSectionEvent.class)
			);
			return trigger != null;
		}
		return true;
	}

	@Override
	protected BlockingWrapper @Nullable [] get(Event event) {
		BlockingWrapper wrapper = BlockingWrapper.newInstance();
		if (trigger != null) {
			BlockCompSectionEvent sectionEvent = new BlockCompSectionEvent(wrapper);
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
