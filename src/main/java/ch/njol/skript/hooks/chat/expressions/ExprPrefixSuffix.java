package ch.njol.skript.hooks.chat.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Name("Prefix/Suffix")
@Description("The prefix or suffix as defined in the server's chat plugin.")
@Examples({
	"on chat:",
	"\tcancel event",
	"\tbroadcast \"%player's prefix%%player's display name%%player's suffix%: %message%\" to the player's world",
	"",
	"set the player's prefix to \"[&lt;red&gt;Admin<reset>] \"",
	"",
	"clear player's prefix"
})
@Since("2.0, INSERT VERSION (delete)")
@RequiredPlugins({"Vault", "a chat plugin that supports Vault"})
public class ExprPrefixSuffix extends SimplePropertyExpression<Player, String> {
	static {
		register(ExprPrefixSuffix.class, String.class, "[chat] (1:prefix|2:suffix)", "players");
	}
	
	private boolean prefix;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		prefix = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public String convert(Player player) {
		return Utils.replaceChatStyles(prefix ? VaultHook.chat.getPlayerPrefix(player) : VaultHook.chat.getPlayerSuffix(player));
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> new Class[] {String.class};
			case RESET, DELETE -> new Class<?>[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event,  Object @Nullable [] delta, ChangeMode mode) {
		CompletableFuture.runAsync(() -> {
			for (Player player : getExpr().getArray(event)) {
				switch (mode) {
					case SET -> {
						if (prefix) {
							VaultHook.chat.setPlayerPrefix(player, (String) delta[0]);
						} else {
							VaultHook.chat.setPlayerSuffix(player, (String) delta[0]);
						}
					}
					case RESET, DELETE -> {
						if (prefix) {
							VaultHook.chat.setPlayerPrefix(player, null);
						} else {
							VaultHook.chat.setPlayerSuffix(player, null);
						}
					}
				}
			}
		}).join();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return prefix ? "prefix" : "suffix";
	}
	
}
