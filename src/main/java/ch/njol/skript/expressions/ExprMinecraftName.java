package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Minecraft Name")
@Description("""
	The raw Minecraft material name of the given item or entity type.
	Note that this is not guaranteed to give same results on all servers.
	""")
@Example("minecraft name of tool of player")
@Example("vanilla name of type of event-entity")
@Since("unknown (2.2), entity types (INSERT VERSION)")
public class ExprMinecraftName extends PropertyExpression<Object, String> {
	
	static {
		register(ExprMinecraftName.class, String.class, "(:raw|minecraft|vanilla) name[s]", "itemtypes/entitydatas");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(expressions[0]);

		if (parseResult.hasTag("raw")) {
			Skript.warning("The 'raw name' expression was deprecated and will be removed in the future. Use 'mineraft name' instead");
		}

		return true;
	}

	@Override
	protected String[] get(Event event, Object[] source) {
		List<String> result = new ArrayList<>();
		for (Object object : source) {
			if (object instanceof ItemType itemType) {
				result.add(itemType.getRawNames().getFirst());
			} else if (object instanceof EntityData<?> entityData) {
				result.add(EntityUtils.toBukkitEntityType(entityData).getKey().asString());
			}
		}
		return result.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vanilla name of " + getExpr().toString(event, debug);
	}

}
