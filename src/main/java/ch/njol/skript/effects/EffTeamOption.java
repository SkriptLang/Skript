package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

// todo doc
public class EffTeamOption extends Effect {

	public static final Team.OptionStatus[] OPTIONS = Team.OptionStatus.values();

	public static String optionStatus() {
		// test-only; make sure the enum erasure hasn't changed between versions
		assert OPTIONS[0] == Team.OptionStatus.ALWAYS
			&& OPTIONS[1] == Team.OptionStatus.NEVER
			&& OPTIONS[2] == Team.OptionStatus.FOR_OTHER_TEAMS
			&& OPTIONS[3] == Team.OptionStatus.FOR_OWN_TEAM;
		return "(3:this team|2:other teams|0:every(body|one)|1:no(body|one))";
	}

	public static String statusName(Team.OptionStatus status) {
		switch (status) {
			case ALWAYS:
				return "everybody";
			case NEVER:
				return "nobody";
			case FOR_OTHER_TEAMS:
				return "other teams";
			default:
				return "this team";
		}
	}

	static {
		String optionStatus = optionStatus();
		Skript.registerEffect(EffTeamOption.class,
			"(allow|permit) " + optionStatus + " to (collide:collide with|tag:see [the] name[ ]tag[s] of|death:see the death message[s] of) %teams%",
				"(allow|permit) " + optionStatus + " to see %teams%'[s] (tag:name[ ]tag[s]|death:death message[s])"
		);
	}

	private Team.Option option;
	private Team.OptionStatus status;
	private Expression<Team> teamExpression;

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
		assert result.mark < OPTIONS.length;
		this.status = OPTIONS[result.mark];
		//noinspection unchecked
		this.teamExpression = (Expression<Team>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Team team : teamExpression.getArray(event)) {
			team.setOption(option, status);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		switch (option) {
			case COLLISION_RULE:
				return "allow " + statusName(status) + " to collide with " + teamExpression.toString(event, debug);
			case DEATH_MESSAGE_VISIBILITY:
				return "allow " + statusName(status) + " to see the death messages of " + teamExpression.toString(event, debug);
			case NAME_TAG_VISIBILITY:
				return "allow " + statusName(status) + " to see the name tags of " + teamExpression.toString(event, debug);
		}
		assert false;
		return "";
	}

}
