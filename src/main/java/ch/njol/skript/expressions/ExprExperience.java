package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.Nullable;

@Name("Experience")
@Description("How much experience was spawned in an experience spawn or block break event. Can be changed.")
@Examples({
	"on experience spawn:",
		"\tadd 5 to the spawned experience",
	"",
	"on break of coal ore:",
		"\tclear dropped experience",
	"",
	"on break of diamond ore:",
		"\tif tool of player = diamond pickaxe:",
			"\t\tadd 100 to dropped experience",
	"",
	"on breed:",
		"\tbreeding father is a cow",
		"\tset dropped experience to 10",
   "",
   "on fish catch:",
		"\tadd 70 to dropped experience",
})
@Since("2.1, 2.5.3 (block break event), 2.7 (experience change event), 2.10 (breeding, fishing)")
@Events({"experience spawn", "break / mine", "experience change", "entity breeding"})
public class ExprExperience extends SimpleExpression<Experience> {

	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE,
			"[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ExperienceSpawnEvent.class, BlockBreakEvent.class,
			PlayerExpChangeEvent.class, EntityBreedEvent.class, PlayerFishEvent.class)) {
			Skript.error("The 'experience' expression can only be used in experience spawn, " +
				"block break, player experience change, entity breeding or fishing events");
			return false;
		}

		return true;
	}

	@Override
	protected Experience @Nullable [] get(Event event) {
		Experience[] exp;

		if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
			exp = new Experience[]{new Experience(experienceSpawnEvent.getSpawnedXP())};
		} else if (event instanceof BlockBreakEvent blockBreakEvent) {
			exp = new Experience[]{new Experience(blockBreakEvent.getExpToDrop())};
		} else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
			exp = new Experience[]{new Experience(playerExpChangeEvent.getAmount())};
		} else if (event instanceof EntityBreedEvent entityBreedEvent) {
			exp = new Experience[]{new Experience(entityBreedEvent.getExperience())};
		} else if (event instanceof PlayerFishEvent fishEvent) {
			exp = new Experience[]{new Experience(fishEvent.getExpToDrop())};
		} else {
			exp = new Experience[0];
		}

		return exp;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Experience.class, Integer.class);
			case ADD, REMOVE -> CollectionUtils.array(Experience[].class, Integer[].class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int exp;

		if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
			exp = experienceSpawnEvent.getSpawnedXP();
		} else if (event instanceof BlockBreakEvent blockBreakEvent) {
			exp = blockBreakEvent.getExpToDrop();
		} else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
			exp = playerExpChangeEvent.getAmount();
		} else if (event instanceof EntityBreedEvent entityBreedEvent) {
			exp = entityBreedEvent.getExperience();
		} else if (event instanceof PlayerFishEvent fishEvent) {
			exp = fishEvent.getExpToDrop();
		} else {
			return;
		}

		if (delta != null) {
			for (Object object : delta) {
				int value = object instanceof Experience experience ? experience.getXP() : (int) object;
				switch (mode) {
					case ADD -> exp += value;
					case SET -> exp = value;
					case REMOVE, REMOVE_ALL -> exp -= value;
				}
			}
		} else {
			exp = 0;
		}

		exp = Math.max(0, exp);
		if (event instanceof ExperienceSpawnEvent experienceSpawnEvent) {
			experienceSpawnEvent.setSpawnedXP(exp);
		} else if (event instanceof BlockBreakEvent blockBreakEvent) {
			blockBreakEvent.setExpToDrop(exp);
		} else if (event instanceof PlayerExpChangeEvent playerExpChangeEvent) {
			playerExpChangeEvent.setAmount(exp);
		} else if (event instanceof EntityBreedEvent entityBreedEvent) {
			entityBreedEvent.setExperience(exp);
		} else if (event instanceof PlayerFishEvent fishEvent) {
			fishEvent.setExpToDrop(exp);
		}
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
