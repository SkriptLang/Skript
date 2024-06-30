package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyMembers;
import ch.njol.skript.util.scoreboard.ScoreUtils;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Team;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO doc
public class ExprEntries extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprEntries.class, String.class, ExpressionType.PROPERTY,
			"[the] entries of %team%", "%team%'[s] entries");
	}

	private Expression<Team> expression;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.expression = (Expression<Team>) expressions[0];
		return true;
	}

	@Override
	protected @Nullable String[] get(Event event) {
		List<String> members = new ArrayList<>();
		for (Team team : expression.getArray(event)) {
			members.addAll(team.getEntries());
		}
		return members.toArray(new String[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return ExprMembers.changers(mode);
	}

	@Override
	public void change(Event event,  Object @Nullable [] delta, Changer.ChangeMode mode) {
		if (delta == null && mode != Changer.ChangeMode.RESET)
			return;
		for (Team team : expression.getArray(event)) {
			switch (mode) {
				case RESET:
					for (String entry : team.getEntries())
						team.removeEntry(entry); // remove entries is paper only :(
				case SET: // set is reset + add
				case ADD:
					if (delta == null)
						break;
					for (Object object : delta)
						team.addEntry(ScoreUtils.toEntry(object));
					break;
				case REMOVE:
					//noinspection ConstantValue IntelliJ is wrong here
					if (delta == null)
						break;
					for (Object object : delta)
						team.removeEntry(ScoreUtils.toEntry(object));
					break;
			}
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the entries of " + expression.toString(event, debug);
	}

}
