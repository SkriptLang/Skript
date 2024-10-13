/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EvtMoveOn extends SkriptEvent {
	
	static {
		// Register EvtPressurePlate before EvtMoveOn, https://github.com/SkriptLang/Skript/issues/2555
		new EvtPressurePlate();

		Skript.registerEvent("Move On", EvtMoveOn.class, PlayerMoveEvent.class, "(step|walk)[ing] (on|over) %*itemtypes%")
			.description(
				"Called when a player moves onto a certain type of block.",
				"Please note that using this event can cause lag if there are many players online."
			).examples(
				"on walking on dirt or grass:",
				"on stepping on stone:"
			).since("2.0");
	}
	
	private static final Map<Material, List<Trigger>> ITEM_TYPE_TRIGGERS = new ConcurrentHashMap<>();
	
	private static final AtomicBoolean REGISTERED_EXECUTOR = new AtomicBoolean();

	private static final EventExecutor EXECUTOR = (listener, e) -> {
		PlayerMoveEvent event = (PlayerMoveEvent) e;
		Location from = event.getFrom(), to = event.getTo();

		if (!ITEM_TYPE_TRIGGERS.isEmpty()) {
			Block block = getOnBlock(to);
			if (block == null || ItemUtils.isAir(block.getType()))
				return;

			Material id = block.getType();
			List<Trigger> triggers = ITEM_TYPE_TRIGGERS.get(id);
			if (triggers == null)
				return;

			int y = getBlockY(to.getY(), block);
			if (to.getWorld().equals(from.getWorld()) && to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ()) {
				Block fromOnBlock = getOnBlock(from);
				if (fromOnBlock != null && y == getBlockY(from.getY(), fromOnBlock) && fromOnBlock.getType() == id)
					return;
			}

			SkriptEventHandler.logEventStart(event);
			for (Trigger trigger : triggers) {
				for (ItemType type : ((EvtMoveOn) trigger.getEvent()).types) {
					if (type.isOfType(block)) {
						SkriptEventHandler.logTriggerStart(trigger);
						trigger.execute(event);
						SkriptEventHandler.logTriggerEnd(trigger);
						break;
					}
				}
			}
			SkriptEventHandler.logEventEnd();
		}
	};

	@Nullable
	private static Block getOnBlock(Location location) {
		Block block = location.getWorld().getBlockAt(location.getBlockX(), (int) (Math.ceil(location.getY()) - 1), location.getBlockZ());
		if (block.getType() == Material.AIR && Math.abs((location.getY() - location.getBlockY()) - 0.5) < Skript.EPSILON) { // Fences
			block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
			if (!ItemUtils.isFence(block))
				return null;
		}
		return block;
	}
	
	private static int getBlockY(double y, Block block) {
		if (ItemUtils.isFence(block) && Math.abs((y - Math.floor(y)) - 0.5) < Skript.EPSILON)
			return (int) Math.floor(y) - 1;
		return (int) Math.ceil(y) - 1;
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private ItemType[] types;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		Literal<? extends ItemType> types = (Literal<? extends ItemType>) args[0];
		if (types == null)
			return false;
		this.types = types.getAll();

		for (ItemType type : this.types) {
			if (type.isAll()) {
				Skript.error("Can't use an 'on walk' event with an alias that matches all blocks");
				return false;
			}
			for (ItemData data : type) { // Check for illegal types
				if (!data.getType().isBlock() || ItemUtils.isAir(data.getType())) {
					Skript.error(type + " is not a block and can thus not be walked on");
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean postLoad() {
		Set<Material> materialSet = new HashSet<>();
		for (ItemType type : types) { // Get unique materials
			for (ItemData data : type)
				materialSet.add(data.getType());
		}

		for (Material material : materialSet)
			ITEM_TYPE_TRIGGERS.computeIfAbsent(material, k -> new ArrayList<>()).add(trigger);

		if (REGISTERED_EXECUTOR.compareAndSet(false, true)) {
			Bukkit.getPluginManager().registerEvent(
				PlayerMoveEvent.class, new Listener(){}, SkriptConfig.defaultEventPriority.value(), EXECUTOR, Skript.getInstance(), true
			);
		}

		return true;
	}

	@Override
	public void unload() {
		Iterator<Entry<Material, List<Trigger>>> iterator = ITEM_TYPE_TRIGGERS.entrySet().iterator();
		while (iterator.hasNext()) {
			List<Trigger> triggers = iterator.next().getValue();
			triggers.remove(trigger);
			if (triggers.isEmpty())
				iterator.remove();
		}
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "walk on " + Classes.toString(types, false);
	}

}
