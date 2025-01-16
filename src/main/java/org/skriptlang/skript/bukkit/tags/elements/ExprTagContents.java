package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Stream;

@Name("Tags Contents")
@Description({
		"Returns all the values that a tag contains.",
		"For item and block tags, this will return items. For entity tags, " +
		"it will return entity datas (a creeper, a zombie)."
})
@Examples({
		"broadcast tag values of minecraft tag \"dirt\"",
		"broadcast (first element of player's tool's block tags)'s tag contents"
})
@Since("2.10")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class ExprTagContents extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprTagContents.class, Object.class, ExpressionType.PROPERTY,
				"[the] tag (contents|values) of %minecrafttag%",
				"%minecrafttag%'[s] tag (contents|values)");
	}

	private Expression<Tag<?>> tag;

	private Class<?> returnType;
	private Class<?>[] possibleReturnTypes;

	@Override
	public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		tag = (Expression<Tag<?>>) expressions[0];

		TagType<?>[] tagTypes = null;
		if (expressions[0] instanceof ExprTag exprTag) {
			tagTypes = exprTag.types;
		} else if (expressions[0] instanceof ExprTagsOf exprTagsOf) {
			tagTypes = exprTagsOf.types;
		} else if (expressions[0] instanceof ExprTagsOfType exprTagsOfType) {
			tagTypes = exprTagsOfType.types;
		}
		if (tagTypes != null) { // try to determine the return type
			possibleReturnTypes = new Class<?>[tagTypes.length];
			for (int i = 0; i < tagTypes.length; i++) {
				Class<?> type = tagTypes[i].type();
				// map types
				if (type == Material.class) {
					type = ItemType.class;
				} else if (type == EntityType.class) {
					type = EntityData.class;
				}
				possibleReturnTypes[i] = type;
			}
			returnType = Classes.getSuperClassInfo(possibleReturnTypes).getC();
		} else {
			returnType = Object.class;
			possibleReturnTypes = new Class<?>[]{returnType};
		}

		return true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes", "RedundantCast"}) // cast to avoid type issues
	protected Object @Nullable [] get(Event event) {
		return ((Stream) stream(event)).toArray(length -> Array.newInstance(getReturnType(), length));
	}

	@Override
	public Stream<?> stream(Event event) {
		Tag<?> tag = this.tag.getSingle(event);
		if (tag == null)
			return Stream.empty();
		return tag.getValues().stream()
			.map(value -> {
				if (value instanceof Material material) {
					return new ItemType(material);
				} else if (value instanceof EntityType entityType) {
					return EntityUtils.toSkriptEntityData(entityType);
				}
				return null;
			})
			.filter(Objects::nonNull);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return possibleReturnTypes;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tag contents of " + tag.toString(event, debug);
	}

	@Override
	public Expression<?> simplify() {
		if (tag instanceof Literal<Tag<?>>) {
			Object[] values = getArray(ContextlessEvent.get());
			return new SimpleLiteral(values, values.getClass().getComponentType(), true);
		}
		return super.simplify();
	}

}
