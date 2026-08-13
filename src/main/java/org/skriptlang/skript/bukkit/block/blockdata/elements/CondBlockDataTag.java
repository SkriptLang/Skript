package org.skriptlang.skript.bukkit.block.blockdata.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataHolder;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataTag;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Has Block Data")
@Description("Whether the blockdata of a block or block related object has the specified tag.")
@Example("""
	if {_block} has blockdata "waterlogged":
		set the blockdata "waterlogged" of {_block} to true
	""")
@Example("""
	if {_stairs} is tagged with "facing" blockdata:
		set the blockdata tag "facing" of {_stairs} to "north"
	""")
@Since("INSERT VERSISON")
public class CondBlockDataTag extends Condition {

	public static void register(SyntaxRegistry registry) {
		String types = "%" + BlockDataHolder.PLURAL_PATTERN_TYPES + "%";
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.simple(
			CondBlockDataTag.class,
			CondBlockDataTag::new,
			types + " (has|have) [the] block[ ]data [tag[s]] %strings%",
			types + " (is|are) tagged with [the] block[ ]data [tag[s]] %strings%",
			types + "(is|are) tagged with %strings% block[ ]data",
			types + " (does not|doesn't|do not| don't) have [the] block[ ]data [tag[s]] %strings%",
			types + " (is not|isn't|are not|aren't) tagged with [the] block[ ]data [tag[s]] %strings%",
			types + " (is not|isn't|are not|aren't) tagged with %strings% block[ ]data"
		));
	}

	private Expression<?> objects;
	private Expression<String> strings;
	private boolean negate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		//noinspection unchecked
		strings = (Expression<String>) exprs[1];
		negate = matchedPattern >= 3;
		return true;
	}

	@Override
	public boolean check(Event event) {
		String[] strings = this.strings.getArray(event);
		return objects.check(event, object -> {
			BlockDataHolder holder = BlockDataHolder.getHolder(object);
			if (holder == null)
				return false;
			//noinspection unchecked
			BlockDataTag[] tags = BlockDataTag.of(holder.getBlockData(object), strings);
			if (tags == null)
				return negate;
			return SimpleExpression.check(strings, string -> Arrays.stream(tags).anyMatch(tag -> tag.getKey().equalsIgnoreCase(string)),
				negate, this.strings.getAnd());
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(objects)
			.append(objects.isSingle() ? "does" : "do")
			.appendIf(negate, "not")
			.append("have blockdata tags", strings)
			.toString();
	}

}
