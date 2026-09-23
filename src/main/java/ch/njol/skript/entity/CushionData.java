package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import org.bukkit.DyeColor;
import org.bukkit.entity.Cushion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CushionData extends ColorableEntityData<Cushion> {

	static {
		if (Skript.classExists("org.bukkit.entity.Cushion")) { // Added in 26.3
			EntityData.register(CushionData.class, "cushion", Cushion.class, "cushion");
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

}
