package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.Nullable;

public class GoatData extends EntityData<Goat> {

	static {
		if (Skript.classExists("org.bukkit.entity.Goat"))
			EntityData.register(GoatData.class, "goat", Goat.class, 0,
				"goat", "screaming goat", "quiet goat");
	}

	private int screaming = 0; // 0 = random, 1 = screaming, 2 = quiet

	public GoatData() {}

	public GoatData(int screaming) {
		this.screaming = screaming;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		screaming = matchedPattern;
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Goat> c, @Nullable Goat goat) {
		if (goat != null && matchedPattern > 0)
			goat.setScreaming(matchedPattern == 1);
		return true;
	}

	@Override
	public void set(Goat entity) {
		if (matchedPattern > 0)
			entity.setScreaming(screaming == 1);
	}

	@Override
	protected boolean match(Goat entity) {
		if (matchedPattern > 0)
			return entity.isScreaming() ? screaming == 1 : screaming == 2;
		return true;
	}

	@Override
	public Class<? extends Goat> getType() {
		return Goat.class;
	}

	@Override
	public EntityData getSuperType() {
		return new GoatData(screaming);
	}

	@Override
	protected int hashCode_i() {
		return screaming;
	}

	@Override
	protected boolean equals_i(EntityData<?> data) {
		if (!(data instanceof GoatData))
			return false;
		return screaming == ((GoatData) data).screaming;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> data) {
		if (!(data instanceof GoatData))
			return false;
		return screaming == ((GoatData) data).screaming;
	}

}
