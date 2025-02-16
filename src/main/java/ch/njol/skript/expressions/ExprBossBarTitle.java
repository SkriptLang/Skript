package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Boss Bar Title")
@Description("The title/text of a boss bar. This is the text shown above the progress bar of the boss bar.")
@Examples("set the title of {bar} to \"Goodbye!\"")
@Since("INSERT VERSION")
public class ExprBossBarTitle extends SimplePropertyExpression<BossBar, String> {

	static {
		register(ExprBossBarTitle.class, String.class, "title", "bossbars");
	}

	@Override
	public @Nullable String convert(BossBar bossBar) {
		return bossBar.getTitle();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> new Class[] {String.class};
			case RESET, DELETE -> new Class[0];
			default -> null;
		};
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		String title = null;
		switch (mode) {
			case SET:
				assert delta.length > 0 && delta[0] != null;
				title = (String) delta[0];
			case RESET, DELETE:
				for (BossBar bossBar : this.getExpr().getArray(event))
					bossBar.setTitle(title);
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "title";
	}

}
