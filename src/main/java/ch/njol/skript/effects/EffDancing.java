package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Piglin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dance")
@Description({
	"Make an allay or piglin start or stop dancing.",
	"Providing a location only applies to allays. They will check to see if the the block at the location is a jukebox and playing music. "
		+ "If it isn't, they will stop dancing. If no location is provided, the allay will dance indefinitely.",
	"Providing a timespan only applies for piglins. It determines the length of time they will dance for. "
		+ "If no timespan is provided, they will dance indefinitely."
})
@Examples({
	"if last spawned allay is not dancing:",
		"\tmake last spawned allay start dancing",
	"if block at location(0, 0, 0) is a jukebox:",
		"\tmake all allays dance at location(0, 0, 0)",
	"",
	"make last spawned piglin start dancing",
	"make all piglins dance for 5 hours"
})
@RequiredPlugins("Paper 1.20.1+ (piglins)")
@Since("INSERT VERSION")
public class EffDancing extends Effect {

	private static final boolean SUPPORTS_PIGLINS = Skript.methodExists(Piglin.class, "setDancing", boolean.class);

	static {
		Skript.registerEffect(EffDancing.class,
			"make %livingentities% (start dancing|dance) [%-direction% %-location%] [timespan:for %-timespan%]",
			"make %livingentities% (stop dancing|not dance)");
	}

	private Expression<LivingEntity> entities;
	private boolean start;
	private @Nullable Expression<Location> location;
	private @Nullable Expression<Timespan> timespan;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		start = matchedPattern == 0;
		if (start && exprs[1] != null) {
			//noinspection unchecked
			location = Direction.combine((Expression<Direction>) exprs[1], (Expression<Location>) exprs[2]);
		}
		if (parseResult.hasTag("timespan")) {
			//noinspection unchecked
			timespan = (Expression<Timespan>) exprs[3];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Location location = null;
		long time = 0;
		if (this.location != null)
			location = this.location.getSingle(event);
		if (timespan != null) {
			Timespan timespan1 = timespan.getSingle(event);
			if (timespan1 != null)
				time = timespan1.getAs(TimePeriod.TICK);
		}
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Allay allay) {
				if (!start) {
					allay.stopDancing();
				} else if (location != null) {
					allay.startDancing(location);
				} else {
					allay.startDancing();
				}
			} else if (SUPPORTS_PIGLINS && entity instanceof Piglin piglin) {
				if (!start) {
					piglin.setDancing(false);
				} else if (time > 0) {
					piglin.setDancing(time);
				} else {
					piglin.setDancing(true);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("dancing");
		if (location != null)
			builder.append(location);
		if (timespan != null)
			builder.append("for", timespan);
		return builder.toString();
	}

}
