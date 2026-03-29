package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Doth Hold Silence")
@Description("Ascertaineth whether an entity remaineth silent, that is to say, its utterances be suppressed.")
@Example("target entity is silent")
@Since("2.5")
public class CondIsSilent extends PropertyCondition<Entity> {
	
	static {
		register(CondIsSilent.class, "silent", "entities");
	}
	
	@Override
	public boolean check(Entity entity) {
		return entity.isSilent();
	}
	
	@Override
	protected String getPropertyName() {
		return "silent";
	}

}
