package org.skriptlang.skript.bukkit.whitelist.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Whitelisted")
@Description("Whether or not the server or a player is whitelisted, or the server is whitelist enforced.")
@Examples({
	"if the player is whitelisted:",
	"if the server is whitelisted:",
	"if the server whitelist is enforced:"
})
@Since("2.5.2, 2.9.0 (enforce, offline players)")
public class CondIsWhitelisted extends Condition {

	static {
		Skript.registerCondition(CondIsWhitelisted.class,
			"[the] server (is|not:(isn't|is not)) (in white[ ]list mode|white[ ]listed)",
			"%offlineplayers% (is|are|not:(isn't|is not|aren't|are not)) white[ ]listed",
			"[the] server white[ ]list (is|not:(isn't|is not)) enforced");
	}

	private Expression<OfflinePlayer> players;
	private boolean isServer;
	private boolean isEnforce;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(parseResult.hasTag("not"));
		isServer = matchedPattern != 1;
		isEnforce = matchedPattern == 2;
		if (matchedPattern == 1)
			players = (Expression<OfflinePlayer>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (isServer)
			return (isEnforce ? Bukkit.isWhitelistEnforced() : Bukkit.hasWhitelist()) ^ isNegated();
		return players.check(event, OfflinePlayer::isWhitelisted, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (isServer) {
			if (isEnforce) {
				builder.append("the server whitelist")
					.append(isNegated() ? "is not" : "is")
					.append("enforced");
			} else {
				builder.append("the server")
					.append(isNegated() ? "is not" : "is")
					.append("whitelisted");
			}
		} else {
			builder.append(players)
				.append(isNegated() ? "is not" : "is")
				.append("whitelisted");
		}
		return builder.toString();
	}

}
