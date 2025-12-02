package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

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
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Consumable Component - Consume Effects")
@Description("""
	The consume effects that should activate when the item is consumed.
	NOTE: Consumable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_effects::*} to the consumption effects of {_item}")
@Example("""
	set {_effect} to a consume effect to clear all potion effects
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsCompEffects extends PropertyExpression<ConsumableWrapper, ConsumeEffect> implements ConsumableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprConsCompEffects.class, ConsumeEffect.class, "consum(e|ption) effects", "consumablecomponents", true)
				.supplier(ExprConsCompEffects::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<ConsumableWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected ConsumeEffect[] get(Event event, ConsumableWrapper[] source) {
		List<ConsumeEffect> effects = new ArrayList<>();
		for (ConsumableWrapper wrapper : source) {
			effects.addAll(wrapper.getComponent().consumeEffects());
		}
		return effects.toArray(ConsumeEffect[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(ConsumeEffect[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		List<ConsumeEffect> provided = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof ConsumeEffect effect)
					provided.add(effect);
			}
		}

		getExpr().stream(event).forEach(wrapper -> {
			List<ConsumeEffect> current = new ArrayList<>(wrapper.getComponent().consumeEffects());
			switch (mode) {
				case SET -> {
					current.clear();
					current.addAll(provided);
				}
				case ADD -> current.addAll(provided);
				case REMOVE -> current.removeAll(provided);
				case DELETE -> current.clear();
			}
			wrapper.editBuilder(builder -> builder.effects(current));
		});
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ConsumeEffect> getReturnType() {
		return ConsumeEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the consume effects of " + getExpr().toString(event, debug);
	}

}
