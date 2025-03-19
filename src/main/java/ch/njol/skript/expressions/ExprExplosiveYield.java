package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Ghast;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Name("Explosive Yield")
@Description({
	"The yield of an explosive (creeper, ghast, primed tnt, fireball, etc.). This is how big of an explosion is caused by the entity.",
	"Read <a href='https://minecraft.wiki/w/Explosion'>this wiki page</a> for more information.",
	"The yield of ghasts can only be set to between 0 and 127."
})
@Examples({
	"on spawn of a creeper:",
		"\tset the explosive yield of the event-entity to 10"
})
@RequiredPlugins("Paper (ghasts)")
@Since("2.5, INSERT VERSION (ghasts)")
public class ExprExplosiveYield extends SimplePropertyExpression<Entity, Number> {

	private static final boolean SUPPORTS_GHASTS = Skript.methodExists(Ghast.class, "getExplosionPower");

	static {
		register(ExprExplosiveYield.class, Number.class, "explosive (yield|radius|size|power)", "entities");
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		if (entity instanceof Explosive explosive) {
			return explosive.getYield();
		} else if (entity instanceof Creeper creeper) {
			return creeper.getExplosionRadius();
		} else if (SUPPORTS_GHASTS && entity instanceof Ghast ghast) {
			return ghast.getExplosionPower();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number number = delta != null ? (Number) delta[0] : 0;
		float floatValue = Math2.fit(0, number.floatValue(), Float.MAX_VALUE);
		int intValue = Math2.fit(0, number.intValue(), Integer.MAX_VALUE);

		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Explosive explosive) {
				changeExplosion(mode, floatValue, explosive::getYield, explosive::setYield, Float.MAX_VALUE);
			} else if (entity instanceof Creeper creeper) {
				changeExplosion(mode, intValue, creeper::getExplosionRadius, creeper::setExplosionRadius, Integer.MAX_VALUE);
			} else if (SUPPORTS_GHASTS && entity instanceof Ghast ghast) {
				changeExplosion(mode, intValue, ghast::getExplosionPower, ghast::setExplosionPower, 127);
			}
		}
	}

	private void changeExplosion(ChangeMode mode, float value, Supplier<Float> getter, Consumer<Float> setter, float max) {
		setter.accept(Math2.fit(0F,
			switch (mode) {
				case SET, DELETE -> value;
				case ADD -> getter.get() + value;
				case REMOVE -> getter.get() - value;
				default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
			},
			max));
	}

	private void changeExplosion(ChangeMode mode, int value, Supplier<Integer> getter, Consumer<Integer> setter, int max) {
		setter.accept(Math2.fit(0,
				switch (mode) {
					case SET, DELETE -> value;
					case ADD -> getter.get() + value;
					case REMOVE -> getter.get() - value;
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				},
			max));
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "explosive yield";
	}

}
