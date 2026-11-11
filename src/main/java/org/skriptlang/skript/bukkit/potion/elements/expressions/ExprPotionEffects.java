package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
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
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider.RetrievalState;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
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

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionEffects.class, SkriptPotionEffect.class,
			"[:active|:hidden|both:(active and hidden|hidden and active)] potion effects",
			"entities/itemtypes",
			false)
				.supplier(ExprPotionEffects::new)
				.build());
	}

	private RetrievalState state;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		state = RetrievalState.fromParseTag(parseResult.tags.isEmpty() ? "" : parseResult.tags.getFirst());
		if (state.includesHidden() && !getExpr().canReturn(Entity.class)) {
			Skript.error("Only living entities have hidden effects");
			return false;
		}
		return true;
	}

	@Override
	protected SkriptPotionEffect[] get(Event event, Object[] source) {
		List<SkriptPotionEffect> potionEffects = new ArrayList<>();
		for (Object object : source) {
			potionEffects.addAll(PotionEffectProvider.of(object, this::error)
				.getAll(state));
		}
		return potionEffects.toArray(new SkriptPotionEffect[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET -> {
				if (state.includesHidden()) {
					Skript.error("Hidden potion effects cannot be directly set or added to.");
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
			case ADD -> {
				assert delta != null;
				for (Object holder : holders) {
					PotionEffectProvider<?> provider = PotionEffectProvider.of(holder, this::error);
					for (Object object : delta) {
						provider.add((PotionEffect) object);
					}
				}
			}
			case SET -> {
				assert delta != null;
				for (Object holder : holders) {
					PotionEffectProvider<?> provider = PotionEffectProvider.of(holder, this::error);
					provider.clearAll(state);
					for (Object object : delta) {
						provider.add((PotionEffect) object);
					}
				}
			}
			case REMOVE -> {
				assert delta != null;
				for (Object holder : holders) {
					PotionEffectProvider<?> provider = PotionEffectProvider.of(holder, this::error);
					for (Object object : delta) {
						provider.remove((SkriptPotionEffect) object, state);
					}
				}
			}
			case REMOVE_ALL -> {
				assert delta != null;
				for (Object holder : holders) {
					PotionEffectProvider<?> provider = PotionEffectProvider.of(holder, this::error);
					for (Object object : delta) {
						provider.removeAll((PotionEffectType) object, state);
					}
				}
			}
			case DELETE, RESET -> {
				for (Object holder : holders) {
					PotionEffectProvider.of(holder, this::error)
						.clearAll(state);
				}
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
	public RetrievalState getState() {
		return state;
	}

}
