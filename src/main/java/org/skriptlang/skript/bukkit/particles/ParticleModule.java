package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Effect;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public class ParticleModule {

	public static void load () throws IOException {
		Classes.registerClass(new ClassInfo<>(GameEffect.class, "gameeffect")
			.user("game ?effects?")
			.since("INSERT VERSION")
			.description("Various game effects that can be played for players, like record disc songs, splash potions breaking, or fake bone meal effects.")
			.name("Game Effect")
			.usage(GameEffect.getAllNamesWithoutData())
			.serializer(new Serializer<>() {
				@Override
				public Fields serialize(GameEffect effect) {
					Fields fields = new Fields();
					fields.putPrimitive("name", effect.getEffect().name());
					fields.putObject("data", effect.getData());
					return fields;
				}

				@Override
				public void deserialize(GameEffect effect, Fields fields) throws StreamCorruptedException, NotSerializableException {
					assert false;
				}

				@Override
				protected GameEffect deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
					String name = fields.getAndRemovePrimitive("name", String.class);
					GameEffect effect;
					try {
						effect = new GameEffect(Effect.valueOf(name));
					} catch (IllegalArgumentException e) {
						return null;
					}
					effect.setData(fields.getObject("data"));
					return effect;
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			})
			.defaultExpression(new EventValueExpression<>(GameEffect.class))
			.parser(new Parser<>() {
				@Override
				public GameEffect parse(String input, ParseContext context) {
					return GameEffect.parse(input);
				}

				@Override
				public String toString(GameEffect effect, int flags) {
					return effect.toString(flags);
				}

				@Override
				public String toVariableNameString(GameEffect o) {
					return o.getEffect().name();
				}
			}));

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.particles", "elements");

	}

}
