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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

@Name("Potion Effects of Entity/Item")
@Description({
	"An expression to obtain the active or hidden potion effects of an entity or item.",
	"When an entity is affected by a potion effect but already has a weaker version of that effect type, the weaker version becomes hidden. " +
			"If the weaker version has a longer duration, it returns after the stronger version expires.",
	"NOTE: Hidden effects are not able to be changed.",
	"NOTE: Clearing the base potion effects of a potion item is not possible. If you wish to do so, just set the item to a water bottle.",
})
@Example("set {_effects::*} to the active potion effects of the player")
@Example("clear the player's hidden potion effects")
@Example("add the potion effects of the player to the potion effects of the player's tool")
@Example("reset the potion effects of the player's tool")
@Example("remove speed and night vision from the potion effects of the player")
@RequiredPlugins("Paper 1.20.4+ for hidden effects")
@Since("2.5.2, 2.14 (active/hidden support, more change modes)")
public class ExprPotionEffects extends PropertyExpression<Object, SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionEffects.class, SkriptPotionEffect.class,
			"[:active|:hidden|both:(active and hidden|hidden and active)] potion effects",
			"livingentities/itemtypes",
			false)
				.supplier(ExprPotionEffects::new)
				.origin(origin)
				.build());
	}

	@ApiStatus.Internal
	public enum State {

		UNSET, ACTIVE, HIDDEN, BOTH;

		static State fromParseTag(String tag) {
			return switch (tag) {
				case "active" -> ACTIVE; // explicitly active
				case "hidden" -> HIDDEN; // explicitly hidden
				case "both" -> BOTH; // explicitly active and hidden
				default -> UNSET; // implicitly active for get, implicitly active and hidden for delete/reset
			};
		}

		boolean includesActive() {
			return this != State.HIDDEN;
		}

		public boolean includesHidden() {
			return this == State.HIDDEN || this == State.BOTH;
		}

		public String displayName() {
			return switch (this) {
				case UNSET -> "";
				case ACTIVE -> "active";
				case HIDDEN -> "hidden";
				case BOTH -> "active and hidden";
			};
		}
	}

	private State state;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
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
		for (Object object : source) {
			if (object instanceof LivingEntity livingEntity) {
				for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
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
				for (PotionEffect potionEffect : PotionUtils.getPotionEffects(itemType)) {
					potionEffects.add(SkriptPotionEffect.fromBukkitEffect(potionEffect, itemType));
				}
			}
		}
		return potionEffects.toArray(new SkriptPotionEffect[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET -> {
				if (state.includesHidden()) {
					Skript.error("The hidden potion effects of an entity cannot be set or added to.");
					yield null;
				}
				yield CollectionUtils.array(PotionEffect[].class);
			}
			case REMOVE -> CollectionUtils.array(SkriptPotionEffect[].class);
			case DELETE, RESET, REMOVE_ALL -> CollectionUtils.array(PotionEffectType[].class);
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] holders = getExpr().getArray(event);
		switch (mode) {
			case SET, DELETE, RESET:
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						reset(entity);
					} else if (holder instanceof ItemType itemType) {
						PotionUtils.clearPotionEffects(itemType);
					}
				}
				if (mode != ChangeMode.SET) { // Fall through for SET to add effects
					break;
				}
				//$FALL-THROUGH$
			case ADD:
				assert delta != null;
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						for (Object object : delta) {
							entity.addPotionEffect((PotionEffect) object);
						}
					} else if (holder instanceof ItemType itemType) {
						for (Object object : delta) {
							PotionUtils.addPotionEffects(itemType, (PotionEffect) object);
						}
					}
				}
				break;
			case REMOVE:
				assert delta != null;
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						for (Object object : delta) {
							remove(entity, (SkriptPotionEffect) object);
						}
					} else if (holder instanceof ItemType itemType) {
						for (Object object : delta) {
							remove(itemType, (SkriptPotionEffect) object);
						}
					}
				}
				break;
			case REMOVE_ALL:
				assert delta != null;
				for (Object holder : holders) {
					if (holder instanceof LivingEntity entity) {
						for (Object object : delta) {
							entity.removePotionEffect((PotionEffectType) object);
						}
					} else if (holder instanceof ItemType itemType) {
						for (Object object : delta) {
							PotionUtils.removePotionEffects(itemType, (PotionEffectType) object);
						}
					}
				}
				break;
			default:
				assert false;
		}
	}

	private void reset(LivingEntity entity) {
		Collection<PotionEffect> potionEffects = entity.getActivePotionEffects();
		if (state == State.ACTIVE) { // preserve hidden effects
			for (PotionEffect potionEffect : potionEffects) {
				Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
				entity.removePotionEffect(potionEffect.getType());
				entity.addPotionEffects(hiddenEffects);
			}
		} else if (state == State.HIDDEN) { // preserve active effect
			for (PotionEffect potionEffect : potionEffects) {
				entity.removePotionEffect(potionEffect.getType());
				// applying a potion effect ignores the hidden effect value
				entity.addPotionEffect(potionEffect);
			}
		} else {
			for (PotionEffect potionEffect : potionEffects) {
				entity.removePotionEffect(potionEffect.getType());
			}
		}
	}

	private void remove(LivingEntity entity, SkriptPotionEffect potionEffect) {
		PotionEffect entityEffect = entity.getPotionEffect(potionEffect.potionEffectType());
		if (entityEffect == null) {
			return;
		}

		Deque<PotionEffect> effects = PotionUtils.getHiddenEffects(entityEffect);
		boolean madeChanges = false;

		// retain (some or all) hidden effects
		// we only remove hidden effects if the user explicitly included them
		if (state.includesHidden()) {
			var effectsIterator = effects.iterator();
			while (effectsIterator.hasNext()) {
				if (potionEffect.matchesQualities(effectsIterator.next())) {
					effectsIterator.remove();
					madeChanges = true;
				}
			}
		}

		// retain the active effect
		// unless the user is only removing hidden effects, we attempt to filter the active effect
		if (state == State.HIDDEN || !potionEffect.matchesQualities(entityEffect)) { // preserve the effect
			effects.addLast(entityEffect);
		} else {
			madeChanges = true;
		}

		if (madeChanges) { // only remove and apply if changes were made
			entity.removePotionEffect(entityEffect.getType());
			entity.addPotionEffects(effects);
		}
	}

	private void remove(ItemType itemType, SkriptPotionEffect potionEffect) {
		for (PotionEffect itemEffect : PotionUtils.getPotionEffects(itemType)) {
			if (potionEffect.matchesQualities(itemEffect)) {
				PotionUtils.removePotionEffects(itemType, potionEffect.potionEffectType());
				break; // API doesn't support multiple effects of the same type
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends SkriptPotionEffect> getReturnType() {
		return SkriptPotionEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + state.displayName() + " potion effects of " + getExpr().toString(event, debug);
	}

	@ApiStatus.Internal
	public State getState() {
		return state;
	}

}
