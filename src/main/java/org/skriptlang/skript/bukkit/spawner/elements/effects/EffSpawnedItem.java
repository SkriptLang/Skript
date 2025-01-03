package org.skriptlang.skript.bukkit.spawner.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EffSpawnedItem extends Effect {

	static {
		var info = SyntaxInfo.builder(EffSpawnedItem.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EffSpawnedItem::new)
			.priority(SyntaxInfo.COMBINED)
			.addPattern("set [the] spawned item of %entities/blocks% to %itemtype%")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
	}

	private Expression<?> spawners;
	private Expression<ItemType> itemTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		spawners = exprs[0];
		//noinspection unchecked
		itemTypes = (Expression<ItemType>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemType itemType = itemTypes.getSingle(event);
		if (itemType == null)
			return;

		ItemStack item = itemType.getRandom();
		if (item == null)
			return;

		for (Object object : spawners.getArray(event)) {
			if (!SpawnerUtils.isSpawner(object))
				continue;

			Spawner spawner = SpawnerUtils.getAsSpawner(object);

			spawner.setSpawnedItem(item);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "set spawned item of " + spawners.toString(event, debug) + " to " + itemTypes.toString(event, debug);
	}

}
