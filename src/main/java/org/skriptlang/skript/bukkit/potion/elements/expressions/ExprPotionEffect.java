package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects.State;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Name("Potion Effect of Entity/Item")
@Description({
	"An expression to obtain a specific potion effect type of an entity or item.",
	"When an entity is affected by a potion effect but already has a weaker version of that effect type, the weaker version becomes hidden. " +
			"If the weaker version has a longer duration, it returns after the stronger version expires.",
	"NOTE: Hidden effects are not able to be changed."
})
@Example("set {_effect} to the player's active speed effect")
@Example("add 10 seconds to the player's slowness effect")
@Example("clear the player's hidden strength effects")
@Example("reset the player's weakness effects")
@Example("delete the player's active jump boost effect")
@RequiredPlugins("Paper 1.20.4+ for hidden effects")
@Since("INSERT VERSION")
public class ExprPotionEffect extends PropertyExpression<Object, SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionEffect.class, SkriptPotionEffect.class,
			"[:active|:hidden|both:(active and hidden|hidden and active)] %potioneffecttypes% [potion] effect[s]",
			"livingentities/itemtypes",
			false)
				.supplier(ExprPotionEffect::new)
				.origin(origin)
				.build());
	}

	private Expression<PotionEffectType> types;
	private State state;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		types = (Expression<PotionEffectType>) expressions[matchedPattern % 2];
		setExpr(expressions[(matchedPattern + 1) % 2]);
		state = State.fromParseTag(parseResult.tags.isEmpty() ? "" : parseResult.tags.getFirst());
		if (state.includesHidden() && ItemType.class.isAssignableFrom(getExpr().getReturnType())) {
			Skript.error("Items (such as potions or stews) do not have hidden effects");
			return false;
		}
		return true;
	}

	@Override
	protected SkriptPotionEffect[] get(Event event, Object[] source) {
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		PotionEffectType[] types = this.types.getArray(event);
		for (Object object : source) {
			if (object instanceof LivingEntity livingEntity) {
				for (PotionEffectType type : types) {
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
				for (PotionEffect effect : PotionUtils.getPotionEffects(itemType)) {
					for (PotionEffectType type : types) {
						if (type.equals(effect.getType())) {
							potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, itemType));
							break;
						}
					}
				}
			}
		}
		return potionEffects.toArray(new SkriptPotionEffect[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, DELETE, RESET -> CollectionUtils.array(Timespan.class);
			case REMOVE -> {
				if (state.includesHidden()) {
					yield CollectionUtils.array(SkriptPotionEffect[].class, Timespan.class);
				}
				yield CollectionUtils.array(Timespan.class);
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] holders = getExpr().getArray(event);
		PotionEffectType[] types = this.types.getArray(event);
		switch (mode) {
			case DELETE, RESET -> {
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						reset(entity, types);
					} else if (holder instanceof ItemType itemType) {
						PotionUtils.removePotionEffects(itemType, types);
					}
				}
			}
			case ADD, REMOVE -> {
				assert delta != null;
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						modify(entity, types, delta, mode);
					} else if (holder instanceof ItemType itemType) {
						if (delta[0] instanceof Timespan change) {
							modify(itemType, types, change, mode);
						}
					}
				}
			}
			default -> {
				assert false;
			}
		}
	}

	private void reset(LivingEntity entity, PotionEffectType[] types) {
		if (state == State.ACTIVE) { // preserve hidden effects
			for (PotionEffectType type : types) {
				PotionEffect potionEffect = entity.getPotionEffect(type);
				if (potionEffect == null) {
					continue;
				}
				Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
				entity.removePotionEffect(type);
				entity.addPotionEffects(hiddenEffects);
			}
		} else if (state == State.HIDDEN) { // preserve active effect
			for (PotionEffectType type : types) {
				PotionEffect original = entity.getPotionEffect(type);
				entity.removePotionEffect(type);
				if (original != null) {
					// applying a potion effect ignores the hidden effect value
					entity.addPotionEffect(original);
				}
			}
		} else {
			for (PotionEffectType type : types) {
				entity.removePotionEffect(type);
			}
		}
	}

	private void modify(LivingEntity entity, PotionEffectType[] types, Object[] delta, ChangeMode mode) {
		for (PotionEffectType type : types) {
			PotionEffect potionEffect = entity.getPotionEffect(type);
			if (potionEffect == null) {
				continue;
			}

			Deque<PotionEffect> finalEffects; // effects to be applied
			Deque<PotionEffect> effects; // effects to be filtered
			boolean madeChanges = false;

			if (state.includesHidden()) { // modify hidden effects
				finalEffects = new ArrayDeque<>();
				effects = PotionUtils.getHiddenEffects(potionEffect);
			} else { // otherwise, simply preserve the hidden effects
				finalEffects = PotionUtils.getHiddenEffects(potionEffect);
				effects = new ArrayDeque<>();
			}

			if (state.includesActive()) { // need to modify the active effect too
				effects.addLast(potionEffect);
			}

			// filter effects
			effectLoop: for (PotionEffect effect : effects) {
				SkriptPotionEffect skriptEffect = SkriptPotionEffect.fromBukkitEffect(effect);
				for (Object object : delta) {
					if (object instanceof Timespan timespan) {
						ExprPotionDuration.changeSafe(skriptEffect, timespan, mode);
						madeChanges = true;
					} else if (object instanceof SkriptPotionEffect base) {
						if (base.matchesQualities(effect)) { // remove this effect
							madeChanges = true;
							continue effectLoop;
						}
					}
				}
				// since we iterate most to least hidden, we need to preserve that order
				finalEffects.addLast(skriptEffect.asBukkitPotionEffect());
			}
			if (!madeChanges) { // no potion effects were modified, don't reapply effects
				return;
			}

			if (!state.includesActive()) { // if we didn't modify the active effect, we need to push it now
				effects.addLast(potionEffect);
			}

			entity.removePotionEffect(type);
			entity.addPotionEffects(finalEffects);
		}
	}

	private void modify(ItemType itemType, PotionEffectType[] types, Timespan change, ChangeMode mode) {
		for (PotionEffect effect : PotionUtils.getPotionEffects(itemType)) {
			for (PotionEffectType type : types) {
				if (type.equals(effect.getType())) {
					// use SkriptPotionEffect source system to handle removal and application
					ExprPotionDuration.changeSafe(SkriptPotionEffect.fromBukkitEffect(effect, itemType), change, mode);
					break;
				}
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
		// avoid double spaces from blank display name
		builder.append(("the " + state.displayName()).stripTrailing(), types);
		if (isSingle()) {
			builder.append("effect");
		} else {
			builder.append("effects");
		}
		builder.append("of", getExpr());
		return builder.toString();
	}

	@ApiStatus.Internal
	public State getState() {
		return state;
	}

}
