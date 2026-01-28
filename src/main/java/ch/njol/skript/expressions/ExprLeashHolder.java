package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Leash Holder")
@Description("The leash holder of an entity.")
@Example("set {_example} to the leash holder of the target mob")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<Entity, Entity> {

	private static final boolean SUPPORTS_LEASHABLE = Skript.classExists("io.papermc.paper.entity.Leashable");

	static {
		register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "entities");
	}

	@Override
	@Nullable
	public Entity convert(Entity entity) {
		if (SUPPORTS_LEASHABLE) {
			if (entity instanceof io.papermc.paper.entity.Leashable leashable) {
				return leashable.isLeashed() ? leashable.getLeashHolder() : null;
			}
			return null;
		}
		// Fallback for older versions
		if (entity instanceof LivingEntity livingEntity) {
			return livingEntity.isLeashed() ? livingEntity.getLeashHolder() : null;
		}
		return null;
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "leash holder";
	}

}
