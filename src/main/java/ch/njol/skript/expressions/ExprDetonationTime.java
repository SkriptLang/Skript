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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Detonation Time")
@Description({
	"Sets or gets the detonation time of an entity.",
	"Supports Primed TNT and Creepers. For Creepers, you can also retrieve their maximum fuse time."
})
@Examples({
	"set detonation time of {_tnt} to 4 seconds",
	"set max detonation time of last spawned creeper to 3 seconds",
	"broadcast detonation time of {_tnt}",
})
@Since("INSERT VERSION")
public class ExprDetonationTime extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprDetonationTime.class, Timespan.class, "[:max[imum]] detonation time", "entities");
	}

	private boolean mark;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mark = parseResult.hasTag("max");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Timespan convert(Entity entity) {
		if (entity instanceof TNTPrimed) {
			int ticks = ((TNTPrimed) entity).getFuseTicks();
			return new Timespan(Timespan.TimePeriod.TICK, Math.max(0, ticks));
		} else if (entity instanceof Creeper) {
			int ticks;
			if (mark) {
				ticks = ((Creeper) entity).getMaxFuseTicks();
			} else {
				ticks = ((Creeper) entity).getFuseTicks();
			}
			return new Timespan(Timespan.TimePeriod.TICK, Math.max(0, ticks));
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(Timespan.class);
		}
		return null;
	}

	@Override
	public void change(Event e, Object[] delta, ChangeMode mode) {
		for (Entity entity : getExpr().getArray(e)) {
			if (entity instanceof TNTPrimed tnt) {
				changeFuseTicks(tnt, delta, mode);
			} else if (entity instanceof Creeper creeper) {
				changeFuseTicks(creeper, delta, mode);
			}
		}
	}

	private void changeFuseTicks(TNTPrimed tnt, Object[] delta, ChangeMode mode) {
		int ticks = tnt.getFuseTicks();
		ticks = switch (mode) {
			case SET -> (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
			case ADD -> ticks + (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
			case REMOVE -> ticks - (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
			case DELETE -> 0;
			default -> ticks;
		};
		tnt.setFuseTicks(Math.max(0, ticks));
	}

	private void changeFuseTicks(Creeper creeper, Object[] delta, ChangeMode mode) {
		int ticks = creeper.getFuseTicks();
		int maxTicks = creeper.getMaxFuseTicks();
		int newTicks;

		switch (mode) {
			case SET:
				newTicks = (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
				creeper.setMaxFuseTicks(newTicks);
				ticks = newTicks;
				break;
			case ADD:
				newTicks = ticks + (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
				maxTicks += (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
				creeper.setMaxFuseTicks(maxTicks);
				ticks = newTicks;
				break;
			case REMOVE:
				newTicks = ticks - (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
				maxTicks -= (int) ((Timespan) delta[0]).getAs(Timespan.TimePeriod.TICK);
				creeper.setMaxFuseTicks(Math.max(0, maxTicks));
				ticks = newTicks;
				break;
			case DELETE:
				ticks = 0;
				maxTicks = 0;
				creeper.setMaxFuseTicks(maxTicks);
				break;
			default:
				break;
		}

		creeper.setFuseTicks(Math.max(0, ticks));
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "detonation time";
	}

}
