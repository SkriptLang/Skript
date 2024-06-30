package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.util.common.AnyPrefixed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Prefix")
@Description({"The prefix of a team, a member of a team, a player, or anything that can have a prefix.",
	"Please note that getting or changing a player's permission-plugin prefix requires Vault "
		+ "and a compatible permissions plugin.",
	"Getting or changing a team prefix is supported by default."
})
@Examples({
	"on chat:",
	"\tcancel event",
	"\tbroadcast \"%player's prefix%%player's display name%: %message%\" to the player's world",
	"",
	"set the player's prefix to \"[&lt;red&gt;Admin&lt;reset&gt;] \"",
	"",
	"set {team}'s prefix to \"&lt;blue&gt;Blue Team&lt;reset&gt; \""
})
@Since("2.0")
@RequiredPlugins({"Vault & Any permissions plugin (player prefixes)"})
public class ExprPrefix extends SimplePropertyExpression<AnyPrefixed, String> {

	static {
		register(ExprPrefix.class, String.class, "prefix", "any-prefixed");
	}

	@Override
	public String convert(AnyPrefixed source) {
		return source.prefix();
	}

	@Override
	protected String getPropertyName() {
		return "prefix";
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
		for (final AnyPrefixed prefixed : getExpr().getArray(event)) {
			if (prefixed.prefixSupportsChange())
				prefixed.setPrefix(value);
		}
	}

}
