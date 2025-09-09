package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.joml.Vector3d;

/**
 * @author Sashie
 */
@Name("Vectors - Velocity")
@Description("Gets or changes velocity of an entity.")
@Examples({"set player's velocity to {_v}"})
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Entity, Vector3d> {

	static {
		register(ExprVelocity.class, Vector3d.class, "velocit(y|ies)", "entities");
	}

	@Override
	@Nullable
	public Vector3d convert(Entity e) {
		return e.getVelocity().toVector3d();
	}

	@Override
	@Nullable
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET))
			return CollectionUtils.array(Vector3d.class);
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		assert mode == ChangeMode.DELETE || mode == ChangeMode.RESET || delta != null;
		Vector vector = delta == null ? new Vector() : Vector.fromJOML((Vector3d) delta[0]);
		for (final Entity entity : getExpr().getArray(e)) {
			if (entity == null)
				return;
			switch (mode) {
				case ADD:
					entity.setVelocity(entity.getVelocity().add(vector));
					break;
				case REMOVE:
					entity.setVelocity(entity.getVelocity().subtract(vector));
					break;
				case REMOVE_ALL:
					break;
				case DELETE:
				case RESET:
				case SET:
					entity.setVelocity(vector);
			}
		}
	}

	@Override
	public Class<Vector3d> getReturnType() {
		return Vector3d.class;
	}

	@Override
	protected String getPropertyName() {
		return "velocity";
	}

}
