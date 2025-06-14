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
import ch.njol.skript.doc.Examples;
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
@Examples({"set the gamerule commandBlockOutput of world \"world\" to false"})
@Since("2.5")
public class ExprGameRule extends SimpleExpression<GameruleValue> {
	
	static {
		Skript.registerExpression(ExprGameRule.class, GameruleValue.class, ExpressionType.COMBINED, "[the] gamerule %gamerule% of %worlds%");
	}

	private Expression<GameRule> gamerule;
	private Expression<World> worlds;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		gamerule = (Expression<GameRule>) exprs[0];
		worlds = (Expression<World>) exprs[1];
		return true;
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[]{Boolean.class, Integer.class};
		}
		return null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			GameRule gamerule = this.gamerule.getSingle(event);
			if (gamerule == null) {
				return;
			}

			Object value = delta[0];
			if (value instanceof Number number && gamerule.getType().equals(Integer.class)) {
				value = number.intValue();
			} else if (!value.getClass().equals(gamerule.getType())) {
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
	}

	@Override
	protected GameruleValue @Nullable [] get(Event event) {
		GameRule<?> gamerule = this.gamerule.getSingle(event);
		if (gamerule == null) {
			return null;
		}

		World[] worlds = this.worlds.getArray(event);
		GameruleValue[] gameruleValues = new GameruleValue[worlds.length];
		int index = 0;

		for (World world : worlds) {
			Object gameruleValue = world.getGameRuleValue(gamerule);
			assert gameruleValue != null;
			gameruleValues[index++] = new GameruleValue<>(gameruleValue);
		}

		return gameruleValues;
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
