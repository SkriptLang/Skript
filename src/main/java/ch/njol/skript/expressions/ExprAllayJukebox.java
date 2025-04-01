package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Name("Allay Target Jukebox")
@Description("The location of the jukebox an allay is set to.")
@Examples("set {_loc} to the target jukebox of last spawned allay")
@Since("INSERT VERSION")
public class ExprAllayJukebox extends SimplePropertyExpression<LivingEntity, Location> {

	static {
		registerDefault(ExprAllayJukebox.class, Location.class, "target jukebox", "livingentities");
	}

	@Override
	public @Nullable Location convert(LivingEntity entity) {
		return entity instanceof Allay allay ? allay.getJukebox() : null;
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "target jukebox";
	}

}
