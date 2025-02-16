package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Is Full")
@Description("Checks to see if the an entity block storage (i.e beehive) is full.")
@Examples({
	"if the entity storage of {_beehive} is full:",
		"\trelease the entity storage of {_beehive}"
})
@Since("INSERT VERSION")
public class CondEntityStorageIsFull extends Condition {

	static {
		Skript.registerCondition(CondEntityStorageIsFull.class, ConditionType.PROPERTY,
			"[the] entity storage of %blocks% (is|are) full",
			"%blocks%'[s] entity storage (is|are) full",
			"[the] entity storage of %blocks% (isn't|is not|aren't|are not) full",
			"%blocks%'[s] entity storage (isn't|is not|aren't|are not) full");
	}

	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exrps, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern >= 2);
		//noinspection unchecked
		blocks = (Expression<Block>) exrps[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return blocks.check(event, block -> {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				return false;
			return blockStorage.isFull();
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the entity storage of", blocks);
		if (blocks.isSingle()) {
			builder.append("is");
		} else {
			builder.append("are");
		}
		if (isNegated())
			builder.append("not");
		builder.append("full");
		return builder.toString();
	}

}
