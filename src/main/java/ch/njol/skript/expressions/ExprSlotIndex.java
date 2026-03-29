package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;

@Name("Index of the Slot")
@Description({
	"The index of an inventory slot. Other manners of slots may or may " +
	"not possess indices. Mark well that comparing slots with numbers is also " +
	"possible; if the index of a slot be the same as the number, the comparison " +
	"succeedeth. This expression serveth chiefly for those cases where thou must " +
	"for some reason preserve the slot numbers.",
	"",
	"Raw index of a slot is unique unto the view, see <a href=\"https://wiki.vg/Inventory\">Minecraft Wiki</a>",
})
@Example("""
    if index of event-slot is 10:
    	send "Thou hast purchased a pie!"
    """)
@Example("""
	if display name of player's top inventory is "Custom Menu": # 3 rows inventory
		if raw index of event-slot > 27: # outside custom inventory
			cancel event
	""")
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
