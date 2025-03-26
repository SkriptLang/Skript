package ch.njol.skript.expressions;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.GameruleValue;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Gamerule Value")
@Description("The gamerule value of a world.")
@Examples({"set the gamerule commandBlockOutput of world \"world\" to false"})
@Since("2.5")
@RequiredPlugins("Minecraft 1.13+")
public class ExprGameRule extends SimpleExpression<GameruleValue> {
	
	static {
		if (Skript.classExists("org.bukkit.GameRule")) {
			Skript.registerExpression(ExprGameRule.class, GameruleValue.class, ExpressionType.COMBINED,
				"[the] gamerule %gamerule% of %worlds%");
		}
	}
	
	@SuppressWarnings("null")
	private Expression<GameRule> gamerule;
	@SuppressWarnings("null")
	private Expression<World> world;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		gamerule = (Expression<GameRule>) exprs[0];
		world = (Expression<World>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET) 
            return CollectionUtils.array(Boolean.class, Number.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert delta != null;
		if (mode == ChangeMode.SET) {
			GameRule bukkitGamerule = gamerule.getSingle(e);
			if (bukkitGamerule == null) 
                return;
			for (World gameruleWorld : world.getArray(e))
                gameruleWorld.setGameRule(bukkitGamerule, delta[0]);
		}
	}
		
	@Nullable
	@Override
	protected GameruleValue[] get(Event e) {
		GameRule<?> bukkitGamerule = gamerule.getSingle(e);
		if (bukkitGamerule == null) 
            return null;
		World[] gameruleWorlds = world.getArray(e);
		GameruleValue[] gameruleValues = new GameruleValue[gameruleWorlds.length];
		int index = 0;
		for (World gameruleWorld : gameruleWorlds) {
			Object gameruleValue = gameruleWorld.getGameRuleValue(bukkitGamerule);
			assert gameruleValue != null;
			gameruleValues[index++] = new GameruleValue<>(gameruleValue);
		}
		return gameruleValues;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends GameruleValue> getReturnType() {
		return GameruleValue.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the gamerule value of " + gamerule.toString(e, debug) + " for world " + world.toString(e, debug);
	}
}
