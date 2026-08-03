package org.skriptlang.skript.bukkit.block.sign.elements.effects;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Sign Glow")
@Description("""
	Makes a sign (either a block or item) have glowing text or normal text. \
	If a sign side is not specified, the front side will be used by default.
	""")
@Example("make target block of player have glowing text")
@Since({"2.8.0", "INSERT VERSION (sign side support)"})
public class EffGlowingText extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.simple(EffGlowingText.class, EffGlowingText::new,
			"make [[the] (front|:back) [side[s]] of] %blocks/itemtypes% have glowing text",
			"make [[the] (front|:back) [side[s]] of] %blocks/itemtypes% have (normal|non[-| ]glowing) text"));
	}

	private Side side;
	private Expression<?> objects;
	private boolean glowing;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		side = parseResult.hasTag("back") ? Side.BACK : Side.FRONT;
		objects = expressions[0];
		glowing = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			if (object instanceof Block block) {
				BlockState state = block.getState();
				if (state instanceof Sign sign) {
					sign.getSide(side).setGlowingText(glowing);
					state.update();
				}
			} else if (object instanceof ItemType itemType) {
				if (!(itemType.getItemMeta() instanceof BlockStateMeta blockStateMeta) ||
					!(blockStateMeta.getBlockState() instanceof Sign sign)) {
					continue;
				}
				sign.getSide(side).setGlowingText(glowing);
				sign.update();
				blockStateMeta.setBlockState(sign);
				itemType.setItemMeta(blockStateMeta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + objects.toString(event, debug) + " have " + (glowing ? "glowing text" : "normal text");
	}

}
