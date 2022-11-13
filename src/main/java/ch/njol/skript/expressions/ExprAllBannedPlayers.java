package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Banned Players")
@Description("Returns a list of all the banned players.")
@Examples({
	"command /banlist:",
	"\ttrigger:",
	"\t\tsend all the banned players"
})
@Since("INSERT VERSION")
public class ExprAllBannedPlayers extends SimpleExpression<OfflinePlayer> {

	static {
		Skript.registerExpression(ExprAllBannedPlayers.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] banned players");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected OfflinePlayer[] get(Event event) {
		return Bukkit.getBannedPlayers().toArray(new OfflinePlayer[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all banned players";
	}

}
