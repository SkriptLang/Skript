package ch.njol.skript.hooks.permission.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.Nullable;

@Name("Player Permissions")
@Description("All permissions of a player")
@Examples("add \"perms.test\" to permissions of event-player")
@Since("INSERT VERSION")
public class ExprPlayerPerms extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprPlayerPerms.class, String.class, ExpressionType.PROPERTY,
			"[all] permissions of %player%",
			"%player%'s [all] permissions");
	}

	private Expression<Player> exprPlayer = null;

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "permissions" + (exprPlayer == null ? "" : " of player");
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (VaultHook.permission == null) {
			return false;
		}

		exprPlayer = expressions.length == 0 ? null : (Expression<Player>) expressions[0];
		return true;
	}

	@Nullable
	@Override
	protected String[] get(Event event) {
		Player player = exprPlayer.getSingle(event);
		return player == null ? null : player.getEffectivePermissions().stream()
										   .map(PermissionAttachmentInfo::getPermission)
										   .toList().toArray(new String[0]);
	}

	@Override
	public Class<?>[] @Nullable acceptChange(ChangeMode mode) {
		if (exprPlayer == null) return null;
		switch (mode) {
			case ADD:
			case REMOVE:
				return CollectionUtils.array(String[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Player player = exprPlayer == null ? null : exprPlayer.getSingle(event);
		String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
		if (player == null || stringDelta == null || !player.isOnline()) return;

		for (String perm : stringDelta) {
			checkMode:
			switch (mode) {
				case ADD:
					VaultHook.permission.playerAdd(null, player, perm);
					break checkMode;
				case REMOVE:
					VaultHook.permission.playerRemove(null, player, perm);
					break checkMode;
			}
		}
	}
}
