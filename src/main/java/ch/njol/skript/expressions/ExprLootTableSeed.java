package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

@Name("Loot Table Seed")
@Description("Returns the seed of a loot table.")
@Examples({
	"loot seed of loot table of block",
	"set loot table seed of loot table of block to 123456789"
})
@Since("INSERT VERSION")
public class ExprLootTableSeed extends SimplePropertyExpression<Object, Long> {

	static {
		register(ExprLootTableSeed.class, Long.class, "loot[[ ]table] seed[s]", "entities/blocks");
	}

	@Override
	public @Nullable Long convert(Object object) {
		if (object instanceof Lootable lootable)
			return lootable.getSeed();
		if (object instanceof Block block)
			return block.getState() instanceof Lootable lootable ? lootable.getSeed() : null;
		return null;
	}

	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block)
				object = block.getState();
			if (!(object instanceof Lootable lootable))
				return;

			Number seed = delta != null ? ((Number) delta[0]) : null;
			if (seed == null)
				return;

			lootable.setSeed(seed.longValue());

			if (lootable instanceof BlockState blockState)
				blockState.update();
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
