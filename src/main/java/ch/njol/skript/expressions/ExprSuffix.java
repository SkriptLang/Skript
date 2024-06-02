package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnySuffixed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Suffix")
@Description({"The suffix of a team, a member of a team, a player, or anything that can have a suffix.",
	"Please note that getting or changing a player's permission-plugin suffix requires Vault "
		+ "and a compatible permissions plugin.",
	"Getting or changing a team suffix is supported by default."
})
@Examples({
	"on chat:",
	"\tcancel event",
	"\tbroadcast \"%player's prefix%%player's display name%%player's suffix%: %message%\" to the player's world",
	"",
	"set the player's suffix to \" (&lt;red&gt;Alive&lt;reset&gt;)\"",
	"",
	"set {team}'s suffix to \" &lt;blue&gt;OK&lt;reset&gt;\""
})
@Since("2.0")
@RequiredPlugins({"Vault & Any permissions plugin (player suffixes)"})
public class ExprSuffix extends SimplePropertyExpression<AnySuffixed, String> {

	static {
		register(ExprPrefix.class, String.class, "suffix", "any-suffixed");
	}

	@Override
	public String convert(AnySuffixed source) {
		return source.suffix();
	}

	@Override
	protected String getPropertyName() {
		return "suffix";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override

	public Class<?> @Nullable [] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {String.class};
		if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE)
			return new Class[0];
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String value = delta != null && delta.length > 0 && delta[0] != null ? delta[0].toString() : null;
		for (final AnySuffixed suffixed : getExpr().getArray(event)) {
			if (suffixed.suffixSupportsChange())
				suffixed.setSuffix(value);
		}
	}

}
