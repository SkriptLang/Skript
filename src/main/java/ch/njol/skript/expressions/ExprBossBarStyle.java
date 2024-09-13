package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Boss Bar Style")
@Description("The style of a boss bar. This changes how the boss bar displays on the player's screen.")
@Examples("set style of {_bar} to 10 segments")
@Since("INSERT VERSION")
public class ExprBossBarStyle extends SimplePropertyExpression<BossBar, BarStyle> {

	static {
		register(ExprBossBarStyle.class, BarStyle.class, "style", "bossbars");
	}

	@Override
	public @Nullable BarStyle convert(BossBar bossBar) {
		return bossBar.getStyle();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> new Class[] {BarStyle.class};
			case RESET -> new Class[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		BarStyle style = BarStyle.SOLID;
		switch (mode) {
			case SET:
				assert delta.length > 0 && delta[0] != null;
				style = (BarStyle) delta[0];
			case RESET:
				for (BossBar bossBar : this.getExpr().getArray(event))
					bossBar.setStyle(style);
		}
	}

	@Override
	public Class<BarStyle> getReturnType() {
		return BarStyle.class;
	}

	@Override
	protected String getPropertyName() {
		return "style";
	}

}
