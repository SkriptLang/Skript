package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Bee;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BeeData extends EntityData<Bee> {
	
	static {
		EntityData.register(BeeData.class, "bee", Bee.class, 2,
			"no nectar bee", "happy bee", "bee", "nectar bee", "angry bee", "angry nectar bee");
	}
	
	private int nectar = 0;
	private int angry = 0;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (matchedCodeName > 3) {
			angry = 1;
		} else if (matchedCodeName < 2) {
			angry = -1;
		}
		if (matchedCodeName == 3 || matchedCodeName == 5) {
			nectar = 1;
		} else if (matchedCodeName < 2) {
			nectar = -1;
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Bee> entityClass, @Nullable Bee bee) {
		angry = bee == null ? 0 : (bee.getAnger() > 0) ? 1 : -1;
		nectar = bee == null ? 0 : bee.hasNectar() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(Bee bee) {
		int random = new Random().nextInt(400) + 400;
		bee.setAnger(angry > 0 ? random : 0);
		bee.setHasNectar(nectar > 0);
	}
	
	@Override
	protected boolean match(Bee bee) {
		if (angry == 0 && nectar == 0)
			return true;
		if ((bee.getAnger() > 0) != (angry == 1))
			return false;
        return bee.hasNectar() == (nectar == 1);
    }
	
	@Override
	public Class<? extends Bee> getType() {
		return Bee.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new BeeData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + angry;
		result = prime * result + nectar;
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		return (angry == other.angry) && (nectar == other.nectar);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		if (angry != 0 && angry != other.angry)
			return false;
		return nectar == 0 || nectar == other.nectar;
	}

}
