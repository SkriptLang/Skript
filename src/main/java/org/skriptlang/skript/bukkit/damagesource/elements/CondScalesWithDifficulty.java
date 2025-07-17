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
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperimentSyntax;

@Name("Damage Source - Does Scale With Difficulty")
@Description("Whether the damage from a damage source scales with the difficulty of the server.")
@Example("""
	on death:
		if event-damage source scales damage with difficulty:
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class CondScalesWithDifficulty extends PropertyCondition<DamageSource> implements DamageSourceExperimentSyntax {

	static {
		Skript.registerCondition(CondScalesWithDifficulty.class,
			"%damagesources% ((does|do) scale|scales) damage with difficulty",
			"%damagesources% (do not|don't|does not|doesn't) scale damage with difficulty",
			"%damagesources%'[s] damage ((does|do) scale|scales) with difficulty",
			"%damagesources%'[s] damage (do not|don't|does not|doesn't) scale with difficulty");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends DamageSource>) exprs[0]);
		setNegated(matchedPattern % 2 == 1);
		return true;
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
		builder.append(getExpr());
		if (isNegated()) {
			builder.append("does not scale");
		} else {
			builder.append("scales");
		}
		builder.append("with difficulty");
		return builder.toString();
	}

}
