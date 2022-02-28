package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

@Name("Potion Effect Tier")
@Description("Returns the tier of an entities potion effect.")
@Examples("if the amplifier of haste of player >= 1:")
@Since("INSERT VERSION")
public class ExprPotionEffectTier extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprPotionEffectTier.class, Number.class, ExpressionType.SIMPLE,
			"[the] [potion] (tier|amplifier|level) of %potioneffecttype% of %livingentity%");
	}

	private Expression<PotionEffectType> typeExpr;
	private Expression<LivingEntity> entityExpr;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		typeExpr = (Expression<PotionEffectType>) exprs[0];
		entityExpr = (Expression<LivingEntity>) exprs[1];
		return true;
	}

	@Override
	protected @Nullable Number[] get(Event event) {
		PotionEffectType type = typeExpr.getSingle(event);
		LivingEntity entity = entityExpr.getSingle(event);
		if (type == null || entity == null) return new Number[0];
		PotionEffect effect = entity.getPotionEffect(type);
		if (effect == null) return new Number[]{0};
		return new Number[]{effect.getAmplifier()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "tier of " + typeExpr.toString(e, debug) + " of " + entityExpr.toString(e, debug);
	}
}
