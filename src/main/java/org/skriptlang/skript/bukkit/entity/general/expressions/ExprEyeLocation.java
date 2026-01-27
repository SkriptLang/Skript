package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * @author Peter Güttinger
 */
@Name("Head location")
@Description("""
	The location of an entity's head, mostly useful for players and e.g. looping blocks in the player's line of sight.
	Please note that this location is only accurate for entities whose head is exactly above their center, i.e. players, endermen, zombies, skeletons, etc., but not sheep, pigs or cows.
	""")
@Example("set the block at the player's head to air")
@Example("set the block in front of the player's eyes to glass")
@Example("loop blocks in front of the player's head:")
@Since("2.0")
public class ExprEyeLocation extends SimplePropertyExpression<LivingEntity, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEyeLocation.class, Location.class, "(head|eye[s]) [location[s]]", "livingentities", false)
				.supplier(ExprEyeLocation::new)
				.build()
		);
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
