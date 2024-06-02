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
@RequiredPlugins({"Vault", "a chat plugin that supports Vault"})
public class ExprSuffix extends SimplePropertyExpression<AnySuffixed, String> {

	static {
		register(ExprPrefix.class, String.class, "[chat|team] suffix", "any-suffixed");
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

	public Class<?> @Nullable [] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {String.class};
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert mode == ChangeMode.SET;
		assert delta != null && delta.length > 0;
		for (final AnySuffixed suffixed : getExpr().getArray(event)) {
			if (suffixed.suffixSupportsChange())
				suffixed.setSuffix(String.valueOf(delta[0]));
		}
	}

}
