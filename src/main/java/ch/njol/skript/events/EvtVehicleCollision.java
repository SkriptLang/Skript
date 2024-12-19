package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EvtVehicleCollision extends SkriptEvent {

	static {
		Skript.registerEvent("Vehicle Collision", EvtVehicleCollision.class, new Class[]{VehicleBlockCollisionEvent.class, VehicleEntityCollisionEvent.class},
				"vehicle collision [(with|of) [a[n]] %-itemtypes/blockdatas/entitydatas%]",
				"vehicle block collision [(with|of) [a[n]] %-itemtypes/blockdatas%]",
				"vehicle entity collision [(with|of) [a[n]] %-entitydatas%]")
				.description("Called when a vehicle collides with a block or entity.")
				.examples("on vehicle collision:", "on vehicle collision with obsidian:", "on vehicle collision with a zombie:")
				.since("INSERT VERSION");

		// VehicleBlockCollisionEvent
		EventValues.registerEventValue(VehicleBlockCollisionEvent.class, Block.class, new Getter<>() {
			@Override
			public Block get(VehicleBlockCollisionEvent event) {
				return event.getBlock();
			}
		}, EventValues.TIME_NOW);

		// VehicleEntityCollisionEvent
		EventValues.registerEventValue(VehicleEntityCollisionEvent.class, Entity.class, new Getter<>() {
			@Override
			public Entity get(VehicleEntityCollisionEvent event) {
				return event.getEntity();
			}
		}, EventValues.TIME_NOW);
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
		if (!(event instanceof VehicleCollisionEvent collisionEvent))
			return false;

		if (expr == null) {
			if (blockCollision && !(event instanceof VehicleBlockCollisionEvent)) {
				return false;
			} else if (entityCollision && !(event instanceof VehicleEntityCollisionEvent)) {
				return false;
			}
			return true;
		}

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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("vehicle");
		if (blockCollision) {
			builder.append("block");
		} else if (entityCollision) {
			builder.append("entity");
		}
		builder.append("collision");
		if (expr != null)
			builder.append("of", expr);
		return builder.toString();
	}

}
