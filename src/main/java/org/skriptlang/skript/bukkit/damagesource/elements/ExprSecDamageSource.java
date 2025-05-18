package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Damage Source")
@Description("Create a custom damage source and change the attributes. "
	+ "When setting a 'causing entity' you must also set a 'direct entity'.")
@Example("""
	set {_source} to a new custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	damage all players by 5 using {_source}
	""")
@Example("""
	on damage:
		if the damage type of event-damage source is magic:
			set the damage to damage * 2
	""")
@Example("""
	on death:
		# This will error because you cannot change any attributes of a finalized damage source
		set the damage type of event-damage source to player attack
		
		# You can grab a copy of the damage source, but any changes made will not be applied to this death event.
		copy event-damage source to {_source}
		set the damage type of {_source} to player attack
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprSecDamageSource extends SectionExpression<DamageSource> implements SyntaxRuntimeErrorProducer, DamageSourceExperiment {

	public static class DamageSourceSectionEvent extends Event {

		private DamageSource damageSource;

		public DamageSourceSectionEvent(DamageSource damageSource) {
			this.damageSource = damageSource;
		}

		public DamageSource getDamageSource() {
			return damageSource;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecDamageSource.class, DamageSource.class, ExpressionType.SIMPLE,
			"[a] [new] custom damage source");
		EventValues.registerEventValue(DamageSourceSectionEvent.class, DamageSource.class, DamageSourceSectionEvent::getDamageSource);
	}

	private Trigger trigger = null;
	private Node node;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node != null) {
			AtomicBoolean isDelayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(node, "custom damage source", afterLoading, DamageSourceSectionEvent.class);
			if (isDelayed.get()) {
				Skript.error("Delays cannot be used within a 'custom damage source' section.");
				return false;
			}
		}
		this.node = getParser().getNode();
		return true;
	}

	@Override
	protected DamageSource @Nullable [] get(Event event) {
		DamageSource damageSource = new DamageSourceWrapper();
		if (trigger != null) {
			DamageSourceSectionEvent sectionEvent = new DamageSourceSectionEvent(damageSource);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
			if (damageSource.getCausingEntity() != null && damageSource.getDirectEntity() == null) {
				error("You must set a 'direct entity' when setting a 'causing entity'.");
				return null;
			}
		}
		return new DamageSource[] {damageSource};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<DamageSource> getReturnType() {
		return DamageSource.class;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new custom damage source";
	}

}
