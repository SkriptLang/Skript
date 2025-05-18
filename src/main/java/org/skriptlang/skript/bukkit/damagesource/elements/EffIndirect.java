package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceExperiment;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceWrapper;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

//@Name("Damage Source - Indirect")
//@Description({
//	"Make the damage from a damage source be indirect or direct.",
//	"Vanilla damage sources are considered indirect if the 'causing entity' and the 'direct entity' are not the same. "
//		+ "(i.e. taking damage from an arrow that was shot by an entity)",
//	"Cannot change any attributes of the damage source from an 'on damage' or 'on death' event."
//})
//@Example("""
//	set {_source} to a new custom damage source:
//		set the damage type to magic
//		set the causing entity to {_player}
//		set the direct entity to {_arrow}
//		set the damage location to location(0, 0, 10)
//		set the source location to location(10, 0, 0)
//		set the food exhaustion to 10
//		make event-damage source be indirect
//		make event-damage source scale with difficulty
//	damage all players by 5 using {_source}
//	""")
//@Since("INSERT VERSION")
//@RequiredPlugins("Minecraft 1.20.4+")
public class EffIndirect extends Effect implements SyntaxRuntimeErrorProducer, DamageSourceExperiment {

//	static {
//		Skript.registerEffect(EffIndirect.class,
//			"make %damagesources% [be] [:in]direct");
//	}

	private Expression<DamageSource> sources;
	private boolean indirect;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		sources = (Expression<DamageSource>) exprs[0];
		indirect = parseResult.hasTag("in");
		node = getParser().getNode();
		return true;
	}

	@Override
	protected void execute(Event event) {
		boolean hasFinal = false;
		for (DamageSource damageSource : sources.getArray(event)) {
			if (!(damageSource instanceof DamageSourceWrapper wrapper)) {
				hasFinal = true;
				continue;
			}
			wrapper.setIndirect(indirect);
		}
		if (hasFinal)
			error("You cannot change the 'indirect' attribute of a finalized damage source.");
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make the damage of", sources, "be");
		if (indirect) {
			builder.append("indirect");
		} else {
			builder.append("direct");
		}
		return builder.toString();
	}

}
