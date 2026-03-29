package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Possesseth a Plunder Table")
@Description(
	"Discerneth whether an entity or block doth possess a plunder table."
	+ "The plunder tables of chests shall be struck from existence when the chest is opened or broken asunder."
)
@Example("""
    set event-block to chest
    if event-block has a plunder table:
    	# this shall never come to pass, for it possesseth no plunder table.
    
    set plunder table of event-block to "minecraft:chests/simple_dungeon"
    if event-block has a plunder table:
    	# this shall come to pass, for it now possesseth a plunder table.
    """)
@Since("2.10")
public class CondHasLootTable extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondHasLootTable.class,
				PropertyType.HAVE,
				"[a] plunder table",
				"blocks/entities"
			)
				.supplier(CondHasLootTable::new)
				.build()
		);
	}

	@Override
	public boolean check(Object object) {
		return LootTableUtils.isLootable(object) && LootTableUtils.getLootTable(object) != null;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "a loot table";
	}

}
