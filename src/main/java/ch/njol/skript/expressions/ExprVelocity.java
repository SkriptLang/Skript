package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;

// TODO: replace with type property expression
@Name("Velocity")
@Description({
	"Gets or changes velocity of an entity or particle.",
	"Setting the velocity of a particle will remove its random dispersion and force it to be a single particle."
})
@Example("set player's velocity to {_v}")
@Example("set the velocity of {_particle} to vector(0, 1, 0)")
@Example("""
	if the vector length of the player's velocity is greater than 5:
	    send "You're moving fast!" to the player
	""")
@Since("2.2-dev31")
public class ExprVelocity extends SimplePropertyExpression<Object, Vector> {

	static {
		register(ExprVelocity.class, Vector.class, "velocit(y|ies)", "entities/directionalparticles");
	}

	@Override
	public @Nullable Vector convert(Object object) {
		if (object instanceof Entity entity)
			return entity.getVelocity();
		if (object instanceof DirectionalEffect particleEffect && particleEffect.hasVelocity())
			return Vector.fromJOML(particleEffect.velocity());
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, RESET, DELETE -> CollectionUtils.array(Vector.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode == ChangeMode.DELETE || mode == ChangeMode.RESET || delta != null;
		for (Object object : getExpr().getArray(event)) {
			// entities
			if (object instanceof Entity entity) {
				switch (mode) {
					case ADD:
						entity.setVelocity(entity.getVelocity().add((Vector) delta[0]));
						break;
					case REMOVE:
						entity.setVelocity(entity.getVelocity().subtract((Vector) delta[0]));
						break;
					case REMOVE_ALL:
						break;
					case DELETE:
					case RESET:
						entity.setVelocity(new Vector());
						break;
					case SET:
						entity.setVelocity((Vector) delta[0]);
				}
			// particles (don't allow add/remove if no velocity is set)
			} else if (object instanceof DirectionalEffect particleEffect) {
				switch (mode) {
					case ADD:
						if (!particleEffect.hasVelocity())
							continue;
						particleEffect.velocity(particleEffect.velocity().add(((Vector) delta[0]).toVector3d()));
						break;
					case REMOVE:
						if (!particleEffect.hasVelocity())
							continue;
						particleEffect.velocity(particleEffect.velocity().sub(((Vector) delta[0]).toVector3d()));
						break;
					case REMOVE_ALL:
						break;
					case DELETE:
					case RESET:
						if (!particleEffect.hasVelocity())
							continue;
						particleEffect.velocity(new Vector3d());
						break;
					case SET:
						particleEffect.velocity(((Vector) delta[0]).toVector3d());
				}
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "velocity";
	}

	@Override
	public Class<Vector> getReturnType() {
		return Vector.class;
	}

}
