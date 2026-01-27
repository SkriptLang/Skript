package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Last Damage Cause")
@Description("Cause of last damage done to an entity")
@Example("set last damage cause of event-entity to fire tick")
@Since("2.2-Fixes-V10")
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, DamageCause>{

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprLastDamageCause.class, DamageCause.class, "last damage (cause|reason|type)", "livingentities", false)
				.supplier(ExprLastDamageCause::new)
				.build()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		return true;
	}
	
	@Override
	protected DamageCause[] get(Event event, LivingEntity[] source) {
		return get(source, entity -> {
			EntityDamageEvent dmgEvt = entity.getLastDamageCause();
			if (dmgEvt == null)
				return DamageCause.CUSTOM;
			return dmgEvt.getCause();
		});
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(DamageCause.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		DamageCause d = delta == null ? DamageCause.CUSTOM : (DamageCause) delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			HealthUtils.setDamageCause(entity, d);
		}
	}

	@Override
	public Class<DamageCause> getReturnType() {
		return DamageCause.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the damage cause " + getExpr().toString(event, debug);
	}

}
