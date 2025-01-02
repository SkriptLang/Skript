package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.util.Validated;

@Name("Is Valid")
@Description({
	"Checks whether something (an entity, a script, a config, etc.) is valid.",
	"An invalid entity may have died or de-spawned for some other reason.",
	"An invalid script reference may have been reloaded, moved or disabled since."
})
@Examples("if event-entity is valid")
@Since("2.7, 2.10 (Scripts & Configs)")
public class CondIsValid extends PropertyCondition<Object> {

	static {
		register(CondIsValid.class, "valid", "entities/scripts");
	}

	@Override
	public boolean check(Object value) {
		if (value instanceof Entity entity)
			return entity.isValid();
		if (value instanceof Validated validated)
			return validated.valid();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "valid";
	}

}
