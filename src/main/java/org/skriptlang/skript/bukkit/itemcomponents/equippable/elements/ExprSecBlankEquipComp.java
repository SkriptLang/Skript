package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("New Equippable Component")
@Description("Gets a blank equippable component. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("""
	set {_component} to a blank equippable component
	set the equippable component of {_item} to {_component}
	""")
@Example("""
	set {_component} to a blank equippable component:
		set the equipment slot to chest slot
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprSecBlankEquipComp extends SectionExpression<EquippableWrapper> implements EquippableExperiment {

	private static class BlankEquippableSectionEvent extends Event {

		private final EquippableWrapper wrapper;

		public BlankEquippableSectionEvent(EquippableWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public EquippableWrapper getWrapper() {
			return wrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecBlankEquipComp.class, EquippableWrapper.class, ExpressionType.SIMPLE,
			"a (blank|empty) equippable component");
		EventValues.registerEventValue(BlankEquippableSectionEvent.class, EquippableWrapper.class, BlankEquippableSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(node, "blank equippable component", afterLoading, BlankEquippableSectionEvent.class);
			if (isDelayed.get()) {
				Skript.error("Delays cannot be used within a 'blank equippable component' section.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected EquippableWrapper @Nullable [] get(Event event) {
		EquippableWrapper wrapper = EquippableWrapper.newInstance();
		if (trigger != null) {
			BlankEquippableSectionEvent sectionEvent = new BlankEquippableSectionEvent(wrapper);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		}
		return new EquippableWrapper[] {wrapper};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<EquippableWrapper> getReturnType() {
		return EquippableWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a blank equippable component";
	}

}
