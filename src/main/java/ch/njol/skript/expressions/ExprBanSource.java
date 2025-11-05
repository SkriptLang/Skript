package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.BanEntry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Ban Source")
@Description("Returns the ban source of a player or IP. This is usually the name of the player that banned them, but " +
	"it might not always be a player name.")
@Example("send the ban source of \"3.3.3.3\"")
@Since("INSERT VERSION")
public class ExprBanSource extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprBanSource.class, String.class, ExpressionType.SIMPLE,
			"[the] source[s] of %offlineplayers/strings%'s ban",
			"[the] ban source of %offlineplayers/strings%",
			"[the] date %offlineplayers/strings%'s ban expires"
		);
	}

	private Expression<Object> targets;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// noinspection unchecked
		targets = (Expression<Object>) expressions[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		Object[] targets = this.targets.getAll(event);
		if (targets == null) {
			return null;
		}

		String[] sources = new String[targets.length];

		for (int i = 0; i < targets.length; i++) {
			Object target = targets[i];

			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			sources[i] = banEntry.getSource();
		}

		return sources;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return new Class<?>[]{ String.class };
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] targets = this.targets.getAll(event);

		assert delta != null;
		String source = (String) delta[0];

		for (Object target : targets) {
			BanEntry<?> banEntry = BukkitUtils.getBanEntry(target);
			if (banEntry == null) {
				continue;
			}

			banEntry.setSource(source);
			banEntry.save();
		}
	}

	@Override
	public boolean isSingle() {
		return targets.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the ban source of " +  targets.toString(event, debug);
	}

}
