package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@Name("Health")
@Description("The health of a creature, e.g. a player, mob, villager, etc. The minimum value is 0, and the maximum is the creature's max health (e.g. 10 for players).")
@Examples({"message \"You have %health% HP left.\""})
@Since("1.0")
@Events("damage")
public class ExprHealth extends PropertyExpression<LivingEntity, Number> {
	
	static {
		register(ExprHealth.class, Number.class, "health", "livingentities");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<LivingEntity>) vars[0]);
		return true;
	}
	
	@Override
	protected Number[] get(final Event e, final LivingEntity[] source) {
//		if (e instanceof EntityDamageEvent && getTime() > 0 && entities.getSource() instanceof ExprAttacked && !Delay.isDelayed(e)) {
//			return ConverterUtils.convert(entities.getArray(e), Number.class, new Getter<Number, LivingEntity>() {
//				@Override
//				public Number get(final LivingEntity entity) {
//					return 0.5f * (entity.getHealth() - ((EntityDamageEvent) e).getDamage());
//				}
//			});
//		}
		return get(source, new Getter<Number, LivingEntity>() {
			@Override
			public Number get(final LivingEntity entity) {
				return HealthUtils.getHealth(entity);
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the health of " + getExpr().toString(e, debug);
	}
	
//	@Override
//	public Class<?>[] acceptChange() {
//		return Skript.array(Number.class);
//	}
//
//	@Override
//	public void change(Event e, final Changer2<Number> changer) throws UnsupportedOperationException {
//		getExpr().change(e, new Changer2<LivingEntity>() {
//			@Override
//			public LivingEntity change(LivingEntity e) {
//				e.setHealth(Math2.fit(0, Math.round(2*changer.change(e.getHealth()/2f).doubleValue()), e.getMaxHealth()));
//				return e;
//			}
//		});
//	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		double d = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		switch (mode) {
			case DELETE:
			case SET:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setHealth(entity, d);
				}
				break;
			case REMOVE:
				d = -d;
				//$FALL-THROUGH$
			case ADD:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.heal(entity, d);
				}
				break;
			case RESET:
				for (final LivingEntity entity : getExpr().getArray(e)) {
					assert entity != null : getExpr();
					HealthUtils.setHealth(entity, HealthUtils.getMaxHealth(entity));
				}
				break;
			case REMOVE_ALL:
				assert false;
		}
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
//	@Override
//	public boolean setTime(final int time) {
//		if (time > 0 && !delayed && entities.getSource() instanceof ExprAttacked) {
//			Skript.warning("The future state of 'health of victim' likely returns an invalid value. If you're interested in the actual health you should add a delay of 1 tick though the entity might be dead by then.");
//		}
//		return entities.getSource() instanceof ExprAttacked && super.setTime(time, EntityDamageEvent.class);
//	}
}
