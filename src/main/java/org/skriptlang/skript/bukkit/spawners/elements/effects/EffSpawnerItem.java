package org.skriptlang.skript.bukkit.spawners.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Modify Spawner Item")
@Description("""
	Make a mob spawner spawn items rather than entities. In this case, the mob spawner's spawn count determines \
	how many stacks are spawned, not how many items should be in each stack.
	""")
@Example("""
	make event-block spawn 15 diamonds
	modify the mob spawner data of event-block:
		set the spawn count to 3
	# This will now spawn 3 stacks of 15 diamonds each
	""")
@Example("""
	force event-block to spawn 5 diamonds
	force event-block to spawn a diamond sword
	""")
public class EffSpawnerItem extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSpawnerItem.class)
			.supplier(EffSpawnerItem::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"make %blocks/entities% spawn %itemstack%",
				"force %blocks/entities% to spawn %itemstack%")
			.build()
		);
	}

	private Expression<?> spawners;
	private Expression<ItemStack> itemStack;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		spawners = exprs[0];
		//noinspection unchecked
		itemStack = (Expression<ItemStack>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemStack item = itemStack.getSingle(event);
		if (item == null)
			return;

		for (Object object : spawners.getArray(event)) {
			if (!SpawnerUtils.isMobSpawner(object))
				continue;

			Spawner mobSpawner = SpawnerUtils.getMobSpawner(object);
			mobSpawner.setSpawnedItem(item);

			if (mobSpawner instanceof CreatureSpawner creatureSpawner)
				creatureSpawner.update(true, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + spawners.toString(event, debug) + " to spawn " + itemStack.toString(event, debug);
	}

}
