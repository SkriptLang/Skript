package ch.njol.skript.effects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Open Book")
@Description("Opens a written book to a player.")
@Examples("open book player's tool to player")
@RequiredPlugins("Minecraft 1.14.2+")
@Since("2.5.1")
public class EffOpenBook extends Effect {
	
	static {
		if (Skript.methodExists(Player.class, "openBook", ItemStack.class)) {
			Skript.registerEffect(EffOpenBook.class, "(open|show) book %itemtype% (to|for) %players%");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> book;
	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		book = (Expression<ItemType>) exprs[0];
		players = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		ItemType itemType = book.getSingle(e);
		if (itemType != null) {
			ItemStack itemStack = itemType.getRandom();
			if (itemStack != null && itemStack.getType() == Material.WRITTEN_BOOK) {
				for (Player player : players.getArray(e)) {
					player.openBook(itemStack);
				}
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "open book " + book.toString(e, debug) + " to " + players.toString(e, debug);
	}
	
}
