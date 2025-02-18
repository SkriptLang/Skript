package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Hatching Entity Type")
@Description("The type of the entity that will be hatched in a Player Egg Throw event.")
@Examples({
	"on player egg throw:",
		"\tset the hatching entity type to a primed tnt"
})
@Events("Egg Throw")
@Since("2.7")
public class ExprHatchingType extends SimpleExpression<EntityData<?>> {

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprHatchingType.class, (Class<EntityData<?>>) (Class<?>) EntityData.class, ExpressionType.SIMPLE,
				"[the] hatching entity [type]"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerEggThrowEvent.class)) {
			Skript.error("You can't use 'the hatching entity type' outside of a Player Egg Throw event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected EntityData<?>[] get(Event event) {
		if (!(event instanceof PlayerEggThrowEvent))
			return new EntityData[0];
		return new EntityData[]{EntityUtils.toSkriptEntityData(((PlayerEggThrowEvent) event).getHatchingType())};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return CollectionUtils.array(EntityData.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (!(event instanceof PlayerEggThrowEvent))
			return;
		//noinspection ConstantConditions
		EntityType entityType = delta != null ? EntityUtils.toBukkitEntityType((EntityData<?>) delta[0]) : EntityType.CHICKEN;
		if (!entityType.isSpawnable())
			return;
		((PlayerEggThrowEvent) event).setHatchingType(entityType);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends EntityData<?>> getReturnType() {
		//noinspection unchecked
		return (Class<EntityData<?>>) (Class<?>) EntityData.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the hatching entity type";
	}

}
