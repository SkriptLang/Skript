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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

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
		"\tset dropped experience to 10"
})
@Since("2.1, 2.5.3 (block break event), 2.7 (experience change event), INSERT VERSION (breeding event)")
@Events({"experience spawn", "break / mine", "experience change", "entity breeding"})
public class ExprExperience extends SimpleExpression<Experience> {

	static {
		Skript.registerExpression(ExprExperience.class, Experience.class, ExpressionType.SIMPLE,
			"[the] (spawned|dropped|) [e]xp[erience] [orb[s]]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ExperienceSpawnEvent.class, BlockBreakEvent.class,
			PlayerExpChangeEvent.class, EntityBreedEvent.class)) {
			Skript.error("The experience expression can only be used in experience spawn, " +
				"block break, player experience change and entity breeding events");
			return false;
		}

		return true;
	}
	
	@Override
	protected @Nullable Experience[] get(Event event) {
		return switch (event) {
			case ExperienceSpawnEvent experienceSpawnEvent ->
				new Experience[]{new Experience(experienceSpawnEvent.getSpawnedXP())};
			case BlockBreakEvent blockBreakEvent ->
				new Experience[]{new Experience(blockBreakEvent.getExpToDrop())};
			case PlayerExpChangeEvent playerExpChangeEvent ->
				new Experience[]{new Experience(playerExpChangeEvent.getAmount())};
			case EntityBreedEvent entityBreedEvent ->
				new Experience[]{new Experience(entityBreedEvent.getExperience())};
			default -> new Experience[0];
		};
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Experience.class, Integer.class);
			case ADD, REMOVE -> CollectionUtils.array(Experience[].class, Integer[].class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int exp = 0;

		switch (event) {
			case ExperienceSpawnEvent experienceSpawnEvent -> exp = experienceSpawnEvent.getSpawnedXP();
			case BlockBreakEvent blockBreakEvent -> exp = blockBreakEvent.getExpToDrop();
			case PlayerExpChangeEvent playerExpChangeEvent -> exp = playerExpChangeEvent.getAmount();
			case EntityBreedEvent entityBreedEvent -> exp = entityBreedEvent.getExperience();

			default -> {
				return;
			}
		}

		if (delta != null) {
			for (Object object : delta) {
				int value = object instanceof Experience experience ? experience.getXP() : (int) object;
				switch (mode) {
					case ADD -> exp += value;
					case SET -> exp = value;
					case REMOVE -> exp -= value;
				}
			}
		}

		exp = Math.max(0, exp);
		switch (event) {
			case ExperienceSpawnEvent experienceSpawnEvent -> experienceSpawnEvent.setSpawnedXP(exp);
			case BlockBreakEvent blockBreakEvent -> blockBreakEvent.setExpToDrop(exp);
			case PlayerExpChangeEvent playerExpChangeEvent -> playerExpChangeEvent.setAmount(exp);
			case EntityBreedEvent entityBreedEvent -> entityBreedEvent.setExperience(exp);
			default -> {}
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
