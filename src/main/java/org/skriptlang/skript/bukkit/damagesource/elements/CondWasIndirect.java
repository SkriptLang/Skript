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

@Name("Damage Source - Was Indirectly Caused")
@Description({
	"Whether the damage from a damage source was indirectly caused.",
	"Vanilla damage sources are considered indirect if the 'causing entity' and the 'direct entity' are not the same. "
		+ "For example, taking damage from an arrow that was shot by an entity."
})
@Example("""
	on damage:
		if event-damage source was indirectly caused:
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class CondWasIndirect extends PropertyCondition<DamageSource> implements DamageSourceExperimentSyntax {

	static {
		Skript.registerCondition(CondWasIndirect.class, ConditionType.PROPERTY,
			"%damagesources% (was|were) ([:in]directly caused|caused [:in]directly)",
			"%damagesources% (was not|wasn't|were not|weren't) ([:in]directly caused|caused [:in]directly)"
		);
	}

	private boolean indirect;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		indirect = parseResult.hasTag("in");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(DamageSource damageSource) {
		return damageSource.isIndirect() == indirect;
	}

	@Override
	protected String getPropertyName() {
		return indirect ? "indirectly caused" : "directly caused";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(getExpr());
		if (getExpr().isSingle()) {
			builder.append("was");
		} else {
			builder.append("were");
		}
		if (isNegated())
			builder.append("not");
		if (indirect) {
			builder.append("indirectly");
		} else {
			builder.append("directly");
		}
		builder.append("caused");
		return builder.toString();
	}

}
