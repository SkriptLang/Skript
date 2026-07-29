package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerEditBook extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerEditBook.class, "Player Edit Book")
			.supplier(EvtPlayerEditBook::new)
			.addEvent(PlayerEditBookEvent.class)
			.addPatterns("book (edit|change|write)")
			.addDescription("Called when a player edits a book.")
			.addExample("""
				on book change:
					send "Nice edit!" to player
				""")
			.addSince("2.2-dev31")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerEditBookEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getPreviousBookMeta());
				return book;
			})
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerEditBookEvent.class, ItemStack.class)
			.getter(event -> {
				ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				book.setItemMeta(event.getNewBookMeta());
				return book;
			})
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerEditBookEvent.class, Component[].class)
			.getter(event -> event.getPreviousBookMeta().pages().toArray(new Component[0]))
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerEditBookEvent.class, Component[].class)
			.getter(event -> event.getNewBookMeta().pages().toArray(new Component[0]))
			.build());
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerEditBookEvent playerEvent = (PlayerEditBookEvent) event;

		return !playerEvent.isSigning();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player edit book";
	}

}
