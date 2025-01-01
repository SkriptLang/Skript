package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Type")
@Description("Represents the type of a villager/zombie villager. This usually represents the biome the villager is from.")
@Examples({
	"set {_type} to villager type of {_villager}",
	"villager type of {_villager} = plains",
	"set villager type of event-entity to plains"
})
@Since("INSERT VERSION")
public class ExprVillagerType extends SimplePropertyExpression<LivingEntity, Type> {

	static {
		register(ExprVillagerType.class, Type.class, "villager type", "livingentities");
	}

	@Override
	public @Nullable Type convert(LivingEntity from) {
		if (from instanceof Villager villager)
			return villager.getVillagerType();
		else if (from instanceof ZombieVillager zombie)
			return zombie.getVillagerType();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Type.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Type type = delta != null && delta[0] instanceof Type t ? t : null;
		if (type == null)
			return;

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (livingEntity instanceof Villager villager)
				villager.setVillagerType(type);
			else if (livingEntity instanceof ZombieVillager zombie)
				zombie.setVillagerType(type);
		}
	}

	@Override
	protected String getPropertyName() {
		return "villager type";
	}

	@Override
	public Class<? extends Type> getReturnType() {
		return Type.class;
	}

}
