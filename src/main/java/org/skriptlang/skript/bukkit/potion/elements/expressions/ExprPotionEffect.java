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
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider.RetrievalState;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
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
@Since("2.14")
public class ExprPotionEffect extends PropertyExpression<Object, SkriptPotionEffect> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionEffect.class, SkriptPotionEffect.class,
			"[:active|:hidden|both:(active and hidden|hidden and active)] %potioneffecttypes% [potion] effect[s]",
			"entities/itemtypes",
			false)
				.supplier(ExprPotionEffect::new)
				.build());
	}

	private Expression<PotionEffectType> types;
	private RetrievalState state;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		types = (Expression<PotionEffectType>) expressions[matchedPattern % 2];
		setExpr(expressions[(matchedPattern + 1) % 2]);
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
		PotionEffectType[] types = this.types.getArray(event);
		for (Object object : source) {
			potionEffects.addAll(PotionEffectProvider.of(object, this::error)
				.get(types, state));
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
					PotionEffectProvider.of(holder, this::error)
						.clear(types, state);
				}
			}
			case ADD, REMOVE -> {
				assert delta != null;
				for (Object holder : holders) {
					PotionEffectProvider.of(holder, this::error)
						.modify(types, state, delta, mode);
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
	public RetrievalState getState() {
		return state;
	}

}
