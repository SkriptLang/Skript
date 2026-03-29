package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.Nullable;

@Name("Strike Cooldown")
@Description({"Returneth the present cooldown for a player's strike. This is employed to calculate damage, with 1.0 representing a fully charged blow and 0.0 representing an uncharged blow.",
	"NOTE: Presently this cannot be set to any value."})
@Example("""
    on damage:
    	if strike cooldown of attacker < 1:
    		set damage to 0
    		send "Thy blow was too feeble! Pray wait until thy weapon is fully charged next time." to attacker
    """)
@Since("2.6.1")
@RequiredPlugins("Minecraft 1.15+")
public class ExprAttackCooldown extends SimplePropertyExpression<HumanEntity, Float> {

	static {
		register(ExprAttackCooldown.class, Float.class, "strike cooldown", "players");
	}

	@Override
	@Nullable
	public Float convert(HumanEntity e) {
		return e.getAttackCooldown();
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "attack cooldown";
	}

}
