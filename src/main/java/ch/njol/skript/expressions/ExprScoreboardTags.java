package ch.njol.skript.expressions;

import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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

@Name("Scoreboard Tags")
@Description({"Scoreboard tags are simple list of texts stored directly in the data of an <a href='classes.html#entity'>entity</a>.",
		"So this is a Minecraft related thing, not Bukkit, so the tags will not get removed when the server stops. " +
		"You can visit <a href='https://minecraft.wiki/w/Scoreboard#Tags'>visit Minecraft Wiki</a> for more info.",
		"This is changeable and valid for any type of entity. " +
		"Also you can use use the <a href='conditions.html#CondHasScoreboardTag'>Has Scoreboard Tag</a> condition to check whether an entity has the given tags.",
		"",
		"Requires Minecraft 1.11+ (actually added in 1.9 to the game, but added in 1.11 to Spigot)."})
@Examples({"on spawn of a monster:",
        "\tif the spawn reason is mob spawner:",
        "\t\tadd \"spawned by a spawner\" to the scoreboard tags of event-entity",
        "",
        "on death of a monster:",
        "\tif the attacker is a player:",
        "\t\tif the victim doesn't have the scoreboard tag \"spawned by a spawner\":",
        "\t\t\tadd 1$ to attacker's balance"})
@Since("2.3")
public class ExprScoreboardTags extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprScoreboardTags.class, String.class, ExpressionType.PROPERTY,
			"[(all [[of] the]|the)] scoreboard tags of %entities%",
			"%entities%'[s] scoreboard tags");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	public String[] get(Event e) {
		return Stream.of(entities.getArray(e))
				.map(Entity::getScoreboardTags)
				.flatMap(Set::stream)
				.toArray(String[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		for (Entity entity : entities.getArray(e)) {
			switch (mode) {
				case SET:
					assert delta != null;
					entity.getScoreboardTags().clear();
					for (Object tag : delta)
						entity.addScoreboardTag((String) tag);
					break;
				case ADD:
					assert delta != null;
					for (Object tag : delta)
						entity.addScoreboardTag((String) tag);
					break;
				case REMOVE:
					assert delta != null;
					for (Object tag : delta)
						entity.removeScoreboardTag((String) tag);
					break;
				case DELETE:
				case RESET:
					entity.getScoreboardTags().clear();
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the scoreboard tags of " + entities.toString(e, debug);
	}

}
