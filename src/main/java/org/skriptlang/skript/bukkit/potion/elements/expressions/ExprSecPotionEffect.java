package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("New Potion Effect")
@Description("Create a new potion effect that can be applied to an entity or item type.")
@Example("""
	set {_potion} to a potion effect of speed 2 for 10 minutes:
		hide the effect's icon
		hide the effect's particles
	""")
@Example("add strength 5 to the potion effects of the player's tool")
@Example("""
	apply invisibility to the player for 5 minutes:
		hide the effect's particles
	""")
@Example("add a potion effect of speed 1 to the potion effects of the player")
@Example("""
	# creates a potion effect with the properties of an existing potion effect
	set {_potion} to a potion effect of slowness based on the player's speed effect
	""")
@Since({"2.5.2", "2.14 (syntax changes, infinite duration support)"})
public class ExprSecPotionEffect extends SectionExpression<SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecPotionEffect.class, SkriptPotionEffect.class)
				.supplier(ExprSecPotionEffect::new)
				.origin(origin)
				.addPatterns(
						"[a[n]] [:ambient] potion effect of %potioneffecttype% [[of tier] %-number%] [for %-timespan%]",
						"[an] (infinite|permanent) [:ambient] potion effect of %potioneffecttype% [[of tier] %-number%] ",
						"[an] (infinite|permanent) [:ambient] %potioneffecttype% [[of tier] %-number%] [potion [effect]] ",
						"[a] potion effect [of %-potioneffecttype%] (from|using|based on) %potioneffect%"
				)
				.build());
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecPotionEffect.class, SkriptPotionEffect.class)
				.supplier(ExprSecPotionEffect::new)
				.origin(origin)
				.priority(SyntaxInfo.PATTERN_MATCHES_EVERYTHING)
				.addPatterns(
						"%*potioneffecttype% %*number%"
				)
				.build());
		EventValues.registerEventValue(PotionEffectSectionEvent.class, SkriptPotionEffect.class, event -> event.effect);
	}

	static class PotionEffectSectionEvent extends Event {
		public SkriptPotionEffect effect;
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}
	}

	private @Nullable Expression<PotionEffectType> potionEffectType;
	private @Nullable Expression<Number> amplifier;
	private @Nullable Expression<Timespan> duration;
	private boolean ambient;
	private boolean infinite;

	private @Nullable Expression<PotionEffect> source;

	private @Nullable Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delayed, ParseResult parseResult,
						@Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		potionEffectType = (Expression<PotionEffectType>) expressions[0];
		if (matchedPattern == 3) {
			source = (Expression<PotionEffect>) expressions[1];
		} else {
			amplifier = (Expression<Number>) expressions[1];
			infinite = matchedPattern != 0;
			if (expressions.length == 3) {
				duration = (Expression<Timespan>) expressions[2];
			}
			ambient = parseResult.hasTag("ambient");
		}

		if (sectionNode != null) {
			trigger = SectionUtils.loadLinkedCode("create potion effect", (beforeLoading, afterLoading)
					-> loadCode(sectionNode, "create potion effect", beforeLoading, afterLoading, PotionEffectSectionEvent.class));
			return trigger != null;
		}

		return true;
	}

	@Override
	protected SkriptPotionEffect[] get(Event event) {
		SkriptPotionEffect potionEffect = null;
		if (source != null) {
			PotionEffect source = this.source.getSingle(event);
			if (source == null) {
				return new SkriptPotionEffect[0];
			}
			potionEffect = SkriptPotionEffect.fromBukkitEffect(source);
		}

		PotionEffectType potionEffectType = null;
		if (this.potionEffectType != null) {
			potionEffectType = this.potionEffectType.getSingle(event);
			if (potionEffectType == null) {
				return new SkriptPotionEffect[0];
			}
		}

		if (potionEffect == null) {
			potionEffect = SkriptPotionEffect.fromType(potionEffectType);
		} else {
			potionEffect.potionEffectType(potionEffectType);
		}

		if (ambient) {
			potionEffect.ambient(true);
		}

		if (this.amplifier != null) {
			Number amplifierNumber = this.amplifier.getSingle(event);
			if (amplifierNumber != null) {
				potionEffect.amplifier(amplifierNumber.intValue() - 1);
			}
		}

		if (this.duration != null) {
			Timespan timespan = this.duration.getSingle(event);
			if (timespan != null) {
				if (timespan.isInfinite()) {
					potionEffect.infinite(true);
				} else {
					potionEffect.duration((int) Math2.fit(0, timespan.getAs(TimePeriod.TICK), Integer.MAX_VALUE));
				}
			}
		} else if (infinite) { // We do not want to call this when 'infinite' is false as the duration would be overridden
			potionEffect.infinite(true);
		}

		if (trigger != null) {
			PotionEffectSectionEvent potionEvent = new PotionEffectSectionEvent();
			potionEvent.effect = potionEffect;
			Variables.withLocalVariables(event, potionEvent, () -> TriggerItem.walk(trigger, potionEvent));
		}

		return new SkriptPotionEffect[]{potionEffect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends SkriptPotionEffect> getReturnType() {
		return SkriptPotionEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (ambient) {
			builder.append("ambient");
		}
		if (infinite) {
			builder.append("infinite");
		}
		builder.append("potion effect");
		if (potionEffectType != null) {
			builder.append("of", potionEffectType);
		}
		if (amplifier != null) {
			builder.append("of tier", amplifier);
		}
		if (source != null) {
			builder.append("based on", source);
		} else if (!infinite) {
			builder.append("for");
			if (duration == null) {
				builder.append(PotionUtils.DEFAULT_DURATION_STRING);
			} else {
				builder.append(duration);
			}
		}
		return builder.toString();
	}

}
