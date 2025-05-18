package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Damage Source")
@Description("Create a custom damage source and change the attributes.")
@Example("""
	set {_source} to a new custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
		set the source location to location(10, 0, 0)
		set the food exhaustion to 10
		make the damage of event-damage source be indirect
		make the damage of event-damage source scale with difficulty
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
		set the food exhaustion of event-damage source to 20
		
		# You can grab a copy of the damage source, but any changes made will not be applied to this death event.
		copy event-damage source to {_source}
		set the food exhaustion of {_source} to 20
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprSecDamageSource extends SectionExpression<DamageSource> implements DamageSourceExperiment {

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
		return true;
	}

	@Override
	protected DamageSource @Nullable [] get(Event event) {
		DamageSource damageSource = new DamageSourceWrapper();
		if (trigger != null) {
			DamageSourceSectionEvent sectionEvent = new DamageSourceSectionEvent(damageSource);
			Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
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
	public String toString(@Nullable Event event, boolean debug) {
		return "a new custom damage source";
	}

}
