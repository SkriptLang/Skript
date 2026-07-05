package org.skriptlang.skript.bukkit.entity.elements.events;


import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
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
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EvtExperienceSpawn extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtExperienceSpawn.class, "Experience Spawn")
			.supplier(EvtExperienceSpawn::new)
			.addEvent(ExperienceSpawnEvent.class)
			.addPatterns(
				"[e]xp[erience] [orb] spawn",
				"spawn of [a[n]] [e]xp[erience] [orb]"
			)
			.addDescription("""
				Called whenever experience is about to spawn.
				Please note that this event will not fire for xp orbs spawned by plugins (including Skript) with Bukkit.
				""")
			.addExample("""
				on xp spawn:
				    event-world is not world("experience")
				    cancel event
				""")
			.addSince("2.0")
			.build());

		eventValueRegistry.register(EventValue.builder(ExperienceSpawnEvent.class, Location.class)
			.getter(ExperienceSpawnEvent::getLocation)
			.build());

		eventValueRegistry.register(EventValue.builder(ExperienceSpawnEvent.class, Experience.class)
			.getter(event -> new Experience(event.getSpawnedXP()))
			.build());

		trackEvent(BlockExpEvent.class, BlockExpEvent::getExpToDrop, BlockExpEvent::setExpToDrop, blockEvent -> blockEvent.getBlock().getLocation().add(0.5, 0.5, 0.5));
		trackEvent(EntityDeathEvent.class, EntityDeathEvent::getDroppedExp, EntityDeathEvent::setDroppedExp, entityEvent -> entityEvent.getEntity().getLocation());
		trackEvent(ExpBottleEvent.class, ExpBottleEvent::getExperience, ExpBottleEvent::setExperience, entityEvent -> entityEvent.getEntity().getLocation());
		trackEvent(PlayerFishEvent.class, PlayerFishEvent::getExpToDrop, PlayerFishEvent::setExpToDrop, playerEvent -> playerEvent.getPlayer().getLocation());
	}

	private static final List<Trigger> TRIGGERS = Collections.synchronizedList(new ArrayList<>());
	private static final AtomicBoolean REGISTERED_EXECUTORS = new AtomicBoolean();
	private static final Map<Class<? extends Event>, EventDetails<Event>> TRACKED_EVENTS = new HashMap<>();

	public static <E extends Event> void trackEvent(
		Class<E> event,
		Function<E, Integer> getExp,
		@Nullable BiConsumer<E, Integer> setExp,
		Function<E, Location> getLocation
	) {
		//noinspection unchecked
		TRACKED_EVENTS.put(event, (EventDetails<Event>) new EventDetails<>(getExp, setExp, getLocation));
	}

	public record EventDetails<E>(Function<E, Integer> getExp,
								  @Nullable BiConsumer<E, Integer> setExp,
								  Function<E, Location> getLocation) {
	}

	private static final EventExecutor EXECUTOR = (listener, event) -> {
		EventDetails<Event> details = TRACKED_EVENTS.get(event.getClass());
		if (details == null)
			return;
		ExperienceSpawnEvent experienceEvent = new ExperienceSpawnEvent(
			details.getExp().apply(event),
			details.getLocation().apply(event)
		);

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

		if (details.setExp() != null)
			details.setExp().accept(event, experienceEvent.getSpawnedXP());
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
			for (Class<? extends Event> clazz : TRACKED_EVENTS.keySet())
				Bukkit.getPluginManager().registerEvent(clazz, new Listener() {
				}, priority, EXECUTOR, Skript.getInstance(), true);
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
