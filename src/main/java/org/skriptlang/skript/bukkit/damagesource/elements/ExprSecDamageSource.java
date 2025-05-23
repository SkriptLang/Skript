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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.MutableDamageSource;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Damage Source")
@Description({
	"Create a custom damage source and change the attributes.",
	"When setting a 'causing entity' you must also set a 'direct entity'.",
	"Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."
})
@Example("""
	set {_source} to a custom damage source:
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
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprSecDamageSource extends SectionExpression<DamageSource> implements SyntaxRuntimeErrorProducer, DamageSourceExperiment {

	public static class DamageSourceSectionEvent extends Event {

		private MutableDamageSource damageSource;

		public DamageSourceSectionEvent(MutableDamageSource damageSource) {
			this.damageSource = damageSource;
		}

		public MutableDamageSource getDamageSource() {
			return damageSource;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecDamageSource.class, DamageSource.class, ExpressionType.SIMPLE,
			"[a] custom damage source [with [the] [damage type] %-damagetype%]");
		EventValues.registerEventValue(DamageSourceSectionEvent.class, DamageSource.class, DamageSourceSectionEvent::getDamageSource);
	}

	private @Nullable Expression<DamageType> damageType;
	private Trigger trigger = null;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			Skript.error("You must contain a section for this expression.");
			return false;
		} else if (node.isEmpty()) {
			Skript.error("You must contain code inside this section.");
			return false;
		}

		AtomicBoolean isDelayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> isDelayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(node, "custom damage source", afterLoading, DamageSourceSectionEvent.class);
		if (isDelayed.get()) {
			Skript.error("Delays cannot be used within a 'custom damage source' section.");
			return false;
		}
		this.node = getParser().getNode();
		if (exprs[0] != null) {
			damageType = LiteralUtils.defendExpression(exprs[0]);
			return LiteralUtils.canInitSafely(damageType);
		}
		return true;
	}

	@Override
	protected DamageSource @Nullable [] get(Event event) {
		MutableDamageSource mutable = new MutableDamageSource();
		if (damageType != null) {
			DamageType damageType = this.damageType.getSingle(event);
			if (damageType != null)
				mutable.setDamageType(damageType);
		}
		DamageSourceSectionEvent sectionEvent = new DamageSourceSectionEvent(mutable);
		Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(trigger, sectionEvent));
		if (mutable.getCausingEntity() != null && mutable.getDirectEntity() == null) {
			error("You must set a 'direct entity' when setting a 'causing entity'.");
			return null;
		}
		return new DamageSource[] {mutable.asBukkitSource()};
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
