package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
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

public class EvtBlockDrop extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockDrop.class, "Block Drop")
			.supplier(EvtBlockDrop::new)
			.addEvent(BlockDropItemEvent.class)
			.addPattern("block drop[ping] [[of] %-itemtypes/blockdatas%]")
			.addDescription("""
				Called when a block broken by a player drops something.
				Note that if breaking of the block leads to others being broken,\s
				such as torches, they will appear in the event-items and event-entities.
				""")
			.addExample("""
				on block drop:
					send "Nice %event-item stacks% you got %player%!" to player
				""")
			.addExample("""
				on block drop of oak log:
					chance of 42%:
						kill event-player
						send "Well you got unlucky and a black hole dropped from %past event-block% instead.."
				""")
			.addSince("2.10")
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDropItemEvent.class, Block.class)
			.getter(event -> new BlockStateBlock(event.getBlockState()))
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDropItemEvent.class, Player.class)
			.getter(BlockDropItemEvent::getPlayer)
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDropItemEvent.class, ItemStack[].class)
			.getter(event -> event.getItems().stream().map(Item::getItemStack).toArray(ItemStack[]::new))
			.build());

		eventValueRegistry.register(EventValue.builder(BlockDropItemEvent.class, Entity[].class)
			.getter(event -> event.getItems().toArray(Entity[]::new))
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
			.append("block drop")
			.appendIf(types != null, "of", types)
			.toString();
	}

}
