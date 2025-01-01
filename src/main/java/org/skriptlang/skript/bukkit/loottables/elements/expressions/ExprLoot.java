package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.inventory.ItemStack;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.world.LootGenerateEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Loot")
@Description("The loot that will be generated in a 'loot generate' event.")
@Examples({
	"on loot generate:",
		"\tchance of %10",
		"\tadd 64 diamonds to loot",
		"\tsend \"You hit the jackpot!!\""
})
@Since("2.7")
@RequiredPlugins("MC 1.16+")
public class ExprLoot extends SimpleExpression<ItemStack> {

	static {
		Skript.registerExpression(ExprLoot.class, ItemStack.class, ExpressionType.SIMPLE, "[the] loot");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(LootGenerateEvent.class)) {
			Skript.error("The 'loot' expression can only be used in a 'loot generate' event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemStack @Nullable [] get(Event event) {
		if (!(event instanceof LootGenerateEvent lootEvent))
			return new ItemStack[0];
		return lootEvent.getLoot().toArray(new ItemStack[0]);
	}

	@Override
	@Nullable
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case DELETE, ADD, REMOVE, SET -> CollectionUtils.array(ItemStack[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof LootGenerateEvent lootEvent))
			return;

		List<ItemStack> items = null;
		if (delta != null) {
			items = new ArrayList<>(delta.length);
			for (Object item : delta)
				items.add((ItemStack) item);
		}

		switch (mode) {
			case ADD -> lootEvent.getLoot().addAll(items);
			case REMOVE -> lootEvent.getLoot().removeAll(items);
			case SET -> lootEvent.setLoot(items);
			case DELETE -> lootEvent.getLoot().clear();
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the loot";
	}

}
