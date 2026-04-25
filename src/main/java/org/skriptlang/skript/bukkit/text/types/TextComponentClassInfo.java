package org.skriptlang.skript.bukkit.text.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.ApiStatus;

import java.io.StreamCorruptedException;

@ApiStatus.Internal
public final class TextComponentClassInfo extends ClassInfo<Component> {

	public TextComponentClassInfo() {
		super(Component.class, "textcomponent");
		this.user("text ?components?")
			.name("Text Component")
			.description("Text components are used to represent how text is displayed in Minecraft.",
				"This includes colors, decorations, and more.")
			.examples("\"<red><bold>This text is red and bold!\"")
			.since("2.15")
			.parser(new TextComponentParser())
			.serializer(new TextComponentSerializer());
	}

	private static final class TextComponentParser extends Parser<Component> {

		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Component component, int flags) {
			return org.skriptlang.skript.bukkit.text.TextComponentParser.instance().toString(component);
		}

		@Override
		public String toVariableNameString(Component component) {
			return "textcomponent:" + component.hashCode();
		}

	}

	static final class TextComponentSerializer extends Serializer<Component> {

		@Override
		public Fields serialize(Component component) {
			return Fields.singletonObject("json", JSONComponentSerializer.json().serialize(component));
		}

		@Override
		protected Component deserialize(Fields fields) throws StreamCorruptedException {
			String json = fields.getObject("json", String.class);
			if (json == null) {
				throw new StreamCorruptedException();
			}
			return JSONComponentSerializer.json().deserialize(json);
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

		@Override
		protected boolean canBeInstantiated() {
			return false;
		}

	}

}
