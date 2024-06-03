package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("FishState")
@Description({"Returns fish entity|hook in FishEvent"})
@Since("2.8.6")
public class ExprFishingEntity extends SimpleExpression<Entity> {
	private int pattern;
	static {
		Skript.registerExpression(ExprFishingEntity.class, Entity.class, ExpressionType.SIMPLE,
			"1¦([(event|fish[ing])( |-)]caught)|2¦([(event|fish[ing])( |-)]hook)");
	}
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event == null)
			return "the fishing entity";
		return Classes.getDebugMessage(getSingle(event));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing entity' expression can only be used in fish event");
			return false;
		}
		pattern = parseResult.mark;
		return true;
	}

	@Override
	protected @Nullable Entity[] get(Event event) {
		if (event instanceof PlayerFishEvent) {
			switch (pattern) {
				case 1:
					return new Entity[]{((PlayerFishEvent) event).getCaught()};
				case 2:
					return new Entity[]{((PlayerFishEvent) event).getHook()};
			}
		}
		return null;
	}
}
