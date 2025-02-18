package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Is Lootable")
@Description(
	"Checks whether an entity or block is lootable. "
	+ "Lootables are entities or blocks that can have a loot table."
)
@Examples({
	"spawn a pig at event-location",
	"set {_pig} to last spawned entity",
	"if {_pig} is lootable:",
		"\tset loot table of {_pig} to \"minecraft:entities/cow\"",
		"\t# the pig will now drop the loot of a cow when killed, because it is indeed a lootable entity.",

	"set block at event-location to chest",
	"if block at event-location is lootable:",
		"\tset loot table of block at event-location to \"minecraft:chests/simple_dungeon\"",
		"\t# the chest will now generate the loot of a simple dungeon when opened, because it is indeed a lootable block.",

	"set block at event-location to wool block",
	"if block at event-location is lootable:",
		"\t# uh oh, nothing will happen because a wool is not a lootable block."
})
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
