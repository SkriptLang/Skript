package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Keyed;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagModule;

@Name("Is Tagged")
@Description({
	"Checks whether an item, block, entity, or entitydata is tagged with the given tag."
})
@Examples({
	"if player's tool is tagged with minecraft tag \"enchantable/sharp_weapon\":",
		"\tenchant player's tool with sharpness 1",
	"",
	"if all logs are tagged with tag \"minecraft:logs\""
})
@Since("2.10")
@Keywords({"blocks", "minecraft tag", "type", "category"})
public class CondIsTagged extends Condition {

	static {
		PropertyCondition.register(CondIsTagged.class, PropertyCondition.PropertyType.BE,
				"tagged (as|with) %minecrafttags%",
				"itemtypes/entities/entitydatas");
	}

	private Expression<Tag<Keyed>> tags;
	private Expression<?> elements;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.elements = expressions[0];
		//noinspection unchecked
		this.tags = (Expression<Tag<Keyed>>) expressions[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Tag<Keyed>[] tags = this.tags.getArray(event);
		if (tags.length == 0)
			return isNegated();
		boolean and = this.tags.getAnd();
 		return elements.check(event, element -> {
			boolean isAny = (element instanceof ItemType itemType && !itemType.isAll());
			Keyed[] values = TagModule.getKeyed(element);
			if (values == null)
				return false;

			for (Tag<Keyed> tag : tags) {
				 if (isTagged(tag, values, !isAny)) {
					 if (!and)
						 return true;
				 } else if (and) {
					 return false;
				 }
			 }
			return and;
		}, isNegated());
	}

	/**
	 * Helper method for checking if a series of values have a tag.
	 * @param tag The tag to check for.
	 * @param values The values to check against.
	 * @param allTagged Whether all values need to have the tag (true), or just one (false).
	 * @return Whether the values are tagged with the tag.
	 */
	private boolean isTagged(Tag<Keyed> tag, Keyed @NotNull [] values, boolean allTagged) {
		for (Keyed value : values) {
			if (tag.isTagged(value)) {
				if (!allTagged)
					return true;
			} else if (allTagged) {
				return false;
			}
		}
		return allTagged;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, elements,
				" tagged as " + tags.toString(event, debug));
	}

}
