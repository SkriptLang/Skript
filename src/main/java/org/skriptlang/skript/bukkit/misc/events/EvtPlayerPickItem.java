package org.skriptlang.skript.bukkit.misc.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerPickItem extends SkriptEvent {

	private static final Patterns<PickType> PATTERNS = new Patterns<>(new Object[][]{
		{"[player] pick[ing] [of] [an|any] item", PickType.ANY},
		{"[player] pick[ing] [of] [a|any] block", PickType.BLOCK},
		{"[player] pick[ing] [of] [an|any] entity", PickType.ENTITY},
		{"[player] pick[ing] [of] %entitydata/itemtype/blockdata%", null}
	});

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerPickItem.class, "Player Pick Item")
			.supplier(EvtPlayerPickItem::new)
			.origin(origin)
			.addEvent(PlayerPickBlockEvent.class)
			.addEvent(PlayerPickEntityEvent.class)
			.addPatterns(PATTERNS.getPatterns())
			.addDescription("Called when a player picks an item, block or an entity" + 
					" using the pick block key (default middle mouse button).",
				"The past event-slot represents the slot containing the item that will be put into the players hotbar," +
					" or nothing, if the item is not in the inventory.",
				"The event-slot represents the slot in the hotbar where the picked item will be placed.",
				"Both event-slots may be set to new slots.")
			.addExample("""
				on player picking a diamond block:
					cancel event
					send "You cannot pick diamond blocks!" to the player
				""")
			.addSince("INSERT VERSION")
			.addRequiredPlugin("1.21.5+")
			.build());

		EventValues.registerEventValue(PlayerPickItemEvent.class, Slot.class, new EventConverter<>() {
			@Override
			public void set(PlayerPickItemEvent event, @Nullable Slot slot) {
				if (!(slot instanceof InventorySlot inventorySlot) || inventorySlot.getInventory() != event.getPlayer().getInventory())
					return;
				event.setSourceSlot(inventorySlot.getIndex());
			}

			@Override
			public @Nullable Slot convert(PlayerPickItemEvent event) {
				int source = event.getSourceSlot();
				if (source == -1)
					return null;
				return new InventorySlot(event.getPlayer().getInventory(), source);
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerPickItemEvent.class, Slot.class, new EventConverter<>() {
			@Override
			public void set(PlayerPickItemEvent event, @Nullable Slot slot) {
				if (!(slot instanceof InventorySlot inventorySlot) || inventorySlot.getInventory() != event.getPlayer().getInventory())
					return;
				event.setTargetSlot(inventorySlot.getIndex());
			}

			@Override
			public Slot convert(PlayerPickItemEvent event) {
				return new InventorySlot(event.getPlayer().getInventory(), event.getTargetSlot());
			}
		});
	}

	private @Nullable PickType pickType;
	private @Nullable Literal<?> type;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		pickType = PATTERNS.getInfo(matchedPattern);
		if (pickType == null)
			type = args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (pickType != null) {
			return switch (pickType) {
				case ANY -> true;
				case BLOCK -> event instanceof PlayerPickBlockEvent;
				case ENTITY -> event instanceof PlayerPickEntityEvent;
			};
		}

		Block pickedBlock;
		Entity pickedEntity;
		if (event instanceof PlayerPickBlockEvent pickBlockEvent) {
			pickedBlock = pickBlockEvent.getBlock();
			pickedEntity = null;
		} else if (event instanceof PlayerPickEntityEvent pickEntityEvent) {
			pickedEntity = pickEntityEvent.getEntity();
			pickedBlock = null;
		} else {
			assert false;
			return false;
		}
		assert type != null;
		return type.check(event, object -> switch (object) {
			case EntityData<?> entityData when pickedEntity != null -> entityData.isInstance(pickedEntity);
			case ItemType itemType when pickedEntity != null -> {
				Relation comparison = DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(pickedEntity), itemType);
				yield Relation.EQUAL.isImpliedBy(comparison);
			}
			case ItemType itemType -> itemType.isOfType(pickedBlock);
			case BlockData blockData when pickedBlock != null -> pickedBlock.getBlockData().matches(blockData);
			default -> false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("player picking");
		if (pickType != null) {
			switch (pickType) {
				case ANY -> builder.append("an item");
				case BLOCK -> builder.append("a block");
				case ENTITY -> builder.append("an entity");
			}
		} else if (type != null) {
			builder.append(type);
		}
		return builder.toString();
	}

	private enum PickType {
		ANY,
		BLOCK,
		ENTITY,
	}

}
