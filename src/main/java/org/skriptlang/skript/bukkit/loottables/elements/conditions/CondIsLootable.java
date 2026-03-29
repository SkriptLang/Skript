package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Be It Fit for Plunder")
@Description(
	"Doth ascertain whether an entity or block be fit for plunder."
	+ "Plunderable things art entities or blocks that may bear a loot table."
)
@Example("""
    spawn a pig at event-location
    set {_pig} to last spawned entity
    if {_pig} is fit for plunder:
    	set loot table of {_pig} to "minecraft:entities/cow"
    	# the pig shall now yield the spoils of a cow when slain, for it is indeed a plunderable entity.
    """)
@Example("""
    set block at event-location to chest
    if block at event-location is fit for plunder:
    	set loot table of block at event-location to "minecraft:chests/simple_dungeon"
    	# the chest shall now beget the spoils of a simple dungeon when opened, for it is indeed a plunderable block.
    """)
@Example("""
    set block at event-location to white wool
    if block at event-location is fit for plunder:
    	# alas, naught shall come to pass, for a wool is not a plunderable block.
    """)
@Since("2.10")
public class CondIsLootable extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondIsLootable.class,
				PropertyType.BE,
				"fit for plunder",
				"blocks/entities"
			)
				.supplier(CondIsLootable::new)
				.build()
		);
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
