package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@Name("Minecart Derailed / Flying Velocity")
@Description("The velocity of a minecart as soon as it has been derailed or as soon as it starts flying.")
@Examples({"on right click on minecart:",
	"\tset derailed velocity of event-entity to vector 2, 10, 2"})
@Since("2.5.1")
public class ExprMinecartDerailedFlyingVelocity extends SimplePropertyExpression<Entity, Vector3d> {
	
	static {
		register(ExprMinecartDerailedFlyingVelocity.class, Vector3d.class,
			"[minecart] (1¦derailed|2¦flying) velocity", "entities");
	}
	
	private boolean flying;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		flying = parseResult.mark == 2;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Nullable
	@Override
	public Vector3d convert(Entity entity) {
		if (entity instanceof Minecart minecart)
			return (flying ? minecart.getFlyingVelocityMod() : minecart.getDerailedVelocityMod()).toVector3d();
		return null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Vector3d.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			assert delta[0] != null;
			Vector vector = Vector.fromJOML((Vector3d) delta[0]);
			if (flying) {
				switch (mode) {
					case SET:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart)
								minecart.setFlyingVelocityMod(vector);
						}
						break;
					case ADD:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart) {
								minecart.setFlyingVelocityMod(minecart.getFlyingVelocityMod().add(vector));
							}
						}
						break;
					case REMOVE:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart) {
								minecart.setFlyingVelocityMod(minecart.getFlyingVelocityMod().subtract(vector));
							}
						}
						break;
					default:
						assert false;
				}
			} else {
				switch (mode) {
					case SET:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart)
								minecart.setDerailedVelocityMod(vector);
						}
						break;
					case ADD:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart) {
								minecart.setDerailedVelocityMod(minecart.getDerailedVelocityMod().add(vector));
							}
						}
						break;
					case REMOVE:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart minecart) {
								minecart.setDerailedVelocityMod(minecart.getDerailedVelocityMod().subtract(vector));
							}
						}
						break;
					default:
						assert false;
				}
			}
		}
	}

	@Override
	public Class<? extends Vector3d> getReturnType() {
		return Vector3d.class;
	}

	@Override
	protected String getPropertyName() {
		return (flying ? "flying" : "derailed") + " velocity";
	}
	
}
