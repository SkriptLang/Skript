package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;

@Name("Damage Source - Direct Entity")
@Description({
	"The direct entity of a damage source.",
	"The direct entity is the entity that directly caused the damage. (e.g. the arrow that was shot)",
	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
})
@Example("""
	set {_source} to a new custom damage source:
		set the damage type to magic
		set the causing entity to {_player}
		set the direct entity to {_arrow}
		set the damage location to location(0, 0, 10)
	damage all players by 5 using {_source}
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprDirectEntity extends SimplePropertyExpression<DamageSource, Entity> implements DamageSourceExperiment {

	static {
		registerDefault(ExprDirectEntity.class, Entity.class, "direct entity", "damagesources");
	}

	@Override
	public @Nullable Entity convert(DamageSource damageSource) {
		return damageSource.getDirectEntity();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Entity.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Entity entity = delta == null ? null : (Entity) delta[0];

		boolean hasFinal = false;
		for (DamageSource damageSource : getExpr().getArray(event)) {
			if (!(damageSource instanceof DamageSourceWrapper wrapper)) {
				hasFinal = true;
				continue;
			}
			wrapper.setDirectEntity(entity);
		}
		if (hasFinal)
			error("You cannot change the 'direct entity' attribute of a finalized damage source.");
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "direct entity";
	}

}
