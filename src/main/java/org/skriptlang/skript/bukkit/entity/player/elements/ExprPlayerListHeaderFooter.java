package org.skriptlang.skript.bukkit.entity.player.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Player List Header and Footer")
@Description("The message above and below the player list in the tab menu.")
@Example("set all players' tab list header to \"Welcome to the Server!\"")
@Example("send \"%the player's tab list header%\" to player")
@Example("reset all players' tab list header")
@Since("2.4")
@Keywords({"tablist", "tab list"})
public class ExprPlayerListHeaderFooter extends SimplePropertyExpression<Player, Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPlayerListHeaderFooter.class, Component.class,
			"(player|tab)[ ]list (header|:footer) [text|message]", "players", false)
				.supplier(ExprPlayerListHeaderFooter::new)
				.build());
	}

	private boolean isFooter;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		isFooter = parseResult.hasTag("footer");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Component convert(Player player) {
		return isFooter ? player.playerListFooter() : player.playerListHeader();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Component[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		Component text = Component.empty();
		if (delta != null) {
			text = TextComponentUtils.joinByNewLine(Arrays.copyOf(delta, delta.length, Component[].class));
		}
		for (Player player : getExpr().getArray(event)) {
			if (isFooter) {
				player.sendPlayerListFooter(text);
			} else {
				player.sendPlayerListHeader(text);
			}
		}
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	protected String getPropertyName() {
		return "player list " + (isFooter ? "footer" : "header");
	}

}
