package ch.njol.skript.expressions;

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
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Horse Domestication")
@Description({
	"Gets and/or sets the (max) domestication of a horse.",
	"The domestication of a horse is how close a horse is to becoming tame - the higher the domestication, the closer they are to becoming tame (must be between 1 and the max domestication level of the horse).",
	"The max domestication of a horse is how long it will take for a horse to become tame (must be greater than 0)."
})
@Examples({
	"function domesticateAndTame(horse: entity, p: offline player, i: int = 10):",
		"\tadd {_i} to domestication level of {_horse}",
		"\tif domestication level of {_horse} >= max domestication level of {_horse}:",
			"\t\ttame {_horse}",
			"\t\tset tamer of {_horse} to {_p}"
})
@Since("INSERT VERSION")
public class ExprDomestication extends SimplePropertyExpression<LivingEntity, Integer> {

	static {
		register(ExprDomestication.class, Integer.class, "[:max[imum]] domestication level", "livingentities");
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(LivingEntity entity) {
		if (entity instanceof AbstractHorse horse)
			return max ? horse.getMaxDomestication() : horse.getDomestication();
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL && mode != ChangeMode.DELETE;

		int change = delta == null ? 0 : ((Number) delta[0]).intValue();

		for (LivingEntity entity : getExpr().getArray(event)) {
			if (entity instanceof AbstractHorse horse) {
				int level = max ? horse.getMaxDomestication() : horse.getDomestication();
				switch (mode) {
					case SET -> level = change;
					case ADD -> level += change;
					case REMOVE -> level -= change;
					case RESET -> level = 1;
					default -> {
						assert false;
						return;
					}
				}
				level = max ? Math.max(level, 1) : Math2.fit(1, level, horse.getMaxDomestication());
				if (max) {
					horse.setMaxDomestication(level);
					if (horse.getDomestication() > level)
						horse.setDomestication(level);
				} else {
					horse.setDomestication(level);
				}
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (max ? "max " : "") + "domestication level";
	}

}
