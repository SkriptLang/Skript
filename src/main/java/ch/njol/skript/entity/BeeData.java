package ch.njol.skript.entity;

import java.util.Random;

import org.bukkit.entity.Bee;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class BeeData extends EntityData<Bee> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Bee")) {
			EntityData.register(BeeData.class, "bee", Bee.class, 2,
				"no nectar bee", "happy bee", "bee", "nectar bee", "angry bee", "angry nectar bee");
		}
	}
	
	private int nectar = 0;
	private int angry = 0;
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 3)
			angry = 1;
		else if (matchedPattern < 2)
			angry = -1;
		if (matchedPattern == 3 || matchedPattern == 5)
			nectar = 1;
		else if (matchedPattern < 2)
			nectar = -1;
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Bee> c, @Nullable Bee e) {
		angry = e == null ? 0 : (e.getAnger() > 0) ? 1 : -1;
		nectar = e == null ? 0 : e.hasNectar() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(Bee entity) {
		int random = new Random().nextInt(400) + 400;
		entity.setAnger(angry > 0 ? random : 0);
		entity.setHasNectar(nectar > 0);
	}
	
	@Override
	protected boolean match(Bee entity) {
		return (angry == 0 || (entity.getAnger() > 0) == (angry == 1)) && (nectar == 0 || entity.hasNectar() == (nectar == 1));
	}
	
	@Override
	public Class<? extends Bee> getType() {
		return Bee.class;
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
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof BeeData))
			return false;
		final BeeData other = (BeeData) obj;
		return (angry == other.angry) && (nectar == other.nectar);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		return (e instanceof BeeData) && (angry == 0 || ((BeeData) e).angry == angry) && (nectar == 0 || ((BeeData) e).nectar == nectar);
	}
	
	@Override
	public EntityData getSuperType() {
		return new BeeData();
	}
}
