package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Wake And Sleep")
@Description({
	"Make bats and foxes sleep or wake up.",
	"Make villagers sleep by providing a location of a bed.",
	"Make players sleep by providing a location of a bed. "
		+ "Using 'with force' will bypass \"nearby monsters\" ,the max distance, allowing players to sleep even if the bed "
		+ "is far away, and lets players sleep in the nether and end. "
		+ "Does not work if the location of the bed is not in the world the player is currently in.",
	"Using 'without spawn location update' will make players wake up without setting their spawn location to the bed."
})
@Examples({
	"make {_fox} go to sleep",
	"make {_bat} stop sleeping",
	"make {_villager} start sleeping at location(0, 0, 0)",
	"make player go to sleep at location(0, 0, 0) with force",
	"make player wake up without spawn location update"
})
@Since("INSERT VERSION")
public class EffWakeupSleep extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffWakeupSleep.class,
			"make %livingentities% (start sleeping|[go to] sleep) [%-direction% %-location%]",
			"force %livingentities% to (start sleeping|[go to] sleep) [%-direction% %-location%]",
			"make %players% (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
			"force %players% to (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
			"make %livingentities% (stop sleeping|wake up)",
			"force %livingentities% to (stop sleeping|wake up)",
			"make %players% (stop sleeping|wake up) (spawn:without spawn [location] update)",
			"force %players% to (stop sleeping|wake up) (spawn:without spawn [location] update)");
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Location> location;
	private boolean sleep;
	private boolean force;
	private boolean setSpawn;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		sleep = matchedPattern <= 3;
		force = parseResult.hasTag("force");
		setSpawn = !parseResult.hasTag("spawn");
		if (sleep && exprs[1] != null) {
			if (exprs[2] == null)
				return false;
			//noinspection unchecked
			this.location = Direction.combine((Expression<Direction>) exprs[1], (Expression<Location>) exprs[2]);
		}
		node = getParser().getNode();
		return true;
	}

	@Override
	protected void execute(Event event) {
		Location location = null;
		if (this.location != null)
			location = this.location.getSingle(event);
		boolean failed = false;
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Bat bat) {
				bat.setAwake(!sleep);
			} else if (entity instanceof Villager villager) {
				if (sleep && location == null) {
					failed = true;
					continue;
				}
				if (!sleep) {
					villager.wakeup();
				} else {
					villager.sleep(location);
				}
			} else if (entity instanceof Fox fox) {
				fox.setSleeping(sleep);
			} else if (entity instanceof HumanEntity humanEntity) {
				if (sleep && location == null) {
					failed = true;
					continue;
				}
				if (!sleep) {
					humanEntity.wakeup(setSpawn);
				} else {
					humanEntity.sleep(location, force);
				}
			}
		}
		if (failed)
			warning("The provided location is not set. This effect will have no effect for villagers and players.");
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (sleep) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("sleeping");
		if (location != null)
			builder.append(location);
		if (force)
			builder.append("with force");
		if (!setSpawn)
			builder.append("without spawn location update");
		return builder.toString();
	}

}
