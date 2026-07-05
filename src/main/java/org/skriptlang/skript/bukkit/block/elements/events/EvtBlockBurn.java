package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.util.BlockUtils;
import org.bukkit.event.block.BlockBurnEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlockBurn extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockBurn.class, "Block Burn")
			.supplier(EvtBlockBurn::new)
			.addEvent(BlockBurnEvent.class)
			.addPattern("[block] burn[ing] [[of] %-itemtypes/blockdatas%]")
			.addDescription("Called when a block is destroyed by fire.")
			.addExample("""
				on burn:
				    broadcast "AHH IT BURNS!!"
				""")
			.addExample("""
				on burn of oak wood:
				    broadcast "Lets hope this wood is not a part of the house.."
				""")
			.addExample("""
				on burn of oak_log[axis=y]:
				    broadcast "Very specific.."
				""")
			.addSince("1.0, 2.6 (BlockData support)")
			.build());
	}

	private @Nullable Literal<Object> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			types = (Literal<Object>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		return BlockUtils.checkEvent(event, types);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("block burning")
			.appendIf(types != null,"of", types)
			.toString();
	}

}
