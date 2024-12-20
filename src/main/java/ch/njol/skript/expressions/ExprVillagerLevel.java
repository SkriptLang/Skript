package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Level")
@Description({
	"Represents the level of a villager.",
	"Level must be between 1 and 5, with 1 being the default level.",
	"Do note when a villager's level is 1, they may lose their profession."})
@Examples({
	"set {_level} to villager level of {_villager}",
	"set villager level of last spawned villager to 2",
	"add 1 to villager level of target entity",
	"remove 1 from villager level of event-entity",
	"reset villager level of event-entity"
})
@Since("INSERT VERSION")
public class ExprVillagerLevel extends SimplePropertyExpression<LivingEntity, Number> {

	private static final boolean HAS_INCREASE_METHOD = Skript.methodExists(Villager.class, "increaseLevel", int.class);

	static {
		register(ExprVillagerLevel.class, Number.class, "villager level", "livingentities");
	}

	@Override
	public @Nullable Number convert(LivingEntity from) {
		if (from instanceof Villager villager)
			return villager.getVillagerLevel();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET ->
				CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number number = delta != null && delta[0] instanceof Number num ? num : 1;
		int changeValue = number.intValue();

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (!(livingEntity instanceof Villager villager)) continue;

			int previousLevel = villager.getVillagerLevel();
			int newLevel = switch (mode) {
				case SET -> changeValue;
				case ADD -> previousLevel + changeValue;
				case REMOVE -> previousLevel - changeValue;
				default -> 1;
			};
			newLevel = Math2.fit(1, newLevel,  5);
			if (newLevel > previousLevel && HAS_INCREASE_METHOD) {
				int increase = Math2.fit(1, newLevel - previousLevel,  5);
				// According to the docs for this method:
				// Increases the level of this villager.
				// The villager will also unlock new recipes unlike the raw 'setVillagerLevel' method
				villager.increaseLevel(increase);
			} else {
				villager.setVillagerLevel(newLevel);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "villager level";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
