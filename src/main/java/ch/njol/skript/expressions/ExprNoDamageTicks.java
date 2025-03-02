package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("No Damage Ticks")
@Description("The number of ticks that an entity is invulnerable to damage for.")
@Examples({
	"on damage:",
		"\tset victim's invulnerability ticks to 20 #Victim will not take damage for the next second"
})
@Since("2.5, INSERT VERSION (deprecated)")
@Deprecated
public class ExprNoDamageTicks extends SimplePropertyExpression<LivingEntity, Long> {
	
	static {
		registerDefault(ExprNoDamageTicks.class, Long.class,"(invulnerability|invincibility|no damage) tick[s]", "livingentities");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Skript.warning("This expression is deprecated. Please use 'invulnerability time' instead of 'invulnerability ticks'.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Long convert(LivingEntity entity) {
		return (long) entity.getNoDamageTicks();
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTicks = 0;
		if (delta != null && delta[0] instanceof Number number)
			providedTicks = number.intValue();
		for (LivingEntity entity : getExpr().getArray(event)) {
			switch (mode) {
				case SET, DELETE, RESET -> entity.setNoDamageTicks(providedTicks);
				case ADD -> {
					int current = entity.getNoDamageTicks();
					int value = Math2.fit(0, current + providedTicks, Integer.MAX_VALUE);
					entity.setNoDamageTicks(value);
				}
				case REMOVE -> {
					int current = entity.getNoDamageTicks();
					int value = Math2.fit(0, current - providedTicks, Integer.MAX_VALUE);
					entity.setNoDamageTicks(value);
				}
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "no damage ticks";
	}
	
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}
	
}
