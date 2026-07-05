package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.DelayedChangeBlock;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFadeEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlockFade extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockFade.class, "Block Fade")
			.supplier(EvtBlockFade::new)
			.addEvent(BlockFadeEvent.class)
			.addPattern("[block] fad(e|ing) [[of] %-itemtypes/blockdatas%]")
			.addDescription("Called when a block 'fades away', e.g. ice or snow melts.")
			.addExample("""
				on block fade of snow or blue ice:
				    broadcast "Great just what we need: Less of %past event-block%.."
				""")
			.addExample("""
				on block fade of snow[layers=2]:
				    set event-block to lava
				    broadcast "Now its getting hot!"
				""")
			.addSince("1.0, 2.6 (BlockData support)")
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(BlockFadeEvent::getBlock)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(event -> new DelayedChangeBlock(event.getBlock(), event.getNewState()))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockFadeEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getNewState()))
			.time(Time.FUTURE)
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
			.append("block fade")
			.appendIf(types != null, "of", types)
			.toString();
	}

}
