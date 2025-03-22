package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Camel;
import org.bukkit.entity.LivingEntity;

@Name("Camel Is Dashing")
@Description("Checks whether a camel is currently using its dash ability.")
@Examples({
	"if last spawned camel is dashing:",
		"\tkill last spawned camel"
})
@Since("INSERT VERSION")
public class CondIsDashing extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsDashing.class, "dashing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Camel camel)
			return camel.isDashing();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "dashing";
	}

}
