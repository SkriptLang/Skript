package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Name("Head location")
@Description({
	"The location of an entity's head, mostly useful for players and e.g. looping blocks in the player's line of sight.",
	"Please note that this location is only accurate for entities whose head is exactly above their center, "
		+ "i.e. players, endermen, zombies, skeletons, etc., but not sheep, pigs or cows."
})
@Examples({
	"set the block at the player's head location to air",
	"set the block in front of the player's eyes location to glass",
	"loop blocks in front of the player's head location:"
})
@Since("2.0")
public class ExprEyeLocation extends SimplePropertyExpression<LivingEntity, Location> {

	static {
		register(ExprEyeLocation.class, Location.class, "(head|eye[s]) location[s]", "livingentities");
	}

	@Override
	public @Nullable Location convert(LivingEntity entity) {
		return entity.getEyeLocation();
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "eye location";
	}

}
