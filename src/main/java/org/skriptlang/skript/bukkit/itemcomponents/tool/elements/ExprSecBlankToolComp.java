package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Blank Tool Component")
@Description("Gets a blank tool component.")
@Examples({
	"set {_component} to a new blank tool component",
	"set the tool component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprSecBlankToolComp extends SectionExpression<ToolWrapper> implements ToolExperiment {

	public static class BlankToolSectionEvent extends Event {

		private final ToolWrapper wrapper;

		public BlankToolSectionEvent(ToolWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public ToolWrapper getWrapper() {
			return wrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecBlankToolComp.class, ToolWrapper.class, ExpressionType.SIMPLE,
			"a (blank|empty) tool component");
		EventValues.registerEventValue(BlankToolSectionEvent.class, ToolWrapper.class, BlankToolSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(node, "blank tool component", afterLoading, BlankToolSectionEvent.class);
			if (isDelayed.get()) {
				Skript.error("Delays cannot be used within a 'blank tool component' section.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected ToolWrapper @Nullable [] get(Event event) {
		ToolWrapper wrapper = ToolWrapper.newInstance();
		if (trigger != null) {
			BlankToolSectionEvent sectionEvent = new BlankToolSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new ToolWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ToolWrapper> getReturnType() {
		return ToolWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a blank tool component";
	}

}
