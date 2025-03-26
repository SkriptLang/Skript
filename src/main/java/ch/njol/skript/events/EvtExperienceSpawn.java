package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Experience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EvtExperienceSpawn extends SkriptEvent {

	static {
		Skript.registerEvent("Experience Spawn", EvtExperienceSpawn.class, ExperienceSpawnEvent.class,
				"[e]xp[erience] [orb] spawn",
				"spawn of [a[n]] [e]xp[erience] [orb]"
			).description(
				"Called whenever experience is about to spawn.",
				"Please note that this event will not fire for xp orbs spawned by plugins (including Skript) with Bukkit."
			).examples(
				"on xp spawn:",
				"\tworld is \"minigame_world\"",
				"\tcancel event"
			).since("2.0");
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Location.class, ExperienceSpawnEvent::getLocation);
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Experience.class, event -> new Experience(event.getSpawnedXP()));
	}

	private static final List<Trigger> TRIGGERS = Collections.synchronizedList(new ArrayList<>());

	private static final AtomicBoolean REGISTERED_EXECUTORS = new AtomicBoolean();

	private static final EventExecutor EXECUTOR = (listener, event) -> {
		ExperienceSpawnEvent experienceEvent;
		if (event instanceof BlockExpEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				((BlockExpEvent) event).getExpToDrop(),
				((BlockExpEvent) event).getBlock().getLocation().add(0.5, 0.5, 0.5)
			);
		} else if (event instanceof EntityDeathEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				((EntityDeathEvent) event).getDroppedExp(),
				((EntityDeathEvent) event).getEntity().getLocation()
			);
		} else if (event instanceof ExpBottleEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				((ExpBottleEvent) event).getExperience(),
				((ExpBottleEvent) event).getEntity().getLocation()
			);
		} else if (event instanceof PlayerFishEvent) {
			if (((PlayerFishEvent) event).getState() != PlayerFishEvent.State.CAUGHT_FISH) // There is no EXP
				return;
			experienceEvent = new ExperienceSpawnEvent(
				((PlayerFishEvent) event).getExpToDrop(),
				((PlayerFishEvent) event).getPlayer().getLocation()
			);
		} else {
			assert false;
			return;
		}

		SkriptEventHandler.logEventStart(event);
		synchronized (TRIGGERS) {
			for (Trigger trigger : TRIGGERS) {
				SkriptEventHandler.logTriggerStart(trigger);
				trigger.execute(experienceEvent);
				SkriptEventHandler.logTriggerEnd(trigger);
			}
		}
		SkriptEventHandler.logEventEnd();

		if (experienceEvent.isCancelled())
			experienceEvent.setSpawnedXP(0);

		if (event instanceof BlockExpEvent) {
			((BlockExpEvent) event).setExpToDrop(experienceEvent.getSpawnedXP());
		} else if (event instanceof EntityDeathEvent) {
			((EntityDeathEvent) event).setDroppedExp(experienceEvent.getSpawnedXP());
		} else if (event instanceof ExpBottleEvent) {
			((ExpBottleEvent) event).setExperience(experienceEvent.getSpawnedXP());
		} else if (event instanceof PlayerFishEvent) {
			((PlayerFishEvent) event).setExpToDrop(experienceEvent.getSpawnedXP());
		}
	};
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean postLoad() {
		TRIGGERS.add(trigger);
		if (REGISTERED_EXECUTORS.compareAndSet(false, true)) {
			EventPriority priority = SkriptConfig.defaultEventPriority.value();
			//noinspection unchecked
			for (Class<? extends Event> clazz : new Class[]{BlockExpEvent.class, EntityDeathEvent.class, ExpBottleEvent.class, PlayerFishEvent.class})
				Bukkit.getPluginManager().registerEvent(clazz, new Listener(){}, priority, EXECUTOR, Skript.getInstance(), true);
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
		return "experience spawn";
	}
	
}
