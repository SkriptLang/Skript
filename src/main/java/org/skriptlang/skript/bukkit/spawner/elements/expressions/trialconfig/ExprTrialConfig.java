package org.skriptlang.skript.bukkit.spawner.elements.expressions.trialconfig;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.bukkit.spawner.util.TrialSpawnerConfig;

@Name("Trial Spawner Configuration")
@Description({
	"Returns the ominous or normal configuration of the trial spawner. "
		+ "If neither ominous or normal is specified, the current configuration "
		+ "of the trial spawner will be returned."
})
@Examples({
	"set {_normal config} to normal trial spawner config of event-block",
	"",
	"set {_config} to trial spawner config of event-block",
	"if event-block is ominous:",
		"\tsend \"{_config} is ominous!\"",
	"else:",
		"\tsend \"{_config} is normal!\""
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprTrialConfig extends SimplePropertyExpression<Block, TrialSpawnerConfig> {

	static {
		register(SpawnerModule.SYNTAX_REGISTRY, ExprTrialConfig.class, TrialSpawnerConfig.class,
			"[1:ominous|2:normal] trial spawner config[uration]", "blocks");
	}

	private ConfigType type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = ConfigType.values()[parseResult.mark];
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable TrialSpawnerConfig convert(Block block) {
		if (block.getState() instanceof TrialSpawner spawner) {
			return switch (type) {
				case OMINOUS -> new TrialSpawnerConfig(spawner.getOminousConfiguration(), spawner, true);
				case NORMAL -> new TrialSpawnerConfig(spawner.getNormalConfiguration(), spawner, false);
				case CURRENT -> new TrialSpawnerConfig(spawner.isOminous()
					? spawner.getOminousConfiguration()
					: spawner.getNormalConfiguration(), spawner, spawner.isOminous());
			};
		}
		return null;
	}

	@Override
	public Class<? extends TrialSpawnerConfig> getReturnType() {
		return TrialSpawnerConfig.class;
	}

	@Override
	protected String getPropertyName() {
		return type.name().toLowerCase() + " trial spawner config";
	}

	private enum ConfigType {
		CURRENT, OMINOUS, NORMAL
	}

}
