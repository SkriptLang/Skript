package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Villager Level/Experience")
@Description({
	"Represents the level/experience of a villager.",
	"The level will determine which trades are available to players (value between 1 and 5, defaults to 1).",
	"When a villager's level is 1, they may lose their profession if they don't have a workstation.",
	"Experience works along with the leveling system, determining which level the villager will move to.",
	"Experience must be greater than or equal to 0.",
	"Learn more about villager levels on <a href='https://minecraft.wiki/w/Trading#Level'>Minecraft Wiki</a>"
})
@Examples({
	"set {_level} to villager level of {_villager}",
	"set villager level of last spawned villager to 2",
	"add 1 to villager level of target entity",
	"remove 1 from villager level of event-entity",
	"reset villager level of event-entity",
	"set villager experience of last spawned entity to 100"
})
@Since("2.10")
public class ExprVillagerLevel extends SimplePropertyExpression<LivingEntity, Number> {

	private static final boolean HAS_INCREASE_METHOD = Skript.methodExists(Villager.class, "increaseLevel", int.class);

	static {
		register(ExprVillagerLevel.class, Number.class, "villager (level|:experience)", "livingentities");
	}

	private boolean experience;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		this.experience = parseResult.hasTag("experience");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Number convert(LivingEntity from) {
		if (from instanceof Villager villager)
			return experience ? villager.getVillagerExperience() : villager.getVillagerLevel();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Number number = delta != null && delta[0] instanceof Number num ? num : 1;
		int changeValue = number.intValue();

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (!(livingEntity instanceof Villager villager)) continue;

			int minLevel;
			int maxLevel;
			int previousAmount;
			if (experience) {
				minLevel = 0;
				maxLevel = Integer.MAX_VALUE;
				previousAmount = villager.getVillagerExperience();
			} else {
				minLevel = 1;
				maxLevel = 5;
				previousAmount = villager.getVillagerLevel();
			}
			int newLevel = switch (mode) {
				case SET -> changeValue;
				case ADD -> previousAmount + changeValue;
				case REMOVE -> previousAmount - changeValue;
				default -> minLevel;
			};
			newLevel = Math2.fit(minLevel, newLevel, maxLevel);
			if (experience) {
				villager.setVillagerExperience(newLevel);
			} else if (newLevel > previousAmount && HAS_INCREASE_METHOD) {
				int increase = Math2.fit(minLevel, newLevel - previousAmount, maxLevel);
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
		return "villager " + (experience ? "experience" : "level");
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
