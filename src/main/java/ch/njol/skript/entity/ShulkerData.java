package ch.njol.skript.entity;

import org.bukkit.DyeColor;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShulkerData extends ColorableEntityData<Shulker> {

	static {
		EntityData.register(ShulkerData.class, "shulker", Shulker.class, "shulker");
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

}
