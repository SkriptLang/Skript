package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;

@Name("Damage Source - Does Scale With Difficulty")
@Description({
	"Whether the damage from a damage source scales with the difficulty of the server.",
	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
})
@Example("""
		on death:
			if event-damage source scales with difficulty:
		""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondScalesWithDifficulty extends PropertyCondition<DamageSource> implements DamageSourceExperiment {

	static {
		Skript.registerCondition(CondScalesWithDifficulty.class,
			"%damagesources% (does scale|scales) with difficulty",
			"%damagesources% (does not|doesn't) scale with difficulty");
	}

	private Expression<DamageSource> sources;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		sources = (Expression<DamageSource>) exprs[0];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(DamageSource damageSource) {
		return damageSource.scalesWithDifficulty();
	}

	@Override
	protected String getPropertyName() {
		return "scales with difficulty";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(sources);
		if (isNegated()) {
			builder.append("does not scale");
		} else {
			builder.append("scales");
		}
		builder.append("with difficulty");
		return builder.toString();
	}

}
