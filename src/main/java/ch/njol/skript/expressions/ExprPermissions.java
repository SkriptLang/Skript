package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Name("All Permissions")
@Description("Returns all permissions of the defined player(s). Note that the modifications to resulting list do not actually change permissions.")
@Examples("set {_permissions::*} to all permissions of the player")
@Since("2.2-dev33")
public class ExprPermissions extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprPermissions.class, String.class, ExpressionType.PROPERTY, "[(all [[of] the]|the)] permissions (from|of) %players%", "[(all [[of] the]|the)] %players%'[s] permissions");
	}
	
	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		final Set<String> permissions = new HashSet<>();
		for (Player player : players.getArray(e))
			for (final PermissionAttachmentInfo permission : player.getEffectivePermissions())
				permissions.add(permission.getPermission());
		return permissions.toArray(new String[permissions.size()]);
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
	public String toString(@Nullable Event event, boolean debug) {
		return "permissions of " + players.toString(event, debug);
	}

}
