package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Force Attack")
@Description({
	"Makes a living entity attack an entity with a melee attack.",
	"Using 'attack' will make the attacker use the item in their main hand "
		+ "and will apply extra data from the item, including enchantments and attributes.",
	"Using 'damage' with a number of hearts will not account for the item in the main hand "
		+ "and will always be the number provided."
})
@Example("""
	spawn a wolf at location(0, 0, 0)
	make last spawned wolf attack all players
	""")
@Example("""
	spawn a zombie at location(0, 0, 0)
	make player damage last spawned zombie by 2
	""")
@Since("2.5.1, 2.13 (multiple, amount)")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffForceAttack extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		Skript.registerEffect(EffForceAttack.class,
			"make %livingentities% attack %entities%",
			"force %livingentities% to attack %entities%",
			"make %livingentities% damage %entities% by %number% [heart[s]]",
			"force %livingentities% to damage %entities% by %number% [heart[s]]");
	}

	private Expression<LivingEntity> attackers;
	private Expression<Entity> victims;
	private @Nullable Expression<Number> amount;
	private Node node;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		attackers = (Expression<LivingEntity>) exprs[0];
		victims = (Expression<Entity>) exprs[1];
		if (matchedPattern >= 2)
			amount = (Expression<Number>) exprs[2];
		node = getParser().getNode();
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Double amount = null;
		if (this.amount != null) {
			Number number = this.amount.getSingle(event);
			if (number == null)
				return;
			Double preAmount = number.doubleValue();
			if (preAmount <= 0) {
				error("Cannot damage an entity by 0 or less. Consider healing instead.");
				return;
			} else if (!Double.isFinite(preAmount)) {
				return;
			}
			amount = preAmount * 2; // hearts
		}

		LivingEntity[] attackers = this.attackers.getArray(event);
		Entity[] victims = this.victims.getArray(event);
		if (amount == null) {
			for (Entity victim : victims) {
				for (LivingEntity attacker : attackers) {
					attacker.attack(victim);
				}
			}
		} else {
			for (Entity victim : victims) {
				if (!(victim instanceof Damageable damageable))
					continue;
				for (LivingEntity attacker : attackers) {
					damageable.damage(amount, attacker);
				}
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", attackers);
		if (amount == null) {
			builder.append("attack", victims);
		} else {
			builder.append("damage", victims, "by", amount);
		}
		return builder.toString();
	}
	
}
