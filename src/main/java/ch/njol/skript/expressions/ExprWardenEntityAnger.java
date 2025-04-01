package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Warden;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Name("Warden Anger Level")
@Description({
	"The anger level a warden feels towards an entity.",
	"A warden can be angry towards multiple entities with different anger levels.",
	"If an entity reaches an anger level of 80+, the warden will pursue it.",
	"Anger level maxes out at 150."
})
@Examples({
	"set the anger level of last spawned warden towards player to 20",
	"clear the last spawned warden's anger level towards player"
})
@Since("INSERT VERSION")
public class ExprWardenEntityAnger extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprWardenEntityAnger.class, Integer.class, ExpressionType.COMBINED,
			"[the] anger level [of] %livingentities% towards %livingentities%",
			"%livingentities%'[s] anger level towards %livingentities%");
	}

	private Expression<LivingEntity> wardens;
	private Expression<LivingEntity> targets;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wardens = (Expression<LivingEntity>) exprs[0];
		//noinspection unchecked
		targets = (Expression<LivingEntity>) exprs[1];
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		List<Integer> list = new ArrayList<>();
		Entity[] entities = this.targets.getArray(event);
		for (LivingEntity livingEntity : wardens.getArray(event)) {
			if (!(livingEntity instanceof Warden warden))
				continue;
			for (Entity entity : entities) {
				list.add(warden.getAnger(entity));
			}
		}
		return list.toArray(new Integer[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = delta != null ? (Integer) delta[0] : 0;
		BiConsumer<Warden, Entity> consumer = switch (mode) {
			case SET -> (warden, entity) -> warden.setAnger(entity, Math2.fit(0, value, 150));
			case DELETE -> Warden::clearAnger;
			case ADD -> (warden, entity) -> {
				int current = warden.getAnger(entity);
				int newValue = Math2.fit(0, current + value, 150);
				warden.setAnger(entity, newValue);
			};
			case REMOVE -> (warden, entity) -> {
				int current = warden.getAnger(entity);
				int newValue = Math2.fit(0, current - value, 150);
				warden.setAnger(entity, newValue);
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		Entity[] entities = this.targets.getArray(event);
		for (LivingEntity livingEntity : wardens.getArray(event)) {
			if (!(livingEntity instanceof Warden warden))
				continue;
			for (Entity entity : entities) {
				consumer.accept(warden, entity);
			}
		}
	}

	@Override
	public boolean isSingle() {
		return wardens.isSingle() && targets.isSingle();
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the anger level of " + wardens.toString(event, debug) + " towards " + targets.toString(event, debug);
	}

}
