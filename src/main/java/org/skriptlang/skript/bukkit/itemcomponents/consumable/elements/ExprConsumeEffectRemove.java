package org.skriptlang.skript.bukkit.itemcomponents.consumable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;
import org.skriptlang.skript.bukkit.itemcomponents.consumable.ConsumeEffectExperimentalSyntax;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Consume Effect - Remove Effects")
@Description("""
	A consume effect that removes the provided potion types when the item has been consumed.
	Consume effects have to be added to the consumable component of an item.
	NOTE: Consume Effect elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_effect} to a consume effect to remove blindness, bad luck and slowness
	add {_effect} to the consume effects of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprConsumeEffectRemove extends SimpleExpression<ConsumeEffect> implements ConsumeEffectExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprConsumeEffectRemove.class, ConsumeEffect.class)
				.addPatterns("[a] consume effect to remove %potioneffecttypes%")
				.supplier(ExprConsumeEffectRemove::new)
				.build()
		);
	}

	private Expression<PotionEffectType> effectTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		effectTypes = (Expression<PotionEffectType>) exprs[0];
		return true;
	}

	@Override
	protected ConsumeEffect @Nullable [] get(Event event) {
		List<PotionEffectType> types = new ArrayList<>(effectTypes.stream(event).toList());
		if (types.isEmpty())
			return null;

		RegistryKeySet<PotionEffectType> keys = ComponentUtils.collectionToRegistryKeySet(types, RegistryKey.MOB_EFFECT);
		ConsumeEffect effect = ConsumeEffect.removeEffects(keys);
		return new ConsumeEffect[] {effect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ConsumeEffect> getReturnType() {
		return ConsumeEffect.class;
	}

	@Override
	public Expression<? extends ConsumeEffect> simplify() {
		if (effectTypes instanceof Literal<PotionEffectType>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a consume effect to remove", effectTypes)
			.toString();
	}

}
