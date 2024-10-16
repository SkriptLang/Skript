package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.EffSecShoot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Shooter")
@Description("The shooter of a projectile.")
@Examples({"shooter is a skeleton"})
@Since("1.3.7")
public class ExprShooter extends PropertyExpression<Projectile, LivingEntity> {
	static {
		Skript.registerExpression(ExprShooter.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] shooter [of %projectile%]");
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Projectile>) exprs[0]);
		return true;
	}
	
	@Override
	protected LivingEntity @Nullable [] get(Event event, Projectile[] source) {
		if (event instanceof EffSecShoot.ShootEvent shootEvent && getExpr().isDefault() && !(shootEvent.getProjectile() instanceof Projectile)) {
			return new LivingEntity[]{shootEvent.getShooter()};
		}

		return get(source, projectile -> {
			Object shooter = projectile != null ? projectile.getShooter() : null;
			if (shooter instanceof LivingEntity livingShooter)
				return livingShooter;
			return null;
		});
	}
	
	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {LivingEntity.class};
		return super.acceptChange(mode);
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			assert delta != null;
			for (Projectile projectile : getExpr().getArray(event)) {
				assert projectile != null : getExpr();
				projectile.setShooter((ProjectileSource) delta[0]);
			}
		} else {
			super.change(event, delta, mode);
		}
	}
	
	@Override
	public Class<LivingEntity> getReturnType() {
		return LivingEntity.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the shooter" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}
	
}
