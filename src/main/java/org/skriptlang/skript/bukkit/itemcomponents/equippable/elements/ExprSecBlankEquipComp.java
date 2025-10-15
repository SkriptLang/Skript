package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

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
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Blank Equippable Component")
@Description("""
	Gets a blank equippable component.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_component} to a blank equippable component:
		set the camera overlay to "custom_overlay"
		set the allowed entities to a zombie and a skeleton
		set the equip sound to "block.note_block.pling"
		set the equipped model id to "custom_model"
		set the shear sound to "ui.toast.in"
		set the equipment slot to chest slot
		allow event-equippable component to be damage when hurt
		allow event-equippable component to be dispensed
		allow event-equippable component to be equipped onto entities
		allow event-equippable component to be sheared off
		allow event-equippable component to swap equipment
	set the equippable component of {_item} to {_component}
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class ExprSecBlankEquipComp extends SectionExpression<EquippableWrapper> implements EquippableExperimentSyntax {

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

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecBlankEquipComp.class, EquippableWrapper.class)
			.addPatterns("a (blank|empty) equippable component")
			.supplier(ExprSecBlankEquipComp::new)
			.build()
		);
		EventValues.registerEventValue(BlankEquippableSectionEvent.class, EquippableWrapper.class, BlankEquippableSectionEvent::getWrapper);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("blank equippable component", (beforeLoading, afterLoading) ->
				loadCode(node, "blank equippable component", beforeLoading, afterLoading, BlankEquippableSectionEvent.class)
			);
			return trigger != null;
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
