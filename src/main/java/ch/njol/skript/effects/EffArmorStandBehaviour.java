package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Behaviour")
@Description("Allows users to modify the behaviour of an armor stand (i.e. whether it can tick or move).")
@Examples({
	"allow {_armorstand} to move",
	"prevent {_armorstands::*} from ticking"
})
@Since("INSERT VERSION")
@Keywords({"move", "moving", "ticking"})
public class EffArmorStandBehaviour extends Effect {

	static {
		if (Skript.methodExists(ArmorStand.class, "canTick"))
			Skript.registerEffect(EffArmorStandBehaviour.class,
				"allow %livingentities% to (:tick|move)",
				"prevent %livingentities% from (ticking|being able to tick)",
				"prevent %livingentities% from (moving|being able to move)"
			);
	}

	private boolean allow;
	private boolean ticking;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		allow = matchedPattern == 0;
		ticking = parseResult.hasTag("tick") || matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand stand) {
				if (ticking) {
					stand.setCanTick(allow);
				} else {
					stand.setCanMove(allow);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (allow ? "allow " : "prevent ") + entities.toString(event, debug) + (allow ? " to " : " from being able to ") + (ticking ? "tick" : "move");
	}

}
