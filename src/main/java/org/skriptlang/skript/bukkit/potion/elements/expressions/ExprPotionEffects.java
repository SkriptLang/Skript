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
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

@Name("Potion Effects of Entity/Item")
@Description({
	"An expression to obtain the active or hidden potion effects of an entity or item.",
	"When an entity is affected by a potion effect but already has a weaker version of that effect type, the weaker version becomes hidden." +
			" If the weaker version has a longer duration, it returns after the stronger version expires.",
	"NOTE: Hidden effects are not able to be changed.",
	"NOTE: Clearing the base potion effects of a potion item is not possible. If you wish to do so, just set the item to a water bottle.",
})
@Example("set {_effects::*} to the active potion effects of the player")
@Example("clear the player's hidden potion effects")
@Example("add the potion effects of the player to the potion effects of the player's tool")
@Example("reset the potion effects of the player's tool")
@Example("remove speed and night vision from the potion effects of the player")
@RequiredPlugins("Paper 1.20.4+ for hidden effects")
@Since("2.5.2, INSERT VERSION (active/hidden support, more change modes)")
public class ExprPotionEffects extends PropertyExpression<Object, SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		register(registry, ExprPotionEffects.class, SkriptPotionEffect.class,
				"[:active|:hidden|both:active and hidden|both:hidden and active] potion effects",
				"livingentities/itemtypes");
	}

	enum State {

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

		boolean includesHidden() {
			return this == State.HIDDEN || this == State.BOTH;
		}

	}

	private State state;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		state = State.fromParseTag(parseResult.tags.isEmpty() ? "" : parseResult.tags.get(0));
		if (!PotionUtils.HAS_HIDDEN_EFFECTS && state.includesHidden()) {
			Skript.error("Usage of hidden effects requires Paper 1.20.4 or newer");
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
			case ADD, SET, REMOVE -> {
				if (state.includesHidden()) {
					Skript.error("The hidden potion effects of an entity can only be deleted, reset, or have all effects of a specific type removed (remove all)");
					yield null;
				}
				if (mode == ChangeMode.REMOVE) {
					yield CollectionUtils.array(PotionEffect[].class, PotionEffectType[].class);
				}
				yield CollectionUtils.array(PotionEffect[].class);
			}
			case DELETE, RESET -> CollectionUtils.array(PotionEffect[].class);
			case REMOVE_ALL -> CollectionUtils.array(PotionEffectType[].class);
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] holders = getExpr().getArray(event);
		switch (mode) {
			case SET, DELETE, RESET:
				for (Object holder : holders) {
					if (holder instanceof LivingEntity livingEntity) {
						Collection<PotionEffect> potionEffects = livingEntity.getActivePotionEffects();
						if (PotionUtils.HAS_HIDDEN_EFFECTS && state == State.ACTIVE) {
							for (PotionEffect potionEffect : potionEffects) {
								// build hidden effects chain to reapply
								PotionEffect hiddenEffect = potionEffect.getHiddenPotionEffect();
								Deque<PotionEffect> hiddenEffects = new ArrayDeque<>();
								while (hiddenEffect != null) {
									hiddenEffects.push(hiddenEffect);
									hiddenEffect = hiddenEffect.getHiddenPotionEffect();
								}
								livingEntity.removePotionEffect(potionEffect.getType());
								livingEntity.addPotionEffects(hiddenEffects);
							}
						} else if (state == State.HIDDEN) {
							for (PotionEffect potionEffect : potionEffects) {
								livingEntity.removePotionEffect(potionEffect.getType());
								// reapply active effect
								livingEntity.addPotionEffect(potionEffect);
							}
						} else {
							for (PotionEffect potionEffect : potionEffects) {
								livingEntity.removePotionEffect(potionEffect.getType());
							}
						}
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
					if (holder instanceof LivingEntity livingEntity) {
						for (Object object : delta) {
							livingEntity.addPotionEffect((PotionEffect) object);
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
					if (holder instanceof LivingEntity livingEntity) {
						for (Object object : delta) {
							if (object instanceof PotionEffect potionEffect) {
								livingEntity.removePotionEffect(potionEffect.getType());
							} else if (object instanceof PotionEffectType potionEffectType) {
								livingEntity.removePotionEffect(potionEffectType);
							}
						}
					} else if (holder instanceof ItemType itemType) {
						for (Object object : delta) {
							if (object instanceof PotionEffect potionEffect) {
								PotionUtils.removePotionEffects(itemType, potionEffect.getType());
							} else if (object instanceof PotionEffectType potionEffectType) {
								PotionUtils.removePotionEffects(itemType, potionEffectType);
							}
						}
					}
				}
				break;
			case REMOVE_ALL:
				assert delta != null;
				for (Object holder : holders) {
					if (holder instanceof LivingEntity livingEntity) {
						for (Object object : delta) {
							livingEntity.removePotionEffect((PotionEffectType) object);
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
		builder.append("potion effects of", getExpr());
		return builder.toString();
	}

}
