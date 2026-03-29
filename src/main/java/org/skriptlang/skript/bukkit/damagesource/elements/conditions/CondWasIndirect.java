package org.skriptlang.skript.bukkit.damagesource.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Wound's Source — Was Indirectly Wrought")
@Description({
	"Whether the harm from a damage source was indirectly wrought.",
	"Vanilla damage sources art deemed indirect if the 'causing entity' and the 'direct entity' be not one and the same."
		+ "For example, suffering harm from an arrow loosed by another creature."
})
@Example("""
    on damage:
    	if event-damage source was indirectly wrought:
    """)
@Since("2.12")
public class CondWasIndirect extends PropertyCondition<DamageSource> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondWasIndirect.class)
				.addPatterns(
					"%damagesources% (was|were) ([:in]directly wrought|wrought [:in]directly)",
					"%damagesources% (was not|wasn't|were not|weren't) ([:in]directly wrought|wrought [:in]directly)")
				.supplier(CondWasIndirect::new)
				.build()
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
