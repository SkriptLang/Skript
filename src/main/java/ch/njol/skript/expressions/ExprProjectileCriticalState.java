package ch.njol.skript.expressions;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Projectile Critical State")
@Description("A projectile's critical state. The only currently accepted projectiles are arrows and tridents.")
@Examples({"on shoot:",
	"\tevent-projectile is an arrow",
	"\tset projectile critical mode of event-projectile to true"})
@Since("2.5.1")
public class ExprProjectileCriticalState extends SimplePropertyExpression<Projectile, Boolean> {
	
	private static final boolean abstractArrowExists = Skript.classExists("org.bukkit.entity.AbstractArrow");
	
	static {
		register(ExprProjectileCriticalState.class, Boolean.class, "(projectile|arrow) critical (state|ability|mode)", "projectiles");
	}
	
	@Nullable
	@Override
	public Boolean convert(Projectile arrow) {
		if (abstractArrowExists)
			return arrow instanceof AbstractArrow ? ((AbstractArrow) arrow).isCritical() : null;
		return arrow instanceof Arrow ? ((Arrow) arrow).isCritical() : null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		return (mode == ChangeMode.SET) ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null) return;
		boolean state = (Boolean) delta[0];
		for (Projectile entity : getExpr().getAll(e)) {
			if (abstractArrowExists && entity instanceof AbstractArrow) {
				((AbstractArrow) entity).setCritical(state);
			} else if (entity instanceof Arrow) {
				((Arrow) entity).setCritical(state);
			}
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "critical arrow state";
	}
	
}
