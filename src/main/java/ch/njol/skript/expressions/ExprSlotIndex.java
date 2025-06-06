package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;

@Name("Slot Index")
@Description({
	"Index of an an inventory slot. Other types of slots may or may " +
	"not have indices. Note that comparing slots with numbers is also " +
	"possible; if index of slot is same as the number, comparison" +
	"succeeds. This expression is mainly for the cases where you must " +
	"for some reason save the slot numbers.",
	"",
	"Raw index of slot is unique for the view, see <a href=\"https://wiki.vg/Inventory\">Minecraft Wiki</a>",
})
@Examples({
	"if index of event-slot is 10:",
	"\tsend \"You bought a pie!\"",
	"",
	"if display name of player's top inventory is \"Custom Menu\": # 3 rows inventory",
	"\tif raw index of event-slot > 27: # outside custom inventory",
	"\t\tcancel event",
})
@Since("2.2-dev35, 2.8.0 (raw index)")
public class ExprSlotIndex extends SimplePropertyExpression<Slot, Long> {
	
	static {
		register(ExprSlotIndex.class, Long.class, "[raw:(raw|unique)] index", "slots");
	}

	private boolean isRaw;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isRaw = parseResult.hasTag("raw");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	@Nullable
	public Long convert(Slot slot) {
		if (slot instanceof SlotWithIndex) {
			SlotWithIndex slotWithIndex = (SlotWithIndex) slot;
			return (long) (isRaw ? slotWithIndex.getRawIndex() : slotWithIndex.getIndex());
		}

		return 0L; // Slot does not have index. At all
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "slot";
	}

}
