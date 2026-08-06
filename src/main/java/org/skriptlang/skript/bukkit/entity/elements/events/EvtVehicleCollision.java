package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

public class EvtVehicleCollision extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtVehicleCollision.class, "Vehicle Collision")
			.supplier(EvtVehicleCollision::new)
			.addEvents(CollectionUtils.array(VehicleBlockCollisionEvent.class, VehicleEntityCollisionEvent.class))
			.addPatterns(
				"vehicle collision [(with|of) [a[n]] %-itemtypes/blockdatas/entitydatas%]",
				"vehicle block collision [(with|of) [a[n]] %-itemtypes/blockdatas%]",
				"vehicle entity collision [(with|of) [a[n]] %-entitydatas%]"
			)
			.addDescription("Called when a vehicle collides with a block or entity.")
			.addExample("""
				on vehicle collision:
					broadcast "COLLISION!"
				""")
			.addExample("""
				on vehicle collision with obsidian:
					broadcast "Looks like something hard was hit.."
				""")
			.addExample("""
				on vehicle collision with a zombie:
					broadcast "How dare you hit the undead!"
				""")
			.addSince("2.10")
			.build());

		eventValueRegistry.register(EventValue.builder(VehicleBlockCollisionEvent.class, Block.class)
			.getter(VehicleBlockCollisionEvent::getBlock)
			.build());

		eventValueRegistry.register(EventValue.builder(VehicleEntityCollisionEvent.class, Entity.class)
			.getter(VehicleEntityCollisionEvent::getEntity)
			.build());
	}

	private Literal<?> expr;
	private boolean blockCollision;
	private boolean entityCollision;
	private final List<ItemType> itemTypes = new ArrayList<>();
	private final List<BlockData> blockDatas = new ArrayList<>();
	private final List<EntityData<?>> entityDatas = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			expr = args[0];
			for (Object object : expr.getAll()) {
				if (object instanceof ItemType itemType) {
					itemTypes.add(itemType);
				} else if (object instanceof BlockData blockData) {
					blockDatas.add(blockData);
				} else if (object instanceof EntityData<?> entityData) {
					entityDatas.add(entityData);
				}
			}
		}
		blockCollision = matchedPattern == 1;
		entityCollision = matchedPattern == 2;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (expr == null) {
			if (blockCollision && !(event instanceof VehicleBlockCollisionEvent)) {
				return false;
			} else return !entityCollision || event instanceof VehicleEntityCollisionEvent;
		}

		VehicleCollisionEvent collisionEvent = (VehicleCollisionEvent) event;

		if (collisionEvent instanceof VehicleBlockCollisionEvent blockCollisionEvent && (!itemTypes.isEmpty() || !blockDatas.isEmpty())) {
			Block eventBlock = blockCollisionEvent.getBlock();
			ItemType eventItemType = new ItemType(eventBlock.getType());
			BlockData eventBlockData = eventBlock.getBlockData();
			for (ItemType itemType : itemTypes) {
				if (itemType.isSupertypeOf(eventItemType))
					return true;
			}
			for (BlockData blockData : blockDatas) {
				if (blockData.matches(eventBlockData))
					return true;
			}
		} else if (collisionEvent instanceof VehicleEntityCollisionEvent entityCollisionEvent && !entityDatas.isEmpty()) {
			EntityData<?> eventEntityData = EntityData.fromEntity(entityCollisionEvent.getEntity());
			for (EntityData<?> entityData : entityDatas) {
				if (entityData.isSupertypeOf(eventEntityData))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("vehicle")
			.appendIf(blockCollision, "block")
			.appendIf(entityCollision, "entity")
			.append("collision")
			.appendIf(expr != null, "of", expr)
			.toString();
	}

}
