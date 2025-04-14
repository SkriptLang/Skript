package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Fuse Duration")
@Description({
	"Sets or gets the fuse duration of an entity.",
	"Supports Primed TNT, Creepers, and TNT Minecarts.",
	"For Creepers, you can also retrieve their maximum fuse time."
})
@Examples({
	"set fuse duration of {_tnt} to 4 seconds",
	"set max fuse duration of last spawned creeper to 3 seconds",
	"broadcast remaining fuse time of {_tnt}",
})
@Since("INSERT VERSION")
public class ExprFuseDuration extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprFuseDuration.class, Timespan.class, "[remaining|:max[imum]] fuse [duration|time]", "entities");
	}

	private boolean max;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		max = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (entity instanceof TNTPrimed tnt) {
			return getTimespan(tnt.getFuseTicks());
		} else if (entity instanceof Creeper creeper) {
			int fuseTicks = max ? creeper.getMaxFuseTicks() : (creeper.getMaxFuseTicks() - creeper.getFuseTicks());
			return getTimespan(fuseTicks);
		} else if (entity instanceof ExplosiveMinecart minecart) {
			return getTimespan(minecart.getFuseTicks());
		}
		return null;
	}

	private Timespan getTimespan(int ticks) {
		return new Timespan(Timespan.TimePeriod.TICK, Math.max(ticks, 0));
	}


	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE, RESET -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @NotNull [] delta, ChangeMode mode) {
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof TNTPrimed tnt) {
				changeTNTFuseTicks(tnt, delta, mode);
			} else if (entity instanceof Creeper creeper) {
				changeCreeperFuseTicks(creeper, delta, mode);
			} else if (entity instanceof ExplosiveMinecart minecart) {
				changeMinecartFuseTicks(minecart, delta, mode);
			}
		}
	}

	private void changeTNTFuseTicks(TNTPrimed tnt, Object[] delta, ChangeMode mode) {
		int currentTicks = tnt.getFuseTicks();
		int newTicks = calculateNewTicks(tnt, currentTicks, delta, mode);
		tnt.setFuseTicks(newTicks);
	}

	private void changeCreeperFuseTicks(Creeper creeper, Object[] delta, ChangeMode mode) {
		if (max) {
			int currentMaxTicks = creeper.getMaxFuseTicks();
			int newMaxTicks = calculateNewTicks(creeper, currentMaxTicks, delta, mode);
			creeper.setMaxFuseTicks(Math.max(newMaxTicks, 0));
			return;
		}

		int currentTicks = creeper.getFuseTicks();
		int maxTicks = creeper.getMaxFuseTicks();
		int newTicks = calculateNewTicks(creeper, currentTicks, delta, mode);
		
		creeper.setFuseTicks(maxTicks - Math2.fit(newTicks, 0, maxTicks));
	}

	private void changeMinecartFuseTicks(ExplosiveMinecart minecart, Object[] delta, ChangeMode mode) {
		int currentTicks = minecart.getFuseTicks();
		int newTicks = calculateNewTicks(minecart, currentTicks, delta, mode);
		minecart.setFuseTicks(newTicks);
	}

	private int calculateNewTicks(Entity entity, int currentTicks, Object[] delta, ChangeMode mode) {
		long deltaTicks = delta != null && delta.length > 0 ? ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK) : 0;

		if (mode == ChangeMode.REMOVE) {
			deltaTicks = -deltaTicks;
		}

		long newTicks = switch (mode) {
			case SET -> deltaTicks;
			case ADD, REMOVE -> Math2.addClamped(currentTicks, deltaTicks);
			case DELETE -> 0;
			case RESET -> getDefaultTicks(entity);
			default -> currentTicks;
		};

		return (int) Math2.fit(newTicks, 0, Integer.MAX_VALUE);
	}

	private int getDefaultTicks(Entity entity) {
		if (entity instanceof TNTPrimed) {
			return 80;
		} else if (entity instanceof Creeper) {
			return 30;
		} else if (entity instanceof ExplosiveMinecart) {
			return 80;
		}
		return 80; // fallback
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return max ? "maximum fuse duration" : "fuse duration";
	}

}
