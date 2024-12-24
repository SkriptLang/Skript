package ch.njol.skript.entity;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

import java.util.Objects;

public class WolfData extends EntityData<Wolf> {

	private static boolean variantsEnabled = false;

	static {
		EntityData.register(WolfData.class, "wolf", Wolf.class, 1,
				"peaceful wolf", "wolf", "angry wolf",
				"wild wolf", "tamed wolf");
		if (Skript.classExists("org.bukkit.entity.Wolf$Variant") && BukkitUtils.registryExists("WOLF_VARIANT")) {
			variantsEnabled = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Wolf.Variant.class).getSupplier().get(), Wolf.Variant.class);
		}
	}


	private static Object[] variants;

	private @Nullable Object variant;
	private @Nullable DyeColor collarColor;

	private int angry = 0;
	private int tamed = 0;

	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern <= 2)
			angry = matchedPattern - 1;
		else
			tamed = matchedPattern == 3 ? -1 : 1;
		if (exprs[0] != null && variantsEnabled)
			variant = ((Literal<Wolf.Variant>) exprs[0]).getSingle();
		if (exprs[1] != null)
			collarColor = ((Literal<Color>) exprs[1]).getSingle().asDyeColor();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Wolf> c, @Nullable Wolf wolf) {
		if (wolf != null) {
			angry = wolf.isAngry() ? 1 : -1;
			tamed = wolf.isTamed() ? 1 : -1;
			collarColor = wolf.getCollarColor();
			if (variantsEnabled)
				variant = wolf.getVariant();
		}
		return true;
	}

	@Override
	public void set(Wolf entity) {
		if (angry != 0)
			entity.setAngry(angry == 1);
		if (tamed != 0)
			entity.setTamed(tamed == 1);
		if (collarColor != null)
			entity.setCollarColor(collarColor);
		Object variantSet = null;
		if (variantsEnabled) {
			variantSet = variant != null ? variant : CollectionUtils.getRandom(variants);
			entity.setVariant((Wolf.Variant) variantSet);
		}
	}

	@Override
	public boolean match(Wolf entity) {
		return (angry == 0 || entity.isAngry() == (angry == 1)) && (tamed == 0 || entity.isTamed() == (tamed == 1)) &&
			(collarColor == null || entity.getCollarColor() == collarColor) && (variant == null || entity.getVariant() == variant);
	}

	@Override
	public Class<Wolf> getType() {
		return Wolf.class;
	}

	@Override
	protected int hashCode_i() {
		int prime = 31, result = 1;
		result = prime * result + angry;
		result = prime * result + tamed;
		result = prime * result + (collarColor == null ? 0 : collarColor.hashCode());
		if (variantsEnabled)
			result = prime * result + (variant == null ? 0 : Objects.hashCode(variant));
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof WolfData))
			return false;
		WolfData other = (WolfData) obj;
		if (angry != other.angry)
			return false;
		if (tamed != other.tamed)
			return false;
		if (collarColor != other.collarColor)
			return false;
		if (variantsEnabled && variant != other.variant)
			return false;
		return true;
	}

	/**
	 * Note that this method is only used when changing Skript versions 2.1 to anything above.
	 */
	@Deprecated
	@Override
	protected boolean deserialize(String s) {
		String[] split = s.split("\\|");
		if (split.length != 2)
			return false;
		try {
			angry = Integer.parseInt(split[0]);
			tamed = Integer.parseInt(split[1]);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof WolfData) {
			WolfData wolfData = (WolfData) entityData;
			return (angry == 0 || wolfData.angry == angry) && (tamed == 0 || wolfData.tamed == tamed) && (wolfData.collarColor == collarColor) && (!variantsEnabled || wolfData.variant == variant);
		}
		return false;
	}

	@Override
	public EntityData<Wolf> getSuperType() {
		return new WolfData();
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Wolf.Variant`
	 */
	public static class VariantDummy {};

}
