package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffLoadServerIcon;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Last Loaded Server Icon")
@Description({"Returns the last loaded server icon with the <a href='effects.html#EffLoadServerIcon'>load server icon</a> effect."})
@Examples("set {server-icon} to the last loaded server icon")
@Since("2.3")
@RequiredPlugins("Paper 1.12.2 or newer")
public class ExprLastLoadedServerIcon extends SimpleExpression<CachedServerIcon> {

	static {
		Skript.registerExpression(ExprLastLoadedServerIcon.class, CachedServerIcon.class, ExpressionType.SIMPLE, "[the] [last[ly]] loaded server icon");
	}

	private static final boolean PAPER_EVENT_EXISTS = Skript.classExists("com.destroystokyo.paper.event.server.PaperServerListPingEvent");

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!PAPER_EVENT_EXISTS) {
			Skript.error("The last loaded server icon expression requires Paper 1.12.2+");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	public CachedServerIcon[] get(Event e) {
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
	public String toString(@Nullable Event e, boolean debug) {
		return "the last loaded server icon";
	}

}
