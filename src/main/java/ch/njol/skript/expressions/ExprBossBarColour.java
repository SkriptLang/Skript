package ch.njol.skript.expressions;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

public class ExprBossBarColour extends SimpleExpression<BarColor> {
	
	static {
		Skript.registerExpression(ExprBossBarColour.class, BarColor.class, ExpressionType.COMBINED, "colo[u]r of [boss[ ]]bar %bossbar%", "[boss[ ]]bar %bossbar%'s colo[u]r");
	}
	
	@SuppressWarnings("null")
	Expression<BossBar> bar;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		bar = (Expression<BossBar>) exprs[0];
		return true;
	}
	
	@Nullable
	@Override
	protected BarColor[] get(Event e) {
		BossBar bossBar = bar.getSingle(e);
		if (bossBar == null)
			return null;
		return new BarColor[]{bossBar.getColor()};
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
				return new Class[]{BarColor.class};
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		BarColor color = delta != null ? (BarColor) delta[0] : BarColor.WHITE;
		for (BossBar bossBar : bar.getArray(e)) {
			bossBar.setColor(color);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends BarColor> getReturnType() {
		return BarColor.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "colour of bossbar " + bar.toString(e, debug);
	}
	
}
