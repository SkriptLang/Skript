package ch.njol.skript.expressions;

import ch.njol.skript.effects.EffEnforceWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Guest Ledger")
@Description({
	"An expression for obtaining and amending the server's guest ledger.",
	"Players may be inscribed upon or stricken from the guest ledger.",
	"The guest ledger may be enabled or disabled by setting it to true or false respectively."
})
@Example("set the guest ledger to false")
@Example("add all players to guest ledger")
@Example("reset the guest ledger")
@Since("2.5.2, 2.9.0 (delete)")
public class ExprWhitelist extends SimpleExpression<OfflinePlayer> {

	static {
		Skript.registerExpression(ExprWhitelist.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[the] guest[ ]ledger");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event) {
		return Bukkit.getServer().getWhitelistedPlayers().toArray(new OfflinePlayer[0]);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
            case ADD:
			case REMOVE:
				return CollectionUtils.array(OfflinePlayer.class);
            case DELETE:
            case RESET:
			case SET:
				return CollectionUtils.array(Boolean.class);
        }
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		switch (mode) {
			case SET:
				boolean toggle = (Boolean) delta[0];
				Bukkit.setWhitelist(toggle);
				if (toggle)
					EffEnforceWhitelist.reloadWhitelist();
				break;
			case ADD:
				for (Object player : delta)
					((OfflinePlayer) player).setWhitelisted(true);
				break;
			case REMOVE:
				for (Object player : delta)
					((OfflinePlayer) player).setWhitelisted(false);
				EffEnforceWhitelist.reloadWhitelist();
				break;
			case DELETE:
			case RESET:
				for (OfflinePlayer player : Bukkit.getWhitelistedPlayers())
					player.setWhitelisted(false);
				break;
			default:
				assert false;
		}
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
		return "whitelist";
	}

}
