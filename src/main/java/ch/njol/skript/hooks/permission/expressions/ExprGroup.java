package ch.njol.skript.hooks.permission.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Name("Group")
@Description({
	"The primary group or all groups of a player. This expression requires Vault and a compatible permissions plugin to be installed.",
	"If you have LuckPerms, ensure you have vault integration enabled in the luck perms configurations."
})
@Examples({"on join:",
			"\tbroadcast \"%group of player%\" # this is the player's primary group",
			"\tbroadcast \"%groups of player%\" # this is all of the player's groups"})
@Since("2.2-dev35")
@RequiredPlugins({"Vault", "a permission plugin that supports Vault"})
public class ExprGroup extends SimpleExpression<String> {

	static {
		PropertyExpression.register(ExprGroup.class, String.class, "group[plural:s]", "offlineplayers");
	}

	private boolean primary;
	@Nullable
	private Expression<OfflinePlayer> players;

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!VaultHook.permission.hasGroupSupport()) {
			Skript.error(VaultHook.NO_GROUP_SUPPORT);
			return false;
		}
		players = (Expression<OfflinePlayer>) exprs[0];
		primary = !parseResult.hasTag("plural");
		return true;
	}

	@SuppressWarnings("null")
	@Override
	protected String[] get(Event event) {
		OfflinePlayer[] players = this.players.getArray(event);
		return CompletableFuture.supplyAsync(() -> { // #5692: LuckPerms errors for vault requests on main thread
			List<String> groups = new ArrayList<>();
			for (OfflinePlayer player : players) {
				if (primary) {
					groups.add(VaultHook.permission.getPrimaryGroup(null, player));
				} else {
					Collections.addAll(groups, VaultHook.permission.getPlayerGroups(null, player));
				}
			}
			return groups.toArray(new String[0]);
		}).join();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.ADD ||
				mode == Changer.ChangeMode.REMOVE ||
				mode == Changer.ChangeMode.SET ||
				mode == Changer.ChangeMode.DELETE ||
				mode == Changer.ChangeMode.RESET) {
			return new Class<?>[] {String[].class};
		}
		return null;
	}

	@Override
	@SuppressWarnings("null")
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		Permission api = VaultHook.permission;
		for (OfflinePlayer player : players.getArray(e)) {
			switch (mode) {
				case ADD:
					for (Object o : delta)
						api.playerAddGroup(null, player, (String) o);
					break;
				case REMOVE:
					for (Object o : delta)
						api.playerRemoveGroup(null, player, (String) o);
					break;
				case RESET:
				case DELETE:
				case SET:
					for (String group : api.getPlayerGroups(null, player)) {
						api.playerRemoveGroup(null, player, group);
					}
					if (mode == Changer.ChangeMode.SET) {
						for (Object o : delta) {
							api.playerAddGroup(null, player, (String) o);
						}
					}
			}
		}
	}

	@SuppressWarnings("null")
	@Override
	public boolean isSingle() {
		return players.isSingle() && primary;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@SuppressWarnings("null")
	@Override
	public String toString(Event event, boolean debug) {
		return "group" + (primary ? "" : "s") + " of " + players.toString(event, debug);
	}

}
