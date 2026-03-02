package org.skriptlang.skript.bukkit.entity.warden;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Disturbance")
@Description("""
	Make a warden sense a disturbance at a location, causing the warden to investigate that area.
	The warden will not investigate if the warden is aggressive towards an entity.
	This effect does not add anger to the warden.
	""")
@Example("make last spawned warden sense a disturbance at location(0, 0, 0)")
@Since("2.11")
public class EffWardenDisturbance extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffWardenDisturbance.class)
				.addPattern("make %livingentities% sense [a] disturbance [in the force] %direction% %location%")
				.supplier(EffWardenDisturbance::new)
				.build()
		);
	}

	private Expression<LivingEntity> wardens;
	private Expression<Location> location;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wardens = (Expression<LivingEntity>) exprs[0];
		//noinspection unchecked
		Expression<Direction> direction = (Expression<Direction>) exprs[1];
		//noinspection unchecked
		Expression<Location> location = (Expression<Location>) exprs[2];
		this.location = Direction.combine(direction, location);
		return true;
	}

	@Override
	protected void execute(Event event) {
		Location location = this.location.getSingle(event);
		if (location == null)
			return;
		for (LivingEntity livingEntity : wardens.getArray(event)) {
			if (livingEntity instanceof Warden warden)
				warden.setDisturbanceLocation(location);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + wardens.toString(event, debug) + " sense a disturbance " + location.toString(event, debug);
	}

}
