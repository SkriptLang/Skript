package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.SpawnerUtils;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Trial Spawner Configuration - Mob Count per Player")
@Description({
	"Returns the total or simultaneous amount of mobs added per player within the spawner's range.",
	"For example, setting the total mob count per player to 5, "
		+ "will make the spawner spawn 5 mobs per player, "
		+ "and setting the simultaneous mob count per player to 2, "
		+ "will make the spawner spawn 2 mobs at once per player.",
	"The default value for the total mob count added per player is 2 and"
		+ " for the simultaneous mob count added per player is 1."
})
@Examples({
	"send \"The trial spawner is spawning %the total trial spawner mob count per player of {_spawner}% mobs in total per player.\"",
	"send \"The trial spawner is spawning %the simultaneous trial spawner mob count per player of {_spawner}% mobs per player.\"",
	"",
	"set the total trial spawner mob count per player of {_spawner} to 10",
	"set the simultaneous trial spawner mob count per player of {_spawner} to 4",
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprTrialConfigMobCountPerPlayer extends PropertyExpression<TrialSpawnerConfig, Float> {

	static {
		var info = SyntaxInfo.Expression.builder(ExprTrialConfigMobCountPerPlayer.class, Float.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(ExprTrialConfigMobCountPerPlayer::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"[the] (:simultaneous|total) trial [spawner] mob (amount|count)[s] per player of %trialspawnerconfigs%",
				"%trialspawnerconfigs%'[s] (:simultaneous|total) trial [spawner] mob (amount|count)[s] per player")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EXPRESSION, info);
	}

	private boolean additional;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends TrialSpawnerConfig>) exprs[0]);
		additional = parseResult.hasTag("simultaneous");
		return true;
	}

	@Override
	protected Float[] get(Event event, TrialSpawnerConfig[] source) {
		List<Float> values = new ArrayList<>();
		for (TrialSpawnerConfig config : source) {
			if (additional) {
				values.add(config.config().getAdditionalSimultaneousEntities());
			} else {
				values.add(config.config().getBaseSimultaneousEntities());
			}
		}

		return values.toArray(new Float[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Float.class);
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
					case SET -> config.setAdditionalSimultaneousEntities(amount);
					case ADD -> config.setAdditionalSimultaneousEntities(config.getAdditionalSimultaneousEntities() + amount);
					case REMOVE -> config.setAdditionalSimultaneousEntities(config.getAdditionalSimultaneousEntities() - amount);
					case RESET -> config.setAdditionalSimultaneousEntities(1); // default value
				}
			} else {
				switch (mode) {
					case SET -> config.setBaseSimultaneousEntities(amount);
					case ADD -> config.setBaseSimultaneousEntities(config.getBaseSimultaneousEntities() + amount);
					case REMOVE -> config.setBaseSimultaneousEntities(config.getBaseSimultaneousEntities() - amount);
					case RESET -> config.setBaseSimultaneousEntities(2); // default value
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
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		if (additional) {
			builder.append("additional");
		} else {
			builder.append("base");
		}
		builder.append("simultaneous tracked entity amount of", getExpr());

		return builder.toString();
	}

}
