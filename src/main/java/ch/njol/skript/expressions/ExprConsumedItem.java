package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Consumed Item")
@Description("Represents the item consumed within an entity shoot bow event.")
@Example("""
	on player shoot bow:
		if the consumed item is an arrow:
			cancel event
			send "You're now allowed to shoot your arrows." to shooter
	""")
@Since("INSERT VERSION")
public class ExprConsumedItem extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprConsumedItem.class, ItemStack.class, ExpressionType.SIMPLE,
			"[the] consumed item");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event) {
		if (!(event instanceof EntityShootBowEvent shootBowEvent))
			return null;
		return new ItemStack[]{shootBowEvent.getConsumable()};
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityShootBowEvent.class);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the consumed item";
	}

}
