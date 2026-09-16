package org.skriptlang.skript.bukkit.world.elements.expressions;


import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.GameruleValue;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

import static ch.njol.skript.expressions.base.PropertyExpression.infoBuilder;

@Name("Gamerule Value")
@Description("The gamerule value of a world.")
@Example("set the gamerule commandBlockOutput of world \"world\" to false")
@Since("2.5")
@SuppressWarnings("rawtypes")
public class ExprGameRule extends SimpleExpression<GameruleValue> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprGameRule.class,
				GameruleValue.class,
				"gamerule %gamerule%",
				"worlds",
				false
			)
				.supplier(ExprGameRule::new)
				.build()
		);
	}

	private Expression<GameRule<?>> gamerule;
	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			gamerule = (Expression<GameRule<?>>) expressions[0];
			worlds = (Expression<World>) expressions[1];
		} else {
			gamerule = (Expression<GameRule<?>>) expressions[1];
			worlds = (Expression<World>) expressions[0];
		}

		return true;
	}

	@Override
	protected GameruleValue<?> @Nullable [] get(Event event) {
		GameRule<?> gamerule = this.gamerule.getSingle(event);
		if (gamerule == null)
			return new GameruleValue[0];

		World[] worlds = this.worlds.getArray(event);
		if (worlds == null)
			return new GameruleValue[0];

		return Arrays.stream(worlds)
			.map(world -> new GameruleValue<>(world.getGameRuleValue(gamerule)))
			.toArray(GameruleValue[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class[]{Boolean.class, Integer.class};
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (mode != ChangeMode.SET)
			return;

		GameRule<?> gamerule = this.gamerule.getSingle(event);
		if (gamerule == null)
			return;

		if (delta == null)
			return;
		Object value = delta[0];

		if (!gamerule.getType().isAssignableFrom(value.getClass())) {
			String currentClassName = Classes.toString(Classes.getSuperClassInfo(value.getClass()));
			currentClassName = Utils.a(currentClassName);

			String targetClassName = Classes.toString(Classes.getSuperClassInfo(gamerule.getType()));
			targetClassName = Utils.a(targetClassName);

			error("The " + gamerule.getKey() + " gamerule can only be set to " + targetClassName + ", not " + currentClassName + ".");
			return;
		}

		for (World world : worlds.getArray(event))
			world.setGameRule((GameRule<Object>) gamerule, value);
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
		return new SyntaxStringBuilder(event, debug)
			.append("the gamerule ", gamerule, " of ", worlds)
			.toString();
	}

}
