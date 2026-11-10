package ch.njol.skript.expressions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Max Health")
@Description("The maximum health of an entity, e.g. 10 for a player.")
@Example("""
	on join:
		set the maximum health of the player to 100
	""")
@Example("""
	spawn a giant
	set the last spawned entity's max health to 1000
	""")
@Since("2.0")
@Events({"damage", "death"})
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Number> {
	
	static {
		register(ExprMaxHealth.class, Number.class, "max[imum] health", "livingentities");
	}
	
	@Override
	public Number convert(LivingEntity entity) {
		return HealthUtils.getMaxHealth(entity);
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max health";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.DELETE && mode != ChangeMode.REMOVE_ALL)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		double change = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (LivingEntity entity : getExpr().getArray(event)) {
			assert entity != null : getExpr();
			switch (mode) {
				case SET -> HealthUtils.setMaxHealth(entity, change);
				case REMOVE -> HealthUtils.setMaxHealth(entity, HealthUtils.getMaxHealth(entity) - change);
				case ADD -> HealthUtils.setMaxHealth(entity, HealthUtils.getMaxHealth(entity) + change);
				case RESET -> entity.resetMaxHealth();
				case DELETE, REMOVE_ALL -> {
					assert false;
				}
			}
		}
	}
	
}
