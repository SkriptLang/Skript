package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


@Name("Boss Bar Flags")
@Description("The flags of a bossbar. These flags control the behavior of the bossbar.")
@Examples({"add darken sky to flags of player's bossbar"})
@Since("INSERT VERSION")
public class ExprBossBarFlags extends PropertyExpression<BossBar, BarFlag> {

	static {
		register(ExprBossBarFlags.class, BarFlag.class, "flags", "bossbars");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<? extends BossBar>) exprs[0]);
		return true;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
				return new Class[] {BarFlag.class};
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (BossBar bossBar : getExpr().getArray(event))
			switch (mode) {
				case SET:
					clearFlags(bossBar);
				case ADD:
					for (Object flag : delta)
						bossBar.addFlag((BarFlag) flag);
					break;
				case REMOVE:
					for (Object flag : delta)
						bossBar.removeFlag((BarFlag) flag);
					break;
				case DELETE:
					clearFlags(bossBar);
			}
	}

	@Override
	protected BarFlag[] get(Event event, BossBar[] source) {
		List<BarFlag> flags = new ArrayList<>();
		for (BossBar bossBar : source)
			for (BarFlag flag : BarFlag.values())
				if (bossBar.hasFlag(flag)) // bukkit has no getter for flags, so we have to check like this...
					flags.add(flag);
		return flags.toArray(new BarFlag[0]);
	}

	@Override
	public Class<? extends BarFlag> getReturnType() {
		return BarFlag.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "flags of " + getExpr().toString(e, debug);
	}

	private void clearFlags(BossBar bossBar) {
		for (BarFlag flag : BarFlag.values())
			if (bossBar.hasFlag(flag))
				bossBar.removeFlag(flag);
	}

}
