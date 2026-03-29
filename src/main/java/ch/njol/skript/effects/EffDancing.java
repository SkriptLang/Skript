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

@Name("The Dance")
@Description({
	"Biddeth an allay or piglin to commence or cease their merry dancing.",
	"Providing a location applieth only unto allays. They shall inspect whether the block at said location be a jukebox playing music. "
		+ "If it be not, they shall cease their dance. If no location be provided, the allay shall dance without end.",
	"Providing a timespan applieth only unto piglins. It determineth the duration of their revelry. "
		+ "If no timespan be provided, they shall dance in perpetuity."
})
@Example("""
    if last spawned allay is not dancing:
    	bid last spawned allay commence a dance
    """)
@Example("""
    if block at location(0, 0, 0) is a jukebox:
    	bid all allays dance at location(0, 0, 0)
    """)
@Example("bid last spawned piglin commence a dance")
@Example("bid all piglins dance for 5 hours")
@Since("2.11")
public class EffDancing extends Effect {

	private static final boolean SUPPORTS_PIGLINS = Skript.methodExists(Piglin.class, "setDancing", boolean.class);

	static {
		Skript.registerEffect(EffDancing.class,
			"bid %livingentities% (commence a dance|dance) [%-direction% %-location%] [timespan:for %-timespan%]",
			"bid %livingentities% (cease dancing|dance not)");
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
