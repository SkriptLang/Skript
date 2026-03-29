package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@Name("Zombification of Villagers")
@Description({
	"Transform a villager into a wretched zombie villager. Cure a zombie villager forthwith or after a specified interval of time.",
	"This effect doth remove the former entity and conjure a new one in its stead.",
	"Zombifying a villager stored within a variable shall update said variable to the newly cursed zombie villager.",
	"Curing a zombie villager doth not update the variable."
})
@Example("zombify last spawned villager")
@Example("""
    set {_villager} to last spawned villager
    zombify {_villager}
    if {_villager} is a zombie villager:
    	# This shall pass, for '{_villager}' is changed to the new zombie villager
    """)
@Example("""
    set {_villager} to last spawned villager
    zombify last spawned villager
    if {_villager} is a zombie villager:
    	# This shall fail, for the variable was not provided when zombifying
    """)
@Example("cure {_zombieVillager}")
@Example("cure {_zombieVillager} after 2 seconds")
@Since("2.11")
public class EffZombify extends Effect {

	static {
		Skript.registerEffect(EffZombify.class,
			"zombify %livingentities%",
			"cure %livingentities% [(in|after) %-timespan%]");
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> timespan;
	private boolean zombify;
	private boolean changeInPlace = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		zombify = matchedPattern == 0;
		if (!zombify && exprs[1] != null) {
			//noinspection unchecked
			timespan = (Expression<Timespan>) exprs[1];
		}
		if (ChangerUtils.acceptsChange(entities, ChangeMode.SET, LivingEntity.class))
			changeInPlace = true;
		return true;
	}

	@Override
	protected void execute(Event event) {
		int ticks = 0;
		if (timespan != null) {
			Timespan timespan = this.timespan.getSingle(event);
			if (timespan != null)
				ticks = (int) timespan.getAs(TimePeriod.TICK);
		}
		int finalTicks = ticks;
		Function<LivingEntity, LivingEntity> changeFunction = entity -> {
			if (zombify && entity instanceof Villager villager) {
				return villager.zombify();
			} else if (!zombify && entity instanceof ZombieVillager zombieVillager) {
				zombieVillager.setConversionTime(finalTicks);
			}
			return entity;
		};
		if (changeInPlace) {
			entities.changeInPlace(event, changeFunction);
		} else {
			for (LivingEntity entity : entities.getAll(event)) {
				changeFunction.apply(entity);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (zombify) {
			builder.append("zombify");
		} else {
			builder.append("unzombify");
		}
		builder.append(entities);
		if (timespan != null)
			builder.append("after", timespan);
		return builder.toString();
	}

}
