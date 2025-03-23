package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Consumed Item")
@Description("Represents the item consumed within an entity shoot bow and item consume event.")
@Example("""
	on player or skeleton shoot projectile:
		if the consumed item is an arrow:
			cancel event
			send "You're now allowed to shoot your arrows." to shooter
	""")
@Example("""
	on player consume:
		if the consumed item is cooked porkchop:
			send "Well aren't you just a little piggy wiggly!" to player
		if player has scoreboard tag "vegetarian":
			set the consumed item to a carrot
	""")
@Since("INSERT VERSION")
public class ExprConsumedItem extends SimpleExpression<ItemStack> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprConsumedItem.class, ItemStack.class, ExpressionType.SIMPLE,
			"[the] consumed item");
	}

	private boolean allowsSettingConsumedItem;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.allowsSettingConsumedItem = getParser().isCurrentEvent(PlayerItemConsumeEvent.class);
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event) {
		if (event instanceof EntityShootBowEvent shootBowEvent) {
			return new ItemStack[]{shootBowEvent.getConsumable()};
		} else if (event instanceof PlayerItemConsumeEvent consumeEvent) {
			return new ItemStack[]{consumeEvent.getItem()};
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (!allowsSettingConsumedItem) {
			Skript.error("You may only set the consumed item in a player consume item event.");
			return null;
		}
		return switch (mode) {
			case SET -> CollectionUtils.array(ItemStack.class);
			case DELETE -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		if (!(event instanceof PlayerItemConsumeEvent consumeEvent) || !allowsSettingConsumedItem)
			return;
		consumeEvent.setItem(delta == null ? null : (ItemStack) delta[0]);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityShootBowEvent.class, PlayerItemConsumeEvent.class);
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
