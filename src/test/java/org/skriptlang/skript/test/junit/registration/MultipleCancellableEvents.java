package org.skriptlang.skript.test.junit.registration;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

@Name("Multiple Cancel Events")
@Description("Test registration to ensure that the 'some events cannot be cancelled' error works.")
@NoDoc
public class MultipleCancellableEvents extends SkriptEvent {

	static {
		Skript.registerEvent("cancel events test 1", MultipleCancellableEvents.class, CollectionUtils.array(NotCancellableEvent.class, CancellableEvent.class), "cancel events test 1");
		Skript.registerEvent("cancel events test 2", MultipleCancellableEvents.class, CancellableEvent.class, "cancel events test 2");
		Skript.registerEvent("cancel events test 3", MultipleCancellableEvents.class, NotCancellableEvent.class, "cancel events test 3");
	}

	public static class CancellableEvent extends Event implements Cancellable {

		// Bukkit stuff
		private final static HandlerList handlers = new HandlerList();

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void setCancelled(boolean cancel) {}

	}

	public static class NotCancellableEvent extends Event {

		// Bukkit stuff
		private final static HandlerList handlers = new HandlerList();

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

	}

	static boolean done;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (!done) {
			done = true;
			Bukkit.getScheduler().runTask(Skript.getInstance(), () -> {
				Bukkit.getPluginManager().callEvent(new CancellableEvent());
				Bukkit.getPluginManager().callEvent(new NotCancellableEvent());
			});
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cancel events test";
	}

}
