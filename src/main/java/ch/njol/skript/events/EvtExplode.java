package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.EffExplosion;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.jetbrains.annotations.Nullable;

public class EvtExplode extends SkriptEvent {

	static {
		Skript.registerEvent("Explode", EvtExplode.class,
				CollectionUtils.array(EffExplosion.ScriptExplodeEvent.class, FireworkExplodeEvent.class, EntityExplodeEvent.class),
				"[a] script[ed] explo(d(e|ing)|sion)",
				"[a] [%-entitytypes%] explo(d(e|ing)|sion)"
			)
			.description(
				"Called when an entity explodes, or when an explosion is created by a script.",
				"The power of the explosion can be obtained by using `event-number`."
			)
			.examples(
				"on explosion:",
				"on script explosion:",
				"on tnt explosion:"
			)
			.since("1.0, INSERT VERSION (script)");
	}

	private int pattern;
	private @Nullable Literal<? extends EntityType> typesLiteral;
	private EntityType @Nullable [] types;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern;

		if (matchedPattern == 2) {
			//noinspection unchecked
			Literal<? extends EntityType> arg = (Literal<? extends EntityType>) args[0];
			if (arg != null) {
				typesLiteral = arg;
				types = arg.getAll();
			}
		}

		return true;
	}

	@Override
	public boolean check(Event event) {
		if (pattern == 0 && event instanceof EffExplosion.ScriptExplodeEvent) {
			return true;
		} else if (pattern == 2 && event instanceof EntityExplodeEvent explodeEvent) {
			if (types == null)
				return true;

			for (EntityType type : types) {
				if (type.isInstance(explodeEvent.getEntity()))
					return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (pattern) {
			case 0 -> "script explosion";
			case 2 -> typesLiteral != null ? typesLiteral.toString(event, debug) + " explosion" : "explosion";
			default -> "unknown explosion";
		};
	}

}
