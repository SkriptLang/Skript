package org.skriptlang.skript.bukkit.entity.data;

import org.bukkit.DyeColor;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

public class CushionData extends ColorableEntityData<Cushion> {

	public static final EntityDataPatterns<?> GROUP =
		EntityDataPatterns.single("cushion:s @a", "[%-color%] cushion[plural:s]");

	public static void register() {
		registerInfo(
			infoBuilder(CushionData.class, "cushion")
				.dataPatterns(GROUP)
				.entityType(EntityType.CUSHION)
				.entityClass(Cushion.class)
				.supplier(CushionData::new)
				.build()
		);
	}

	public CushionData() {
		super();;
	}

	public CushionData(@Nullable DyeColor color) {
		super(color);
	}

	@Override
	public Class<? extends Cushion> getType() {
		return Cushion.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CushionData();
	}

}
