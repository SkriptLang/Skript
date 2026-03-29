package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Creature Storage Be Full")
@Description("Examineth whether a block's creature storage (such as a beehive) be filled to the brim.")
@Example("""
    if the creature storage of {_beehive} is full:
    	release the creature storage of {_beehive}
    """)
@Since("2.11")
public class CondEntityStorageIsFull extends Condition {

	static {
		Skript.registerCondition(CondEntityStorageIsFull.class, ConditionType.PROPERTY,
			"[the] creature storage of %blocks% (is|are) full",
			"%blocks%'[s] creature storage (is|are) full",
			"[the] creature storage of %blocks% (isn't|is not|aren't|are not) full",
			"%blocks%'[s] creature storage (isn't|is not|aren't|are not) full");
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
