package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.jetbrains.annotations.Nullable;

@Name("Barter Input")
@Description("The item picked up by the piglin in a piglin bartering event.")
@Examples({
	"on piglin barter:",
		"\tif the bartering input is a gold ingot:",
			"\t\tbroadcast \"my precious...\""
})
@Since("INSERT VERSION")
public class ExprBarterInput extends SimpleExpression<ItemType> {

	static {
		if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
			Skript.registerExpression(ExprBarterInput.class, ItemType.class,
					ExpressionType.SIMPLE, "[the] [piglin] barter[ing] input");
		}
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		if (!getParser().isCurrentEvent(PiglinBarterEvent.class)) {
			Skript.error("The expression 'barter input' can only be used in the piglin bartering event");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected ItemType[] get(Event event) {
		if (!(event instanceof PiglinBarterEvent))
			return null;

		return new ItemType[] { new ItemType(((PiglinBarterEvent) event).getInput()) };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the barter input";
	}

}
