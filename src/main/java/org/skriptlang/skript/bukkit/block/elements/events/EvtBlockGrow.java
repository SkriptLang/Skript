package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockGrowEvent;
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


public class EvtBlockGrow extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockGrow.class, "Block Growth")
			.supplier(EvtBlockGrow::new)
			.addEvent(BlockGrowEvent.class)
			.addPattern("(plant|crop|block) grow[(th|ing)] [[of] %-itemtypes/blockdatas%]")
			.addDescription("""
				Called when a crop grows.
				Alternative to new form of generic grow event.
				""")
			.addExample("""
				on crop growth:
					broadcast "IT GREW!"
				""")
			.addSince("2.2-Fixes-V10, INSERT VERSION (BlockData support)")
			.build());

		eventValueRegistry.register(EventValue.builder(BlockGrowEvent.class, Block.class)
			.getter(BlockEvent::getBlock)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockGrowEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getNewState()))
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
			.append("block grow")
			.appendIf(types != null, "of", types)
			.toString();
	}

}
