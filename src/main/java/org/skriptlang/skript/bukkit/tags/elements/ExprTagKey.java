package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: adapt to generic expression after Any X is merged

@Name("Tag Namespaced Key")
@Description("The namespaced key of a minecraft tag. This takes the form of \"namespace:key\", e.g. \"minecraft:dirt\".")
@Examples({
	"broadcast namespaced keys of the tags of player's tool",
	"if the key of {_my-tag} is \"minecraft:stone\":",
		"\treturn true"
})
@Since("INSERT VERSION")
@Keywords({"minecraft tag", "type", "key", "namespace"})
public class ExprTagKey extends SimplePropertyExpression<Tag<?>, String> {

	static {
		register(ExprTagKey.class, String.class, "[namespace[d]] key[s]", "minecrafttags");
	}

	@Override
	public @Nullable String convert(@NotNull Tag<?> from) {
		return from.getKey().toString();
	}

	@Override
	protected String getPropertyName() {
		return "namespaced key";
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

}
