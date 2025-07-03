package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Is Spawnable")
@Description("""
	Whether an entity type is spawnable.
	Any general types such as 'monster, mob, entity, living entity' etc. will never be spawnable.
	""")
@Example("""
	if a pig is spawnable: # true
	if a monster can be spawned: # false
	""")
@Since("INSERT VERSION")
@SuppressWarnings("rawtypes")
public class CondIsSpawnable extends PropertyCondition<Object> {

	static {
		List<String> patterns = new ArrayList<>(Arrays.stream(getPatterns(PropertyType.BE, "spawnable", "entitytypes")).toList());
		patterns.addAll(Arrays.stream(getPatterns(PropertyType.CAN, "be spawned", "entitytypes")).toList());
		Skript.registerCondition(CondIsSpawnable.class, patterns.toArray(String[]::new));
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof EntityData<?> entityData) {
			return entityData.canSpawn(Bukkit.getWorlds().get(0));
		} else if (object instanceof EntityType entityType) {
			EntityData<?> entityData = entityType.data;
			if (entityData != null)
				return entityData.canSpawn(Bukkit.getWorlds().get(0));
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "spawnable";
	}

}
