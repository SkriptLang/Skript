package ch.njol.skript.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Poison/Cure")
@Description("Poison or cure a creature.")
@Examples({"poison the player",
		"poison the victim for 20 seconds",
		"cure the player from poison"})
@Since("1.3.2")
public class EffPoison extends Effect {
	static {
		Skript.registerEffect(EffPoison.class,
				"poison %livingentities% [for %-timespan%]",
				"(cure|unpoison) %livingentities% [(from|of) poison]");
	}
	
	private final static int DEFAULT_DURATION = 15 * 20; // 15 seconds on hard difficulty, same as EffPotion
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entites;
	@Nullable
	private Expression<Timespan> duration;
	
	private boolean cure;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entites = (Expression<LivingEntity>) exprs[0];
		if (matchedPattern == 0)
			duration = (Expression<Timespan>) exprs[1];
		cure = matchedPattern == 1;
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "poison " + entites.toString(e, debug);
	}
	
	@Override
	protected void execute(final Event e) {
		for (final LivingEntity le : entites.getArray(e)) {
			if (!cure) {
				Timespan dur;
				int d = (int) (duration != null && (dur = duration.getSingle(e)) != null ? 
						(dur.getAs(Timespan.TimePeriod.TICK) >= Integer.MAX_VALUE ? Integer.MAX_VALUE : dur.getAs(Timespan.TimePeriod.TICK)) : DEFAULT_DURATION);
				if (le.hasPotionEffect(PotionEffectType.POISON)) {
					for (final PotionEffect pe : le.getActivePotionEffects()) {
						if (pe.getType() != PotionEffectType.POISON)
							continue;
						d += pe.getDuration();
					}
				}
				le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, d, 0), true);
			} else {
				le.removePotionEffect(PotionEffectType.POISON);
			}
		}
	}
	
}
