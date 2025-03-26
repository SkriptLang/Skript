package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Barter Drops")
@Description("The items dropped by the piglin in a piglin bartering event.")
@Examples({
	"on piglin barter:",
		"\tif the bartering drops contain a jack-o-lantern:",
			"\t\tremove jack-o-lantern from bartering output",
			"\t\tbroadcast \"it's not halloween yet!\""
})
@Since("2.10")
public class ExprBarterDrops extends SimpleExpression<ItemType> {
	
	static {
		if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
			Skript.registerExpression(ExprBarterDrops.class, ItemType.class,
					ExpressionType.SIMPLE, "[the] [piglin] barter[ing] drops");
		}
	}

	private Kleenean delay;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		if (!getParser().isCurrentEvent(PiglinBarterEvent.class)) {
			Skript.error("The expression 'barter drops' can only be used in the piglin bartering event");
			return false;
		}

		delay = isDelayed;

		return true;
	}
	
	@Override
	@Nullable
	protected ItemType[] get(Event event) {
		if (!(event instanceof PiglinBarterEvent))
			return null;

		return ((PiglinBarterEvent) event).getOutcome()
			.stream()
			.map(ItemType::new)
			.toArray(ItemType[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (!delay.isFalse()) {
			Skript.error("Can't change the piglin bartering drops after the event has already passed");
			return null;
		}

		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case DELETE:
				return CollectionUtils.array(ItemType[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof PiglinBarterEvent))
			return;

		List<ItemStack> outcome = ((PiglinBarterEvent) event).getOutcome();

		switch (mode) {
			case SET:
				outcome.clear();
			case ADD:
				for (Object item : delta) {
					((ItemType) item).addTo(outcome);
				}
				break;
			case REMOVE:
				for (Object item : delta) {
					((ItemType) item).removeFrom(false, outcome);
				}
				break;
			case REMOVE_ALL:
				for (Object item : delta) {
					((ItemType) item).removeAll(false, outcome);
				}
				break;
			case DELETE:
				outcome.clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the barter drops";
	}

}
