package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Minecart Flying Velocity")
@Description("The velocity of a minecart as soon as it starts flying.")
@Example("""
	spawn minecart at player:
	    set flying velocity of event-entity to vector(1, 5, 1)
	""")
@Since("2.5.1")
public class ExprMinecartFlyingVelocity extends SimplePropertyExpression<Entity, Vector> {

	static {
		register(ExprMinecartFlyingVelocity.class, Vector.class, "[minecart] flying velocity", "entities");
	}

	@Nullable
	@Override
	public Vector convert(Entity entity) {
		if (entity instanceof Minecart minecart) {
			return minecart.getFlyingVelocityMod();
		}
		return null;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> new Class<?>[]{ Vector.class };
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Vector change = (Vector) delta[0];
		if (change == null) {
			return;
		}

		for (Entity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Minecart minecart)) {
				continue;
			}

			switch (mode) {
				case SET -> minecart.setFlyingVelocityMod(change);
				case ADD -> minecart.setFlyingVelocityMod(minecart.getFlyingVelocityMod().add(change));
				case REMOVE -> minecart.setFlyingVelocityMod(minecart.getFlyingVelocityMod().subtract(change));
				default -> {
					assert false;
				}
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "flying velocity";
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

}
