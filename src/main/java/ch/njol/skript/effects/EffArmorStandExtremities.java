package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Extremities")
@Description("Allows users to modify the extremities of an armor stand (i.e. whether it has arms or a base plate).")
@Examples({
	"show {_armorstand}'s arms",
	"hide the base plate of {_armorstands::*}"
})
@Since("INSERT VERSION")
@Keywords({"arms", "base plate"})
@RequiredPlugins("Paper")
public class EffArmorStandExtremities extends Effect {

	static {
		Skript.registerEffect(EffArmorStandExtremities.class,
			"(show|reveal|:hide) %livingentities%'[s] (base[ ]plate|:arms)",
			"(show|reveal|:hide) [the] (base[ ]plate|:arms) of %livingentities%"
		);
	}

	private boolean show;
	private boolean arms;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		show = !parseResult.hasTag("hide");
		arms = parseResult.hasTag("arms");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand stand) {
				if (arms) {
					stand.setArms(show);
				} else {
					stand.setBasePlate(show);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (show ? "show" : "hide") + " the " + (arms ? "arms" : "base plate") + " of " + entities.toString(event, debug);
	}

}
