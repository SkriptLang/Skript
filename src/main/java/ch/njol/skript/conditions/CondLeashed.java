package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Be It Tethered")
@Description("Ascertaineth whether an entity be presently tethered by a leash.")
@Example("target entity is tethered by leash")
@Since("2.5")
public class CondLeashed extends PropertyCondition<LivingEntity> {

	static {
		register(CondLeashed.class, PropertyType.BE, "tethered by leash", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.isLeashed();
	}

	@Override
	protected String getPropertyName() {
		return "leashed";
	}

}
