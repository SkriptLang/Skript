package org.skriptlang.skript.bukkit.item.book.elements.effects;

import ch.njol.skript.lang.SyntaxStringBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Open Book")
@Description("Opens a written book to a player.")
@Example("open book player's tool to player")
@Since("2.5.1")
public class EffOpenBook extends Effect {
	
	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.simple(EffOpenBook.class, EffOpenBook::new,
			"(open|show) book %itemstack% (to|for) %players%"));
	}

	private Expression<ItemStack> book;
	private Expression<Player> players;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		book = (Expression<ItemStack>) expressions[0];
		//noinspection unchecked
		players = (Expression<Player>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		ItemStack book = this.book.getSingle(event);
		if (book == null || !book.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
			return;
		}
		for (Player player : players.getArray(event)) {
			player.openBook(book);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("open book", book, "to", players)
			.toString();
	}

}
