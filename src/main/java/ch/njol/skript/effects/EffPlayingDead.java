package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Play Dead")
@Description("Make an axolotl start or stop playing dead.")
@Examples("make last spawned axolotl play dead")
@Since("INSERT VERSION")
public class EffPlayingDead extends Effect {

	static {
		Skript.registerEffect(EffPlayingDead.class,
			"make %livingentities% (start playing|play) dead",
			"force %livingentities% to (start playing|play) dead",
			"make %livingentities% (stop playing|not play) dead",
			"force %livingentities% to (stop playing|not play) dead");
	}

	private Expression<LivingEntity> entities;
	private boolean playDead;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		playDead = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Axolotl axolotl)
				axolotl.setPlayingDead(playDead);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (playDead ? " start" : " stop") + " playing dead";
	}

}
