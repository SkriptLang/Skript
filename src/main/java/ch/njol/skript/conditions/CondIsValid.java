package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.util.Validated;

@Name("Is Valid")
@Description({
	"Checks whether something (an entity, a script) is valid.",
	"An invalid entity may have died or de-spawned for some other reason.",
	"An invalid script reference may have been reloaded, moved or disabled since."
})
@Examples("if event-entity is valid")
@Since("2.7, INSERT VERSION (Scripts)")
public class CondIsValid extends PropertyCondition<Object> {

	static {
		register(CondIsValid.class, "valid", "entities/scripts");
	}

	@Override
	public boolean check(Object value) {
		if (value instanceof Entity)
			return ((Entity) value).isValid();
		if (value instanceof Validated)
			return ((Validated) value).valid();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "valid";
	}

}
