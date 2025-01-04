package org.skriptlang.skript.bukkit.spawner.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.TrialSpawner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EffMakeOminous extends Effect {

	static {
		var info = SyntaxInfo.builder(EffMakeOminous.class)
			.origin(SyntaxOrigin.of(Skript.instance()))
			.supplier(EffMakeOminous::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns("make [the] [trial] spawner state of %blocks% (1:ominous|regular)")
			.build();

		SpawnerModule.SYNTAX_REGISTRY.register(SyntaxRegistry.EFFECT, info);
	}

	private boolean ominous;
	private Expression<Block> spawners;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ominous = parseResult.mark == 1;
		//noinspection unchecked
		spawners = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : spawners.getArray(event)) {
			if (!(block.getState() instanceof TrialSpawner spawner))
				continue;

			spawner.setOminous(ominous);

			spawner.update(true, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("make the trial spawner state of", spawners);
		if (ominous) {
			builder.append("ominous");
		} else {
			builder.append("regular");
		}

		return builder.toString();
	}

}
