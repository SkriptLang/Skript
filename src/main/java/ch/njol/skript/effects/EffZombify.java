package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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

@Name("Zombify Villager")
@Description({
	"Turn a villager into a zombie villager. Cure a zombie villager immediately or after specified amount of time.",
	"This effect removes the old entity and creates a new entity. Using a variable with an entity stored in it, will not change the variable to the new entity."
})
@Examples({
	"zombify {_villager}",
	"if {_villager} is a villager:",
		"\t# This will pass because '{_villager}' does not change to the zombie villager",
	"",
	"unzombify {_zombieVillager}",
	"unzombify {_zombieVillager} after 2 seconds"
})
@Since("INSERT VERSION")
public class EffZombify extends Effect {

	static {
		Skript.registerEffect(EffZombify.class,
			"zombify %livingentities%",
			"unzombify %livingentities% [(in|after) %-timespan%]");
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Timespan> timespan;
	private boolean zombify;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		zombify = matchedPattern == 0;
		if (!zombify && exprs[1] != null) {
			//noinspection unchecked
			timespan = (Expression<Timespan>) exprs[1];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		int ticks = 0;
		if (timespan != null)
			ticks = (int) timespan.getSingle(event).getAs(TimePeriod.TICK);
		for (LivingEntity entity : entities.getAll(event)) {
			if (zombify && entity instanceof Villager villager) {
				villager.zombify();
			} else if (!zombify && entity instanceof ZombieVillager zombieVillager) {
				zombieVillager.setConversionTime(ticks);
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
