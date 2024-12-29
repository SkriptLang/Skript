package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Raw Name")
@Description("The raw Minecraft material name of the given item. Note that this is not guaranteed to give same results on all servers.")
@Examples("raw name of tool of player")
@Since("unknown (2.2)")
public class ExprRawName extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprRawName.class, String.class, ExpressionType.SIMPLE, "(raw|minecraft|vanilla) name[s] of %itemtypes%");
	}
	
	@SuppressWarnings("null")
	private Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.types = (Expression<ItemType>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		return Arrays.stream(types.getAll(e))
				.map(ItemType::getRawNames)
				.flatMap(List::stream)
				.toArray(String[]::new);
	}
	
	@Override
	public boolean isSingle() {
		return types.isSingle();
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@SuppressWarnings("null")
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "minecraft name of " + types.toString(e, debug);
	}
	
}
