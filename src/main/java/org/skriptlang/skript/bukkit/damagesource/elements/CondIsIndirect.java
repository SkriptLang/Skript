package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;

@Name("Damage Source - Is Indirect")
@Description({
	"Whether the damage from a damage source is indirect.",
	"Vanilla damage sources are considered indirect if the 'causing entity' and the 'direct entity' are not the same. "
		+ "(i.e. taking damage from an arrow that was shot by an entity)",
	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
})
@Example("""
	on damage:
		if event-damage source is indirect:
	""")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondIsIndirect extends PropertyCondition<DamageSource> implements DamageSourceExperiment {

	static {
		register(CondIsIndirect.class, "[:in]direct", "damagesources");
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
		return indirect ? "indirect" : "direct";
	}

}
