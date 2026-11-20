package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustSerializable;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.lang.reflect.Field;

public class ShulkerData extends ColorableEntityData<Shulker> implements YggdrasilRobustSerializable {

	public static final EntityDataPatterns<?> GROUP =
		EntityDataPatterns.single("shulker:s @a", "[%-color%] shulker[plural:s]");

	public static void register() {
		registerInfo(
			infoBuilder(ShulkerData.class, "shulker")
				.dataPatterns(GROUP)
				.entityType(EntityType.SHULKER)
				.entityClass(Shulker.class)
				.supplier(ShulkerData::new)
				.build()
		);
	}

	public ShulkerData() {
		super();
	}

	public ShulkerData(@Nullable DyeColor color) {
		super(color);
	}

	@Override
	public Class<? extends Shulker> getType() {
		return Shulker.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ShulkerData();
	}

	@Override
	public boolean incompatibleField(@NotNull Field field, @NotNull Fields.FieldContext value) {
		return false;
	}

	@Override
	public boolean excessiveField(@NotNull Fields.FieldContext field) {
		return false;
	}

	@Override
	public boolean missingField(@NotNull Field field) {
		// Before 2.17, shulkers were simple and did not have a color
		return field.getName().equals("color");
	}

}
