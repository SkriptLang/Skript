package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyMembers;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO doc
public class ExprMembers extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprMembers.class, Object.class, ExpressionType.PROPERTY,
			"[the] members of %any-group%", "%any-group%'[s] members");
	}

	private Expression<AnyMembers<?>> expression;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.expression = (Expression<AnyMembers<?>>) expressions[0];
		return true;
	}

	@Override
	protected @Nullable Object[] get(Event event) {
		List<Object> members = new ArrayList<>();
		for (AnyMembers<?> objects : expression.getArray(event)) {
			members.addAll(objects.members());
		}
		return members.toArray();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
				return new Class[] {Entity[].class, String[].class, OfflinePlayer[].class, Object[].class};
			case RESET:
				return new Class[0];
		}
		return null;
	}

	@Override
	public void change(Event event,  Object @Nullable [] delta, Changer.ChangeMode mode) {
		if (delta == null && mode != Changer.ChangeMode.RESET)
			return;
		for (AnyMembers<?> group : expression.getArray(event)) {
			if (!group.membersSupportChanges())
				continue;
			switch (mode) {
				case RESET:
					group.resetMembers();
					break;
				case SET:
					group.setMembers(Arrays.asList(delta));
					break;
				case ADD:
					group.addMembers(Arrays.asList(delta));
					break;
				case REMOVE:
					group.removeMembers(Arrays.asList(delta));
					break;
			}
		}
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the members of " + expression.toString(event, debug);
	}

}
