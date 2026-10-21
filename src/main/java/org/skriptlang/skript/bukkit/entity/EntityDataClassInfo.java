package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import static org.skriptlang.skript.bukkit.entity.EntityData.ALL_ENTITY_DATAS;

@SuppressWarnings("rawtypes")
public class EntityDataClassInfo extends ClassInfo<EntityData> {

	EntityDataClassInfo() {
		super(EntityData.class, "entitydata");
		this.user("entity ?types?")
			.name("Entity Type")
			.description("The type of an <a href='#entity'>entity</a>, e.g. player, wolf, powered creeper, etc.")
			.usage("")
			.examples("victim is a cow",
					"spawn a creeper")
			.since("1.3")
			.before("entitytype")
			.supplier(ALL_ENTITY_DATAS::iterator)
			.parser(new Parser<>() {
				@Override
				public String toString(EntityData entityData, int flags) {
					return entityData.toString(flags);
				}

				@Override
				public @Nullable EntityData parse(String string, ParseContext context) {
					return EntityData.parse(string);
				}

				@Override
				public String toVariableNameString(EntityData entityData) {
					return "entitydata:" + entityData.toString();
				}
			}).serializer(new EntityDataSerializer());
	}

	public static class EntityDataSerializer extends Serializer<EntityData> {
		//<editor-fold desc="serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(EntityData entityData) throws NotSerializableException {
			Fields fields = entityData.serialize();
			fields.putObject("codeName", entityData.info.dataName());
			return fields;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}

		@Override
		public void deserialize(EntityData entityData, Fields fields) {
			assert false;
		}

		@Override
		protected EntityData deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			String codeName = fields.getAndRemoveObject("codeName", String.class);
			if (codeName == null)
				throw new StreamCorruptedException();
			if (codeName.equals("simple")) {
				String simpleCodeName;
				if (fields.hasField("info.name")) {
					simpleCodeName = fields.getObject("info.name", String.class);
				} else {
					simpleCodeName = fields.getObject("info.codeName", String.class);
				}
				if (simpleCodeName == null) {
					throw new StreamCorruptedException();
				}
				// TODO something more generic for handling simple entity datas that become non-simple
				boolean changed = false;
				if (simpleCodeName.equals("shulker")) {
					codeName = "shulker";
					changed = true;
				}
				if (changed) {
					// remove redundant fields
					fields.removeField("info.name");
					fields.removeField("info.codeName"); // before 2.17
				}
			}
			EntityDataInfo<?, ?> info = EntityData.getInfo(codeName);
			if (info == null)
				throw new StreamCorruptedException("Invalid EntityData code name " + codeName);
			EntityData<?> entityData = info.instance();
			entityData.deserialize(fields);
			return entityData;
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}
		//</editor-fold>
	}

}
