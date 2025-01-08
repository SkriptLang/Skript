package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Open Book")
@Description("Opens a written book to a player.")
@Examples("open book player's tool to player")
@RequiredPlugins("Minecraft 1.14.2+")
@Since("2.5.1")
public class EffOpenBook extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		if (Skript.methodExists(Player.class, "openBook", ItemStack.class)) {
			Skript.registerEffect(EffOpenBook.class, "(open|show) book %itemtype% (to|for) %players%");
		}
	}

	private Node node;
	private Expression<ItemType> book;
	private Expression<Player> players;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		book = (Expression<ItemType>) exprs[0];
		players = (Expression<Player>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		ItemType itemType = book.getSingle(event);
		if (itemType == null) {
			error("The provided book item was null.", book.toString(null, false));
			return;
		}

		ItemStack itemStack = itemType.getRandom();
		if (itemStack == null || !(itemStack.getType() == Material.WRITTEN_BOOK)) {
			error("The provided book item was either invalid or not a written book.", book.toString(null, false));
			return;
		}

		for (Player player : players.getArray(event)) {
			player.openBook(itemStack);
		}
	}

	@Override
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "open book " + book.toString(event, debug) + " to " + players.toString(event, debug);
	}

}
