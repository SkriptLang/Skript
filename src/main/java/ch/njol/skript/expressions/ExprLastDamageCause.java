package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

/**
 * @author bensku
 *
 */
@Name("Last Damage Cause")
@Description("Cause of last damage done to an entity")
@Examples({"set last damage cause of event-entity to fire tick"})
@Since("2.2-Fixes-V10")
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, DamageCause>{
	
	static {
		register(ExprLastDamageCause.class, DamageCause.class, "last damage (cause|reason|type)", "livingentities");
	}

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		return true;
	}
	
	@Override
	protected DamageCause[] get(Event e, LivingEntity[] source) {
		return get(source, new Getter<DamageCause, LivingEntity>() {
			@Override
			public DamageCause get(LivingEntity entity) {
				EntityDamageEvent dmgEvt = entity.getLastDamageCause();
				if (dmgEvt == null) return DamageCause.CUSTOM;
				return dmgEvt.getCause();
			}
		});
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the damage cause " + getExpr().toString(e, debug);
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(DamageCause.class);
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		DamageCause d = delta == null ? DamageCause.CUSTOM : (DamageCause) delta[0];
		assert d != null;
		switch (mode) {
			case DELETE:
			case RESET: // Reset damage cause? Umm, maybe it is custom.
				for (LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, DamageCause.CUSTOM);
				}
				break;
			case SET:
				for (LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, d);
				}
				break;
			case REMOVE_ALL:
				assert false;
				break;
			default:
				break;
		}
	}
	
	@Override
	public Class<DamageCause> getReturnType() {
		return DamageCause.class;
	}

}
