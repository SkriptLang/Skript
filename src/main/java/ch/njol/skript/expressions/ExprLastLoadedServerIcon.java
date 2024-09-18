package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.EffLoadServerIcon;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

@Name("Last Loaded Server Icon")
@Description("Returns the last loaded server icon with the <a href='effects.html#EffLoadServerIcon'>load server icon</a> effect.")
@Examples("set {server-icon} to the last loaded server icon")
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprLastLoadedServerIcon extends SimpleExpression<CachedServerIcon> {

	private static final boolean SUPPORTS_SERVER_LIST_PING_EVENT =
		Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	static {
		Skript.registerExpression(ExprLastLoadedServerIcon.class, CachedServerIcon.class,
			ExpressionType.SIMPLE, "[the] [last[ly]] loaded server icon");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!SUPPORTS_SERVER_LIST_PING_EVENT) {
			Skript.error("The last loaded server icon expression requires Paper 1.12.2+");
			return false;
		}

		return true;
	}

	@Override
	public CachedServerIcon @Nullable [] get(Event event) {
		return CollectionUtils.array(EffLoadServerIcon.lastLoaded);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends CachedServerIcon> getReturnType() {
		return CachedServerIcon.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the last loaded server icon";
	}

}
