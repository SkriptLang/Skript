package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Axolotl.Variant;
import org.jetbrains.annotations.Nullable;

public class AxolotlData extends EntityData<Axolotl> {

	static {
		if (Skript.classExists("org.bukkit.entity.Axolotl")) {
			EntityData.register(AxolotlData.class, "axolotl", Axolotl.class, 0,
				"axolotl", "lucy axolotl", "wild axolotl", "gold axolotl", "cyan axolotl", "blue axolotl");
		}
	}

	@Nullable
	private Variant variant = null;

	public AxolotlData() {}

	public AxolotlData(@Nullable Variant variant) {
		this.variant = variant;
		matchedPattern = variant != null ? variant.ordinal() + 1 : 0;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			variant = Variant.values()[matchedPattern - 1];
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Axolotl> c, @Nullable Axolotl axolotl) {
		if (axolotl != null)
			variant = axolotl.getVariant();
		return true;
	}

	@Override
	public void set(Axolotl entity) {
		if (variant != null)
			entity.setVariant(variant);
	}

	@Override
	protected boolean match(Axolotl entity) {
		return variant == null || variant == entity.getVariant();
	}

	@Override
	public Class<? extends Axolotl> getType() {
		return Axolotl.class;
	}

	@Override
	public EntityData getSuperType() {
		return new AxolotlData(variant);
	}

	@Override
	protected int hashCode_i() {
		return variant != null ? variant.hashCode() : 0;
	}

	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof AxolotlData))
			return false;
		return variant == ((AxolotlData) data).variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof AxolotlData))
			return false;
		return variant == null || variant == ((AxolotlData) data).variant;
	}

}
