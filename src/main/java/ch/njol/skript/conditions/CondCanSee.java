package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Can See")
@Description("Checks whether the given players can see the provided entities.")
@Examples({
	"if sender can't see the player-argument:",
		"\tmessage \"who dat?\"",
	"",
	"if the player can see the last spawned entity:",
		"\tmessage \"hello there!\""
})
@Since("2.3, 2.10 (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public class CondCanSee extends Condition {

	static {
		Skript.registerCondition(CondCanSee.class,
				"%entities% (is|are) [visible|:invisible] for %players%",
				"%players% can see %entities%",
				"%entities% (is|are)(n't| not) [visible|:invisible] for %players%",
				"%players% can('t| not) see %entities%");
	}

	@SuppressWarnings("null")
	private Expression<Player> viewers;
	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
		if (matchedPattern == 1 || matchedPattern == 3) {
			viewers = (Expression<Player>) exprs[0];
			entities = (Expression<Entity>) exprs[1];
		} else {
			entities = (Expression<Entity>) exprs[0];
			viewers = (Expression<Player>) exprs[1];
		}
		setNegated(matchedPattern > 1 ^ result.hasTag("invisible"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		return viewers.check(event,
				player -> entities.check(event,
						player::canSee
				), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, event, debug, viewers,
				"see " + entities.toString(event, debug));
	}

}
