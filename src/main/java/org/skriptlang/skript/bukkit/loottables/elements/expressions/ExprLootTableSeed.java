package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Seed of Loot Table")
@Description("Returns the seed of a loot table. Setting the seed of a block or entity that does not have a loot table will not do anything.")
@Examples({
	"set {_seed} loot table seed of block",
	"set loot table seed of entity to 123456789"
})
@Since("2.10")
public class ExprLootTableSeed extends SimplePropertyExpression<Object, Long> {

	static {
		register(ExprLootTableSeed.class, Long.class, "loot[[ ]table] seed[s]", "entities/blocks");
	}

	@Override
	public @Nullable Long convert(Object object) {
		Lootable lootable = LootTableUtils.getAsLootable(object);
		return lootable != null ? lootable.getSeed() : null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		long seedValue = ((Number) delta[0]).longValue();

		for (Object object : getExpr().getArray(event)) {
			if (!LootTableUtils.isLootable(object))
				continue;

			Lootable lootable = LootTableUtils.getAsLootable(object);
			lootable.setSeed(seedValue);
			LootTableUtils.updateState(lootable);
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot table seed";
	}

}
