package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;

@Name("Experience")
@Description("How much experience was spawned in an experience spawn or block break event. Can be changed.")
@Examples({
	"on experience spawn:",
		"\tadd 5 to the spawned experience",
	"on break of coal ore:",
		"\tclear dropped experience",
	"on break of diamond ore:",
		"\tif tool of player = diamond pickaxe:",
			"\t\tadd 100 to dropped experience",
	"on fishing:",
		"\tadd 70 to dropped experience"
})
@Since("2.1, 2.5.3 (block break event), 2.7 (experience change event), INSERT VERSION (fishing)")
@Events({"experience spawn", "break / mine", "experience change", "Fishing"})
public class ExprExperience extends SimpleExpression<Experience> {

	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE, "[the] [spawned|dropped] [e]xp[erience] [orb[s]]");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ExperienceSpawnEvent.class, BlockBreakEvent.class,
			PlayerExpChangeEvent.class, PlayerFishEvent.class)) {
			Skript.error("The 'experience' expression can only be used in experience spawn, " +
				"block break, player experience change and fishing events");
			return false;
		}
		return true;
	}

	@Override
	protected Experience @Nullable [] get(Event event) {
		if (event instanceof ExperienceSpawnEvent)
			return new Experience[]{new Experience(((ExperienceSpawnEvent) event).getSpawnedXP())};
		else if (event instanceof BlockBreakEvent)
			return new Experience[]{new Experience(((BlockBreakEvent) event).getExpToDrop())};
		else if (event instanceof PlayerFishEvent)
			return new Experience[]{new Experience(((PlayerFishEvent) event).getExpToDrop())};
		else if (event instanceof PlayerExpChangeEvent)
			return new Experience[]{new Experience(((PlayerExpChangeEvent) event).getAmount())};
		else
			return new Experience[0];
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				return new Class[]{Experience[].class, Number[].class};
			case SET:
				return new Class[]{Experience.class, Number.class};
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int experience;
		if (event instanceof ExperienceSpawnEvent)
			experience = ((ExperienceSpawnEvent) event).getSpawnedXP();
		else if (event instanceof BlockBreakEvent)
			experience = ((BlockBreakEvent) event).getExpToDrop();
		else if (event instanceof PlayerFishEvent)
			experience = ((PlayerFishEvent) event).getExpToDrop();
		else if (event instanceof PlayerExpChangeEvent)
			experience = ((PlayerExpChangeEvent) event).getAmount();
		else
			return;

		if (delta != null) {
			for (Object object : delta) {
				int value = object instanceof Experience ? ((Experience) object).getXP() : ((Number) object).intValue();
				switch (mode) {
					case ADD:
						experience += value;
						break;
					case SET:
						experience = value;
						break;
					case REMOVE:
					case REMOVE_ALL:
						experience -= value;
						break;
					case RESET:
					case DELETE:
						assert false;
						break;
				}
			}
		} else {
			experience = 0;
		}

		experience = Math.max(0, Math.round(experience));
		if (event instanceof ExperienceSpawnEvent)
			((ExperienceSpawnEvent) event).setSpawnedXP(experience);
		else if (event instanceof PlayerFishEvent)
			((PlayerFishEvent) event).setExpToDrop(experience);
		else if (event instanceof BlockBreakEvent)
			((BlockBreakEvent) event).setExpToDrop(experience);
		else
			((PlayerExpChangeEvent) event).setAmount(experience);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Experience> getReturnType() {
		return Experience.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the experience";
	}

}
