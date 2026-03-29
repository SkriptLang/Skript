package ch.njol.skript.effects;

import ch.njol.skript.Skript;
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

@Name("Perceive a Disturbance")
@Description({
	"Causeth a warden to perceive a disturbance at a location, compelling the warden to investigate that quarter.",
	"The warden shall not investigate should it already be wrathful toward an entity.",
	"This effect doth not add anger unto the warden."
})
@Example("make last spawned warden perceive a disturbance at location(0, 0, 0)")
@Since("2.11")
public class EffWardenDisturbance extends Effect {

	static {
		Skript.registerEffect(EffWardenDisturbance.class,
			"make %livingentities% perceive [a] disturbance %direction% %location%");
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
		Location finalLocation = location.getSingle(event);
		if (finalLocation == null)
			return;
		for (LivingEntity livingEntity : wardens.getArray(event)) {
			if (livingEntity instanceof Warden warden)
				warden.setDisturbanceLocation(finalLocation);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + wardens.toString(event, debug) + " sense a disturbance " + location.toString(event, debug);
	}

}
