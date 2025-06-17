package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects.State;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

@Name("Potion Effect of Entity/Item")
@Description({
	"An expression to obtain a specific potion effect type of an entity or item.",
	"When an entity is affected by a potion effect but already has a weaker version of that effect type, the weaker version becomes hidden." +
			" If the weaker version has a longer duration, it returns after the stronger version expires.",
	"NOTE: Hidden effects are not able to be changed."
})
@Example("set {_effect} to the player's active speed effect")
@Example("add 10 seconds to the player's slowness effect")
@Example("clear the player's hidden strength effects")
@Example("reset the player's weakness effects")
@Example("delete the player's active jump boost effect")
@Since("INSERT VERSION")
public class ExprPotionEffect extends PropertyExpression<Object, SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprPotionEffect.class, SkriptPotionEffect.class,
				"[:active|:hidden|both:active and hidden|both:hidden and active] %potioneffecttypes% [potion] effect[s]",
				"livingentities/itemtypes");
	}

	private Expression<PotionEffectType> types;
	private State state;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		types = (Expression<PotionEffectType>) expressions[matchedPattern % 2];
		setExpr(expressions[(matchedPattern + 1) % 2]);
		state = State.fromParseTag(parseResult.tags.isEmpty() ? "" : parseResult.tags.get(0));
		return true;
	}

	@Override
	protected SkriptPotionEffect[] get(Event event, Object[] source) {
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (Object object : source) {
			if (object instanceof LivingEntity livingEntity) {
				for (PotionEffectType type : types.getArray(event)) {
					PotionEffect potionEffect = livingEntity.getPotionEffect(type);
					if (potionEffect == null) {
						continue;
					}
					if (state.includesActive()) {
						potionEffects.add(SkriptPotionEffect.fromBukkitEffect(potionEffect, livingEntity));
					}
					if (state.includesHidden()) {
						PotionEffect hiddenEffect = potionEffect.getHiddenPotionEffect();
						while (hiddenEffect != null) {
							// do not set source for hidden effects
							potionEffects.add(SkriptPotionEffect.fromBukkitEffect(hiddenEffect));
							hiddenEffect = hiddenEffect.getHiddenPotionEffect();
						}
					}
				}
			} else if (object instanceof ItemType itemType) {
				Set<PotionEffectType> types = Set.of(this.types.getArray(event));
				for (PotionEffect effect : PotionUtils.getPotionEffects(itemType)) {
					if (types.contains(effect.getType())) {
						potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, itemType));
					}
				}
			}
		}
		return potionEffects.toArray(new SkriptPotionEffect[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE -> {
				if (state.includesHidden()) {
					Skript.error("The hidden potion effects of an entity can only be deleted or reset");
					yield null;
				}
				yield CollectionUtils.array(Timespan.class);
			}
			case DELETE, RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] holders = getExpr().getArray(event);
		switch (mode) {
			case DELETE, RESET -> {
				PotionEffectType[] types = this.types.getArray(event);
				for (Object holder : holders) {
					if (holder instanceof LivingEntity livingEntity) {
						if (state == State.ACTIVE) {
							for (PotionEffectType type : types) {
								PotionEffect potionEffect = livingEntity.getPotionEffect(type);
								if (potionEffect == null) {
									continue;
								}
								// build hidden effects chain to reapply
								PotionEffect hiddenEffect = potionEffect.getHiddenPotionEffect();
								Deque<PotionEffect> hiddenEffects = new ArrayDeque<>();
								while (hiddenEffect != null) {
									hiddenEffects.push(hiddenEffect);
									hiddenEffect = hiddenEffect.getHiddenPotionEffect();
								}
								livingEntity.removePotionEffect(type);
								livingEntity.addPotionEffects(hiddenEffects);
							}
						} else if (state == State.HIDDEN) {
							for (PotionEffectType type : types) {
								PotionEffect original = livingEntity.getPotionEffect(type);
								livingEntity.removePotionEffect(type);
								if (original != null) { // reapply active effect
									livingEntity.addPotionEffect(original);
								}
							}
						} else {
							for (PotionEffectType type : types) {
								livingEntity.removePotionEffect(type);
							}
						}
					} else if (holder instanceof ItemType itemType) {
						PotionUtils.removePotionEffects(itemType, types);
					}
				}
			}
			case ADD, REMOVE -> {
				assert delta != null;
				Timespan change = (Timespan) delta[0];
				SkriptPotionEffect[] potionEffects = get(event, holders);
				for (SkriptPotionEffect potionEffect : potionEffects) {
					ExprPotionDuration.changeSafe(potionEffect, change, mode);
				}
			}
			default -> {
				assert false;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return types.isSingle() && !state.includesHidden();
	}

	@Override
	public Class<? extends SkriptPotionEffect> getReturnType() {
		return SkriptPotionEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (state.includesActive()) {
			builder.append("active");
			if (state.includesHidden()) {
				builder.append("and");
			}
		}
		if (state.includesHidden()) {
			builder.append("hidden");
		}
		builder.append(types, "effect", (isSingle() ? "" : "s"), "of", getExpr());
		return builder.toString();
	}

}
