package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Boss Bar Progress")
@Description("The progress of a boss bar (ranges from 0 to 100)")
@Examples({"set progress of player's bossbar to 56"})
@Since("INSERT VERSION")
public class ExprBossBarProgress extends SimplePropertyExpression<BossBar, Number> {

	static {
		register(ExprBossBarProgress.class, Number.class, "progress", "bossbars");
	}

	@Override
	@Nullable
	public Number convert(final BossBar bossBar) {
		return bossBar.getProgress() * 100;
	}

	@Override
	protected String getPropertyName() {
		return "progress";
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Number.class};
		return null;
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		for (final BossBar bossBar : getExpr().getArray(event)) {
			switch (mode) {
				case RESET:
					bossBar.setProgress(0);
					break;
				case SET:
					assert delta[0] != null;
					bossBar.setProgress(((Number) delta[0]).doubleValue() / 100);
			}
		}
	}
}
