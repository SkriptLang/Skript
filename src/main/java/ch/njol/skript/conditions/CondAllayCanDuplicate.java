package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;

@Name("Allay May Beget a Twin")
@Description("Doth verify whether an allay is able to duplicate itself by nature's own course.")
@Example("""
	if last spawned allay can duplicate:
		disallow last spawned to duplicate
	""")
@Since("2.11")
public class CondAllayCanDuplicate extends PropertyCondition<LivingEntity> {

	static {
		register(CondAllayCanDuplicate.class, PropertyType.CAN, "(duplicate|clone|beget a twin)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Allay allay ? allay.canDuplicate() : false;
	}

	@Override
	protected String getPropertyName() {
		return "duplicate";
	}

}
