package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffTeamOption;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

// todo doc
public class CondTeamOption extends Condition {

	static {
		String optionStatus = EffTeamOption.optionStatus();
		Skript.registerCondition(CondTeamOption.class,
			"[:only] " + optionStatus + " can(not:not|n't) (collide:collide with|tag:see [the] name[ ]tag[s] of|death:see the death message[s] of) %teams%",
			"[:only] " + optionStatus + " can(not:not|n't) see %teams%'[s] (tag:name[ ]tag[s]|death:death message[s])"
		);
	}

	private Team.Option option;
	private Team.OptionStatus status;
	private Expression<Team> teamExpression;
	private boolean only;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (result.hasTag("death"))
			this.option = Team.Option.DEATH_MESSAGE_VISIBILITY;
		else if (result.hasTag("tag"))
			this.option = Team.Option.NAME_TAG_VISIBILITY;
		else if (result.hasTag("collide"))
			this.option = Team.Option.COLLISION_RULE;
		else { // how did we get here? No idea, but just in case
			Skript.error("Unrecognized team option: choose one of name tags, death messages or collision.");
			return false;
		}
		assert result.mark < EffTeamOption.OPTIONS.length;
		this.status = EffTeamOption.OPTIONS[result.mark];
		this.only = result.hasTag("only");
		this.setNegated(result.hasTag("not"));
		//noinspection unchecked
		this.teamExpression = (Expression<Team>) expressions[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return teamExpression.check(event, team -> {
			Team.OptionStatus current = team.getOption(option);
			if (only)
				return current == status;
			switch (status) { // if everybody can X, then this team can X
				case FOR_OWN_TEAM:
				case FOR_OTHER_TEAMS:
					return current == status || current == Team.OptionStatus.ALWAYS;
				default:
					return current == status;
			}
		}, this.isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		switch (option) {
			case COLLISION_RULE:
				return (only ? "only " : "")
					+ EffTeamOption.statusName(status)
					+ (this.isNegated() ? " cannot" : "")
					+ " collide with " + teamExpression.toString(event, debug);
			case DEATH_MESSAGE_VISIBILITY:
				return (only ? "only " : "")
					+ EffTeamOption.statusName(status)
					+ (this.isNegated() ? " cannot" : "")
					+ " see the death messages of " + teamExpression.toString(event, debug);
			case NAME_TAG_VISIBILITY:
				return (only ? "only " : "")
					+ EffTeamOption.statusName(status)
					+ (this.isNegated() ? " cannot" : "")
					+ " see the name tags of " + teamExpression.toString(event, debug);
		}
		assert false;
		return "";
	}

}
