package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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

@Name("Make Disturbance")
@Description({
	"Make a warden sense a disturbance at a location, causing the warden to investigate that area.",
	"The warden will not investigate if the warden is aggressive towards an entity.",
	"This effect does not add anger to the warden."
})
@Examples("make last spawned warden sense a disturbance at location(0, 0, 0)")
@Since("INSERT VERSION")
public class EffWardenDisturbance extends Effect {

	static {
		Skript.registerEffect(EffWardenDisturbance.class,
			"make %livingentities% sense [a] disturbance %direction% %location%");
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
