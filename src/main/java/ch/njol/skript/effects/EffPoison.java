package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Poison/Cure")
@Description("Poison or cure a living entity.")
@Examples({
	"poison the player",
	"poison the victim for 20 seconds",
	"cure the player from poison"
})
@Since("1.3.2")
public class EffPoison extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffPoison.class,
			"poison %livingentities% [for %-timespan%]",
			"(cure|unpoison) %livingentities% [(from|of) poison]");
	}
	
	private final static int DEFAULT_DURATION = 15 * 20; // 15 seconds on hard difficulty, same as EffPotion

	private Node node;
	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> duration;
	private boolean cure;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entities = (Expression<LivingEntity>) exprs[0];
		if (matchedPattern == 0)
			duration = (Expression<Timespan>) exprs[1];
		cure = matchedPattern == 1;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		int duration = DEFAULT_DURATION;
		if (this.duration != null) {
			Timespan timespan = this.duration.getSingle(event);
			if (timespan == null) {
				warning("The provided duration was null, so defaulted to 15 seconds.", this.duration.toString(null, false));
			} else {
				duration = (int) timespan.getAs(TimePeriod.TICK); // this will truncate anything greater than Integer.MAX_VALUE
			}
		}

		for (LivingEntity entity : entities.getArray(event)) {
			if (!cure) {
				if (entity.hasPotionEffect(PotionEffectType.POISON)) {
					for (PotionEffect effect : entity.getActivePotionEffects()) {
						if (effect.getType() != PotionEffectType.POISON)
							continue;
						duration += effect.getDuration();
					}
				}
				entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 0), true);
			} else {
				entity.removePotionEffect(PotionEffectType.POISON);
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "poison " + entities.toString(event, debug);
	}

}
