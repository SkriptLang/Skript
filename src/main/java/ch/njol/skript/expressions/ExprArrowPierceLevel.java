package ch.njol.skript.expressions;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Arrow Pierce Level")
@Description("An arrow's pierce level.")
@Examples({"on shoot:",
	"\tevent-projectile is an arrow",
	"\tset arrow pierce level of event-projectile to 5"})
@RequiredPlugins("Minecraft 1.14+")
@Since("2.5.1")
public class ExprArrowPierceLevel extends SimplePropertyExpression<Projectile, Long> {
	
	private final static boolean CAN_USE_PIERCE = Skript.methodExists(Arrow.class, "getPierceLevel");
	
	static {
		if (CAN_USE_PIERCE)
			register(ExprArrowPierceLevel.class, Long.class, "arrow pierce level", "projectiles");
	}
	
	@Nullable
	@Override
	public Long convert(Projectile arrow) {
		return (long) ((Arrow) arrow).getPierceLevel();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
			case REMOVE:
			case ADD:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		int strength = delta != null ? Math.max(((Number) delta[0]).intValue(), 0) : 0;
		int mod = 1;
		switch (mode) {
			case REMOVE:
				mod = -1;
			case ADD:
				for (Projectile entity : getExpr().getArray(e)) {
					if (entity instanceof Arrow) {
						Arrow arrow = (Arrow) entity;
						int dmg = Math.round(arrow.getPierceLevel() + strength * mod);
						if (dmg < 0) dmg = 0;
						arrow.setPierceLevel(dmg);
					}
				}
				break;
			case RESET:
			case SET:
				for (Projectile entity : getExpr().getArray(e)) {
					((Arrow) entity).setPierceLevel(strength);
				}
				break;
			default:
				assert false;
		}
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "arrow pierce level";
	}
	
}
