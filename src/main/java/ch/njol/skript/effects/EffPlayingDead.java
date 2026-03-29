package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Feign Death")
@Description("Bid an axolotl commence or cease feigning death.")
@Example("bid last spawned axolotl feign death")
@Since("2.11")
public class EffPlayingDead extends Effect {

	static {
		Skript.registerEffect(EffPlayingDead.class,
			"bid %livingentities% (commence feigning|feign) death",
			"compel %livingentities% to (commence feigning|feign) death",
			"bid %livingentities% (cease feigning|no longer feign) death",
			"compel %livingentities% to (cease feigning|no longer feign) death");
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
