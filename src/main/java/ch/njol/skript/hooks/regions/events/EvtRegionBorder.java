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
package ch.njol.skript.hooks.regions.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EvtRegionBorder extends SkriptEvent {

	static {
		Skript.registerEvent("Region Enter/Leave", EvtRegionBorder.class, RegionBorderEvent.class,
				"(:enter[ing]|leav(e|ing)|exit[ing]) [of] ([a] region|[[the] region] %-regions%)",
				"region (:enter[ing]|leav(e|ing)|exit[ing])")
				.description(
					"Called when a player enters or leaves a <a href='./classes.html#region'>region</a>.",
					"This event requires a supported regions plugin to be installed."
				).examples(
					"on region exit:",
					"\tmessage \"Leaving %region%.\""
				).since("2.1")
				.requiredPlugins("Supported regions plugin");
		EventValues.registerEventValue(RegionBorderEvent.class, Region.class, new Getter<Region, RegionBorderEvent>() {
			@Override
			public Region get(RegionBorderEvent e) {
				return e.getRegion();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(RegionBorderEvent.class, Player.class, new Getter<Player, RegionBorderEvent>() {
			@Override
			public Player get(RegionBorderEvent e) {
				return e.getPlayer();
			}
		}, EventValues.TIME_NOW);
	}

	// Even WorldGuard doesn't have events, and this way all region plugins are supported for sure.
	private final static EventExecutor EXECUTOR = new EventExecutor() {
		@Nullable
		Event last = null;

		@Override
		public void execute(@Nullable Listener listener, Event event) {
			if (event == last)
				return;
			last = event;

			PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
			Location to = moveEvent.getTo();
			Location from = moveEvent.getFrom();

			if (to.equals(from))
				return;

			Set<? extends Region> oldRegions = RegionsPlugin.getRegionsAt(from);
			Set<? extends Region> newRegions = RegionsPlugin.getRegionsAt(to);

			for (Region oldRegion : oldRegions) {
				if (!newRegions.contains(oldRegion))
					callEvent(oldRegion, moveEvent, false);
			}

			for (Region newRegion : newRegions) {
				if (!oldRegions.contains(newRegion))
					callEvent(newRegion, moveEvent, true);
			}
		}
	};

	private static void callEvent(Region region, PlayerMoveEvent event, boolean enter) {
		RegionBorderEvent regionEvent = new RegionBorderEvent(region, event.getPlayer(), enter);
		regionEvent.setCancelled(event.isCancelled());
		synchronized (TRIGGERS) {
			for (Trigger trigger : TRIGGERS) {
				if (((EvtRegionBorder) trigger.getEvent()).applies(regionEvent))
					trigger.execute(regionEvent);
			}
		}
		event.setCancelled(regionEvent.isCancelled());
	}

	private static final List<Trigger> TRIGGERS = Collections.synchronizedList(new ArrayList<>());

	private static final AtomicBoolean REGISTERED_EXECUTORS = new AtomicBoolean();
	
	private boolean enter;
	
	@Nullable
	private Literal<Region> regions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		enter = parseResult.hasTag("enter");
		regions = args.length == 0 ? null : (Literal<Region>) args[0];
		return true;
	}

	@Override
	public boolean postLoad() {
		TRIGGERS.add(trigger);
		if (REGISTERED_EXECUTORS.compareAndSet(false, true)) {
			EventPriority priority = SkriptConfig.defaultEventPriority.value();
			Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new Listener(){}, priority, EXECUTOR, Skript.getInstance(), true);
			Bukkit.getPluginManager().registerEvent(PlayerTeleportEvent.class, new Listener(){}, priority, EXECUTOR, Skript.getInstance(), true);
			Bukkit.getPluginManager().registerEvent(PlayerPortalEvent.class, new Listener(){}, priority, EXECUTOR, Skript.getInstance(), true);
		}
		return true;
	}

	@Override
	public void unload() {
		TRIGGERS.remove(trigger);
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
		return (enter ? "enter" : "leave") + " of " + (regions == null ? "a region" : regions.toString(event, debug));
	}
	
	private boolean applies(RegionBorderEvent event) {
		if (enter != event.isEntering())
			return false;
		if (regions == null)
			return true;
		Region region = event.getRegion();
		return regions.check(event, r -> r.equals(region));
	}
	
}
