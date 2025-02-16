package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Creature/Entity/Player/Projectile/Villager/Powered Creeper/etc.")
@Description({
	"The entity involved in an event (an entity is a player, a creature or an inanimate object like ignited TNT, a dropped item or an arrow).",
	"You can use the specific type of the entity that's involved in the event, e.g. in a 'death of a creeper' event you can use 'the creeper' instead of 'the entity'."
})
@Examples({
	"give a diamond sword of sharpness 3 to the player",
	"kill the creeper",
	"kill all powered creepers in the wolf's world",
	"projectile is an arrow"
})
@Since("1.0")
public class ExprEntity extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprEntity.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "[the] [event-]<.+>");
	}

	private EventValueExpression<Entity> entity;
	private EntityData<?> type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			if (!StringUtils.startsWithIgnoreCase(parseResult.expr, "the ") && !StringUtils.startsWithIgnoreCase(parseResult.expr, "event-")) {
				
				String input = parseResult.regexes.get(0).group();
				ItemType item = Aliases.parseItemType("" + input);
				log.clear();
				if (item != null) {
					log.printLog();
					return false;
				}
			}
			
			EntityData<?> type = EntityData.parseWithoutIndefiniteArticle("" + parseResult.regexes.get(0).group());
			log.clear();
			log.printLog();
			if (type == null || type.isPlural().isTrue())
				return false;
			this.type = type;
		} finally {
			log.stop();
		}
		entity = new EventValueExpression<>(type.getType());
		return entity.init(matchedPattern, isDelayed, parseResult);
	}

	@Override
	protected Entity @Nullable [] get(Event event) {
		Entity[] entities = entity.getArray(event);
		if (entities.length == 0 || type.isInstance(entities[0]))
			return entities;
		return null;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		for (Class<R> t : to) {
			if (t.equals(EntityData.class)) {
				return new SimpleLiteral<>((R) type, false);
			}
		}
		return super.getConvertedExpression(to);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + type;
	}

}
