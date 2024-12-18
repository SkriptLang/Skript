package ch.njol.skript.entity;

import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class MooshroomData extends EntityData<MushroomCow> {
	
	static {
		if (Skript.methodExists(MushroomCow.class, "getVariant")) {
			EntityData.register(MooshroomData.class, "mooshroom", MushroomCow.class, 1,
				"mooshroom", "red mooshroom", "brown mooshroom");
		}
	}
	
	@Nullable
	private Variant variant = null;
	
	public MooshroomData() {}
	
	public MooshroomData(@Nullable Variant variant) {
		this.variant = variant;
		super.matchedPattern = variant == Variant.BROWN ? 2 : 1;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			variant = Variant.values()[matchedPattern - 1];
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends MushroomCow> c, @Nullable MushroomCow mushroomCow) {
		if (mushroomCow != null)
			variant = mushroomCow.getVariant();
		return true;
	}
	
	@Override
	public void set(MushroomCow entity) {
		if (variant != null)
			entity.setVariant(variant);
	}
	
	@Override
	protected boolean match(MushroomCow entity) {
		return variant == null || variant == entity.getVariant();
	}
	
	@Override
	public Class<? extends MushroomCow> getType() {
		return MushroomCow.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new MooshroomData(variant);
	}
	
	@Override
	protected int hashCode_i() {
		return variant != null ? variant.hashCode() : 0;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof MooshroomData))
			return false;
		return variant == ((MooshroomData) data).variant;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof MooshroomData))
			return false;
		return variant == null || variant == ((MooshroomData) data).variant;
	}
}
