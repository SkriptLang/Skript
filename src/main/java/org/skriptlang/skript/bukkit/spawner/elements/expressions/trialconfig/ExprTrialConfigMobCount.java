package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;

@Name("Trial Spawner Configuration - Mob Count")
@Description({
	"Returns the total or simultaneous mob count of the trial spawner configuration.",
	"Both the simultaneous and total mob count "
		+ "increases once another player comes within the spawner's range. "
		+ "For each additional player present, the simultaneous mob count "
		+ "increases by the simultaneous mob count per player, "
		+ "and the total mob count increases by the total mob count per player. "
		+ "Assuming you are using the default values, with 2 players,"
		+ "8 mobs spawn in total with 3 at once, "
		+ "and with 3 players, 10 mobs spawn in total with 4 at once.",
	"The default value for the total mob count is 2, and the default value for the simultaneous mob count is 6.",
	"The trial spawner will stop spawning mobs if the number of living mobs spawned by it reached the total mob count."
})
@Examples({
	"send \"The trial spawner is spawning %the total trial spawner mob count of {_spawner}% mobs in total.\"",
	"send \"The trial spawner is spawning %the simultaneous trial spawner mob count of {_spawner}% mobs at once.\"",
	"",
	"set the total trial spawner mob count of {_spawner} to 10",
	"set the simultaneous trial spawner mob count of {_spawner} to 4",
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.21+")
public class ExprTrialConfigMobCount extends SimplePropertyExpression<TrialSpawnerConfig, Float> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprTrialConfigMobCount.class, Float.class,
			"(:simultaneous|total) trial [spawner] mob (count|amount)", "trialspawnerconfigs");
	}

	private boolean additional;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		additional = parseResult.hasTag("simultaneous");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Float convert(TrialSpawnerConfig config) {
		if (additional)
			return config.config().getAdditionalSpawnsBeforeCooldown();
		return config.config().getBaseSpawnsBeforeCooldown();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		float amount = (float) delta[0];

		for (TrialSpawnerConfig trialConfig : getExpr().getArray(event)) {
			TrialSpawnerConfiguration config = trialConfig.config();
			if (additional) {
				switch (mode) {
					case SET -> config.setAdditionalSpawnsBeforeCooldown(amount);
					case ADD -> config.setAdditionalSpawnsBeforeCooldown(config.getAdditionalSpawnsBeforeCooldown() + amount);
					case REMOVE -> config.setAdditionalSpawnsBeforeCooldown(config.getAdditionalSpawnsBeforeCooldown() - amount);
				}
			} else {
				switch (mode) {
					case SET -> config.setBaseSpawnsBeforeCooldown(amount);
					case ADD -> config.setBaseSpawnsBeforeCooldown(config.getBaseSpawnsBeforeCooldown() + amount);
					case REMOVE -> config.setBaseSpawnsBeforeCooldown(config.getBaseSpawnsBeforeCooldown() - amount);
				}
			}

			SpawnerUtils.updateState(trialConfig.state());
		}
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return (additional ? "additional " : "base ") + "spawns";
	}

}
