package ch.njol.skript.expressions;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.GameruleValue;
import ch.njol.util.Kleenean;

@Name("Gamerule Value")
@Description("The gamerule value of a world.")
@Example("set the gamerule commandBlockOutput of world \"world\" to false")
@Since("2.5")
public class ExprGameRule extends SimpleExpression<GameruleValue> {
	
	static {
		Skript.registerExpression(ExprGameRule.class, GameruleValue.class, ExpressionType.COMBINED, "[the] gamerule %gamerule% of %worlds%");
	}

	private Expression<GameRule<?>> gamerule;
	private Expression<World> worlds;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// noinspection unchecked
		gamerule = (Expression<GameRule<?>>) exprs[0];
		// noinspection unchecked
		worlds = (Expression<World>) exprs[1];
		return true;
	}

	@Override
	protected GameruleValue<?> @Nullable [] get(Event event) {
		GameRule<?> gamerule = this.gamerule.getSingle(event);
		if (gamerule == null) {
			return null;
		}

		World[] worlds = this.worlds.getArray(event);
		GameruleValue<?>[] gameruleValues = new GameruleValue[worlds.length];

		for (int i = 0; i < worlds.length; i++) {
			Object gameruleValue = worlds[i].getGameRuleValue(gamerule);
			assert gameruleValue != null;
			gameruleValues[i] = new GameruleValue<>(gameruleValue);
		}

		return gameruleValues;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[]{Boolean.class, Integer.class};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode != ChangeMode.SET) {
			return;
		}

		GameRule gamerule = this.gamerule.getSingle(event);
		if (gamerule == null) {
			return;
		}

		assert delta != null;
		Object value = delta[0];
		if (!gamerule.getType().isAssignableFrom(value.getClass())) {
			String currentClassName = Classes.toString(Classes.getSuperClassInfo(value.getClass()));
			currentClassName = Utils.a(currentClassName);

			String targetClassName = Classes.toString(Classes.getSuperClassInfo(gamerule.getType()));
			targetClassName = Utils.a(targetClassName);

			error("The " + gamerule.getName() + " gamerule can only be set to " + targetClassName + ", not " + currentClassName + ".");
			return;
		}

		for (World gameruleWorld : worlds.getArray(event)) {
			gameruleWorld.setGameRule(gamerule, value);
		}
	}

	@Override
	public boolean isSingle() {
		return worlds.isSingle();
	}
	
	@Override
	public Class<? extends GameruleValue> getReturnType() {
		return GameruleValue.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the gamerule " + gamerule.toString(event, debug) + " of " + worlds.toString(event, debug);
	}

}
