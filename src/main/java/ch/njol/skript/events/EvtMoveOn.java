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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


public class EvtMoveOn extends SelfRegisteringSkriptEvent { // TODO on jump
	
	static {
		// Register EvtPressurePlate before EvtMoveOn, https://github.com/SkriptLang/Skript/issues/2555
		new EvtPressurePlate();
		Skript.registerEvent("Move On", EvtMoveOn.class, PlayerMoveEvent.class, "(step|walk)[ing] (on|over) %*itemtypes%")
				.description("Called when a player moves onto a certain type of block. Please note that using this event can cause lag if there are many players online.")
				.examples("on walking on dirt or grass:", "on stepping on stone:")
				.since("2.0");
	}
	
	/**
	 * Actual fence blocks and fence gates.
	 */
	private static final ItemType FENCE_PART = Aliases.javaItemType("fence part");
	
	private static final HashMap<Material, List<Trigger>> ITEM_TYPE_TRIGGERS = new HashMap<>();

	@SuppressWarnings("ConstantConditions")
	private ItemType[] types = null;
	
	private static boolean registeredExecutor = false;
	private static final EventExecutor executor = (l, event) -> {
		PlayerMoveEvent e = (PlayerMoveEvent) event;
		Location from = e.getFrom(), to = e.getTo();

		if (!ITEM_TYPE_TRIGGERS.isEmpty()) {
			Block block = getOnBlock(to);
			if (block == null || block.getType() == Material.AIR)
				return;
			Material id = block.getType();
			List<Trigger> ts = ITEM_TYPE_TRIGGERS.get(id);
			if (ts == null)
				return;
			int y = getBlockY(to.getY(), id);
			if (to.getWorld().equals(from.getWorld()) && to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ()
					&& y == getBlockY(from.getY(), getOnBlock(from).getType()) && getOnBlock(from).getType() == id)
				return;

			SkriptEventHandler.logEventStart(e);
			triggersLoop: for (Trigger t : ts) {
				EvtMoveOn se = (EvtMoveOn) t.getEvent();
				for (ItemType i : se.types) {
					if (i.isOfType(block)) {
						SkriptEventHandler.logTriggerStart(t);
						t.execute(e);
						SkriptEventHandler.logTriggerEnd(t);
						continue triggersLoop;
					}
				}
			}
			SkriptEventHandler.logEventEnd();
		}
	};

	@Nullable
	private static Block getOnBlock(Location l) {
		Block block = l.getWorld().getBlockAt(l.getBlockX(), (int) (Math.ceil(l.getY()) - 1), l.getBlockZ());
		if (block.getType() == Material.AIR && Math.abs((l.getY() - l.getBlockY()) - 0.5) < Skript.EPSILON) { // Fences
			block = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ());
			if (!FENCE_PART.isOfType(block))
				return null;
		}
		return block;
	}
	
	private static int getBlockY(double y, Material id) {
		if (FENCE_PART.isOfType(id) && Math.abs((y - Math.floor(y)) - 0.5) < Skript.EPSILON)
			return (int) Math.floor(y) - 1;
		return (int) Math.ceil(y) - 1;
	}
	
	public static Block getBlock(PlayerMoveEvent e) {
		return e.getTo().clone().subtract(0, 0.5, 0).getBlock();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		Literal<? extends ItemType> l = (Literal<? extends ItemType>) args[0];
		if (l == null)
			return false;
		types = l.getAll();
		for (ItemType t : types) {
			if (t.isAll()) {
				Skript.error("Can't use an 'on walk' event with an alias that matches all blocks");
				return false;
			}
			boolean hasBlock = false;
			for (ItemData d : t) {
				if (d.getType().isBlock() && d.getType() != Material.AIR) // don't allow air
					hasBlock = true;
			}
			if (!hasBlock) {
				Skript.error(t + " is not a block and can thus not be walked on");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void register(Trigger trigger) {
		Set<Material> materialSet = new HashSet<>();
		for (ItemType t : types) {
			for (ItemData d : t) {
				if (!d.getType().isBlock())
					continue;
				materialSet.add(d.getType());
			}
		}

		for (Material material : materialSet) {
			List<Trigger> ts = ITEM_TYPE_TRIGGERS.computeIfAbsent(material, k -> new ArrayList<>());
			ts.add(trigger);
		}

		if (!registeredExecutor) {
			Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new Listener() {}, SkriptConfig.defaultEventPriority.value(), executor, Skript.getInstance(), true);
			registeredExecutor = true;
		}
	}

	@Override
	public void unregister(Trigger t) {
		Iterator<Entry<Material, List<Trigger>>> i2 = ITEM_TYPE_TRIGGERS.entrySet().iterator();
		while (i2.hasNext()) {
			List<Trigger> ts = i2.next().getValue();
			ts.remove(t);
			if (ts.isEmpty())
				i2.remove();
		}
	}

	@Override
	public void unregisterAll() {
		ITEM_TYPE_TRIGGERS.clear();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "walk on " + Classes.toString(types, false);
	}

}
