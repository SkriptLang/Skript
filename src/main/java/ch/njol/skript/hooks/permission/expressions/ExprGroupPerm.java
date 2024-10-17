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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Group Permissions")
@Description("All permissions of a group")
@Examples("add \"perms.test\" to permissions of group \"Group1\"")
@Since("INSERT VERSION")
public class ExprGroupPerm extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprGroupPerm.class, String.class, ExpressionType.SIMPLE,
			"[all] permissions of group %string%",
			"group %string%'s [all] permissions");
	}

	private Expression<String> exprGroup = null;

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "permissions" + (exprGroup == null ? "" : " of group");
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
		if (VaultHook.permission == null || !VaultHook.permission.hasGroupSupport()) {
			Skript.error(VaultHook.NO_GROUP_SUPPORT);
			return false;
		}

		exprGroup = (Expression<String>) expressions[0];
		return true;
	}

	@Override
	protected @Nullable String[] get(Event event) {
		return CollectionUtils.array("Not done yet");
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		if (exprGroup == null) return null;
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
		String group = exprGroup == null ? null : exprGroup.getSingle(event);
		String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);
		if (group == null || stringDelta == null || !group.trim().isEmpty()) return;

		for (String perm : stringDelta) {
			checkMode:
			switch (mode) {
				case ADD:
					VaultHook.permission.groupAdd((String) null, group, perm);
					break checkMode;
				case REMOVE:
					VaultHook.permission.groupRemove((String) null, group, perm);
					break checkMode;
			}
		}
	}
}
