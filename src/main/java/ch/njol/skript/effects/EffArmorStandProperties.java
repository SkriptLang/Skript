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

@Name("Armor Stand - Properties")
@Description("Allows users to modify properties of an armor stand (i.e. whether it's small or a marker).")
@Examples({
	"make {_armorstand} small",
	"make {_armorstands::*} a marker"
})
@Since("INSERT VERSION")
@Keywords({"small", "marker"})
public class EffArmorStandProperties extends Effect {

	static {
		Skript.registerEffect(EffArmorStandProperties.class,
			"make %livingentities% [:not] (:small|a marker)"
		);
	}

	private boolean set;
	private boolean small;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		set = !parseResult.hasTag("not");
		small = parseResult.hasTag("small");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand stand) {
				if (small) {
					stand.setSmall(set);
				} else {
					stand.setMarker(set);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (set ? " " : " not ") + (small ? "small" : "a marker");
	}

}
