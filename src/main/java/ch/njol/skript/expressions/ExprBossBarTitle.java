package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Boss Bar Title")
@Description("The title/text of a boss bar. This is the text shown above the progress bar of the bossbar.")
@Examples({"set title of player's bossbar to \"Goodbye!\""})
@Since("INSERT VERSION")
public class ExprBossBarTitle extends SimplePropertyExpression<BossBar, String> {

	static {
		register(ExprBossBarTitle.class, String.class, "title", "bossbars");
	}

	@Override
	@Nullable
	public String convert(final BossBar bossBar) {
		return bossBar.getTitle();
	}

	@Override
	protected String getPropertyName() {
		return "title";
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET)
			return new Class[] {String.class};
		return null;
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final Changer.ChangeMode mode) {
		for (final BossBar bossBar : getExpr().getArray(event)) {
			assert delta[0] != null;
			bossBar.setTitle((String) delta[0]);
		}
	}

}
