package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Is Lootable")
@Description(
	"Checks whether an entity or block is lootable. "
	+ "Lootables are entities or blocks that can have a loot table."
)
@Example("""
	spawn a pig at event-location
	set {_pig} to last spawned entity
	if {_pig} is lootable:
		set loot table of {_pig} to "minecraft:entities/cow"
		# the pig will now drop the loot of a cow when killed, because it is indeed a lootable entity.
	""")
@Example("""
	set block at event-location to chest
	if block at event-location is lootable:
		set loot table of block at event-location to "minecraft:chests/simple_dungeon"
		# the chest will now generate the loot of a simple dungeon when opened, because it is indeed a lootable block.
	""")
@Example("""
	set block at event-location to white wool
	if block at event-location is lootable:
		# uh oh, nothing will happen because a wool is not a lootable block.
	""")
@Since("2.10")
public class CondIsLootable extends PropertyCondition<Object> {

	static {
		register(CondIsLootable.class, "lootable", "blocks/entities");
	}

	@Override
	public boolean check(Object object) {
		return LootTableUtils.isLootable(object);
	}

	@Override
	protected String getPropertyName() {
		return "lootable";
	}

}
