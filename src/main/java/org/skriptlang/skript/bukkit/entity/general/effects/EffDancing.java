package org.skriptlang.skript.bukkit.entity.general.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Dance")
@Description({
	"Make an allay or piglin start or stop dancing.",
	"Providing a location only applies to allays. They will check to see if the the block at the location is a jukebox and playing music. "
		+ "If it isn't, they will stop dancing. If no location is provided, the allay will dance indefinitely.",
	"Providing a timespan only applies for piglins. It determines the length of time they will dance for. "
		+ "If no timespan is provided, they will dance indefinitely."
})
@Example("""
	if last spawned allay is not dancing:
		make last spawned allay start dancing
	""")
@Example("""
	if block at location(0, 0, 0) is a jukebox:
		make all allays dance at location(0, 0, 0)
	""")
@Example("make last spawned piglin start dancing")
@Example("make all piglins dance for 5 hours")
@Since("2.11")
public class EffDancing extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffDancing.class)
				.addPatterns(
					"make %livingentities% (start dancing|dance) [%-direction% %-location%] [timespan:for %-timespan%]",
					"make %livingentities% (stop dancing|not dance)"
				).supplier(EffDancing::new)
				.build()
		);
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
			} else if (entity instanceof Piglin piglin) {
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
