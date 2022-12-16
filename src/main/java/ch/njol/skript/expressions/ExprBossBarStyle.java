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
import org.eclipse.jdt.annotation.Nullable;

@Name("Boss Bar Style")
@Description("The style of a boss bar. This changes how the boss bar displays on the player's screen.")
@Examples({"set style of player's bossbar to 10 segments"})
@Since("INSERT VERSION")
public class ExprBossBarStyle extends SimplePropertyExpression<BossBar, BarStyle> {

	static {
		register(ExprBossBarStyle.class, BarStyle.class, "style", "bossbars");
	}

	@Override
	@Nullable
	public BarStyle convert(final BossBar bossBar) {
		return bossBar.getStyle();
	}

	@Override
	protected String getPropertyName() {
		return "style";
	}

	@Override
	public Class<BarStyle> getReturnType() {
		return BarStyle.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {BarStyle.class};
		return null;
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		for (final BossBar bossBar : getExpr().getArray(event)) {
			assert delta[0] != null;
			bossBar.setStyle((BarStyle) delta[0]);
		}
	}

}
