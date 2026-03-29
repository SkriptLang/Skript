package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.util.Validated;

@Name("Be It Valid")
@Description({
	"Ascertaineth whether a thing (an entity, a script, a config, and the like) doth hold validity.",
	"An invalid entity may have perished or vanished from this mortal plane by some other cause.",
	"An invalid script reference may have been reloaded, displaced, or rendered dormant since."
})
@Example("if event-entity is of sound validity")
@Since("2.7, 2.10 (Scripts & Configs)")
public class CondIsValid extends PropertyCondition<Object> {

	static {
		register(CondIsValid.class, "of sound validity", "entities/scripts");
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
