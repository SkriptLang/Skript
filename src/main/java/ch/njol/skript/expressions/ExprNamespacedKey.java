package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Namespaced Key")
@Description({"The namespaced key of a Minecraft object. This takes the form of \"namespace:key\", e.g. \"minecraft:dirt\".",
	"See <a href='https://docs.skriptlang.org/classes.html#keyed'>Keyed</a> for all types that support keys."})
@Examples({
	"set {_key} to namespaced key of player's tool",
	"if namespaced key of biome at player = \"minecraft:plains\":",
	"broadcast namespaced keys of the tags of player's tool",
	"if the key of {_my-tag} is \"minecraft:stone\":",
		"\treturn true"
})
@Since("2.10")
@Keywords({"minecraft tag", "type", "key", "namespace"})
public class ExprNamespacedKey extends SimplePropertyExpression<Keyed, String> {

	static {
		register(ExprNamespacedKey.class, String.class, "[namespace[d]] key[s]", "keyeds");
	}

	@Override
	public @Nullable String convert(@NotNull Keyed from) {
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
