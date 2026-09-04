package ch.njol.skript.expressions;

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

@Name("Raw Name")
@Description("""
	The raw Minecraft material name of the given item or entity type.
	This expression may return multiple names for item types, but always returns a single name per entity data.
	Note that this is not guaranteed to give same results on all servers.
	""")
@Example("raw name of tool of player")
@Example("raw name of (type of event-entity)")
@Since("unknown (2.2), entity types (INSERT VERSION)")
public class ExprRawName extends PropertyExpression<Object, String> {
	
	static {
		register(ExprRawName.class, String.class, "(raw|minecraft|vanilla) name[s]", "itemtypes/entitydatas");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(expressions[0]);
		return true;
	}

	@Override
	protected String[] get(Event event, Object[] source) {
		List<String> result = new ArrayList<>();
		for (Object object : source) {
			if (object instanceof ItemType itemType) {
				result.addAll(itemType.getRawNames());
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
		return "raw name of " + getExpr().toString(event, debug);
	}

}
