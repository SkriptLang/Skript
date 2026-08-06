package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Send Experience Change")
@Description("""
	Makes a player see their experience level & progress as something it is not.
	This does not change the level or level progress of the player in any way.
	""")
@Example("make player see their own experience as level 50 with 50% progress")
@Example("make player see their xp as level 140 with 0% progress")
@Since("INSERT VERSION")
public class EffSendExperienceChange extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendExperienceChange.class)
			.supplier(EffSendExperienceChange::new)
			.addPatterns("make %players% see their [own] (experience|exp|xp) as level %integer% with %number% progress")
			.build());
	}

	private Expression<Player> players;
	private Expression<Number> progress;
	private Expression<Integer> level;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		level = (Expression<Integer>) expressions[1];
		progress = (Expression<Number>) expressions[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		Number progress = this.progress.getSingle(event);
		if (progress == null)
			return;
		Integer level = this.level.getSingle(event);
		if (level == null)
			return;
		for (Player player : players)
			player.sendExperienceChange(Math.clamp(progress.floatValue(), 0.0f, 1.0f), Math.max(level, 0));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "see their own experience as level", level, "with", progress, "progress")
			.toString();
	}

}
