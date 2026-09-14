package org.skriptlang.skript.bukkit.block.sign.elements.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Has Glowing Text")
@Description("""
	Checks whether a sign (either a block or an item) has glowing text. \
	If a sign side is not specified, the front side will be used by default.
	""")
@Example("if target block has glowing text")
@Since({"2.8.0", "INSERT VERSION (sign side support)"})
public class CondGlowingText extends PropertyCondition<Object> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.CONDITION, SyntaxInfo.simple(CondGlowingText.class, CondGlowingText::new,
			Arrays.stream(getPatterns(PropertyType.HAVE, "glowing text", "blocks/itemtypes"))
				.map(pattern -> "[[the] (front|:back) [side[s]] of]" + pattern)
				.toArray(String[]::new)));
	}

	private Side side;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		side = parseResult.hasTag("back") ? Side.BACK : Side.FRONT;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Object obj) {
		BlockState blockState = switch (obj) {
			case Block block -> block.getState();
			case ItemType itemType when itemType.getItemMeta() instanceof BlockStateMeta blockStateMeta ->
				blockStateMeta.getBlockState();
			default -> null;
		};
		if (blockState instanceof Sign sign) {
			return sign.getSide(side).isGlowingText();
		}
		return false;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "glowing text";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (side == Side.FRONT ? "front" : "back") + " side of " + super.toString(event, debug);
	}

}
