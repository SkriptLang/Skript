package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.aliases.ItemType;
import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;
import org.skriptlang.skript.bukkit.entity.EntityItemTypeComparable;

import java.util.HashMap;
import java.util.Map;

public class CushionData extends ColorableEntityData<Cushion> implements EntityItemTypeComparable {

	public static final EntityDataPatterns<?> GROUP =
		EntityDataPatterns.single("cushion:s @a", "[%-color%] cushion[plural:s]");

	public static final Map<DyeColor, Material> CUSHIONS = new HashMap<>();

	public static void register() {
		registerInfo(
			infoBuilder(CushionData.class, "cushion")
				.dataPatterns(GROUP)
				.entityType(EntityType.CUSHION)
				.entityClass(Cushion.class)
				.supplier(CushionData::new)
				.build()
		);

		for (DyeColor dyeColor : DyeColor.values()) {
			String name = dyeColor.name() + "_CUSHION";
			Material material = Material.getMaterial(name);
			if (material == null)
				continue;
			CUSHIONS.put(dyeColor, material);
		}
	}

	public CushionData() {
		super();
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

	@Override
	public boolean isOfItemType(ItemType itemType) {
		if (color == null)
			return MaterialSetTag.ITEMS_CUSHIONS.isTagged(itemType.getMaterial());
		Material material = CUSHIONS.get(color);
		if (material == null)
			return false;
		return itemType.isOfType(material);
	}

}
