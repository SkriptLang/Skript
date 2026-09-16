package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.BlockStateBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerFillBucket extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerFillBucket.class, "Player Fill Bucket")
			.supplier(EvtPlayerFillBucket::new)
			.addEvent(PlayerBucketFillEvent.class)
			.addPatterns("bucket fill[ing] [liquid: with %-itemtype%]",
				"[player] fill[ing] [a] bucket [liquid: (with|of) %-itemtype%]")
			.addDescription("""
				Called when a player fills a bucket.
				""")
			.addExample("""
				on player filling a bucket:
				""")
			.addExample("""
				on player filling a bucket with water:
					broadcast "thirsty eh?"
				""")
			.addSince("1.0, INSERT VERSION (with itemtype)")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBucketFillEvent.class, Block.class)
			.getter(event -> event.getBlockClicked().getRelative(event.getBlockFace()))
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerBucketFillEvent.class, Block.class)
			.getter(event -> {
				BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
				state.setType(event.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
				return new BlockStateBlock(state, true);
			})
			.build());
	}

	private ItemType liquid;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("liquid")) {
			Literal<ItemType> liquidLiteral = (Literal<ItemType>) args[0];
			liquid = liquidLiteral.getSingle();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (liquid == null)
			return true;
		PlayerBucketFillEvent playerEvent = (PlayerBucketFillEvent) event;
		ItemStack item = playerEvent.getItemStack();
		if (item == null)
			return false;
		return liquid.getBlockType() == item.getType().asBlockType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("player filling a bucket")
			.appendIf(liquid != null, "with", liquid)
			.toString();
	}

}
