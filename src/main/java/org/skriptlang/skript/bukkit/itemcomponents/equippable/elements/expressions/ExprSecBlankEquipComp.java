package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.expressions;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
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

@Name("Bare Equippable Component")
@Description("""
    Procureth a bare equippable component, unsullied and void of properties.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("""
    set {_component} to a bare equippable component:
    	set the looking-glass overlay to "custom_overlay"
    	set the permitted entities to a zombie and a skeleton
    	set the donning sound to "block.note_block.pling"
    	set the donned visage id to "custom_model"
    	set the shearing sound to "ui.toast.in"
    	set the armament slot to chest slot
    	grant event-equippable component to suffer damage when hurt
    	grant event-equippable component to be dispensed forth
    	grant event-equippable component to be donned upon entities
    	grant event-equippable component to be shorn from
    	grant event-equippable component to exchange armament
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
			.addPatterns("a (bare|empty) equippable component")
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
