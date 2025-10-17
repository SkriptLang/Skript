package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Language;
import ch.njol.util.Kleenean;
import io.papermc.paper.world.WeatheringCopperState;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.CopperGolem.Oxidizing.Waxed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CopperGolemData extends EntityData<CopperGolem> {

	static {
		if (Skript.classExists("org.bukkit.entity.CopperGolem"))
			register(CopperGolemData.class, "copper golem", CopperGolem.class, 0, "copper golem");
	}

	private Kleenean waxed = Kleenean.UNKNOWN;
	private @Nullable WeatheringCopperState state;

	public CopperGolemData() {}

	public CopperGolemData(@Nullable Kleenean waxed, @Nullable WeatheringCopperState state) {
		this.waxed = waxed == null ? Kleenean.UNKNOWN : waxed;
		this.state = state;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		Literal<WeatheringCopperState> expr = (Literal<WeatheringCopperState>) exprs[0];
		if (expr != null)
			state = expr.getSingle();
		if (matchedPattern == 1) {
			waxed = Kleenean.TRUE;
		} else if (matchedPattern == 2) {
			waxed = Kleenean.FALSE;
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends CopperGolem> entityClass, @Nullable CopperGolem golem) {
		if (golem != null) {
			state = golem.getWeatheringState();
			waxed = Kleenean.get(golem.getOxidizing() instanceof Waxed);
		}
		return true;
	}

	@Override
	public void set(CopperGolem golem) {
		if (state != null)
			golem.setWeatheringState(state);
		if (waxed.isTrue())
			golem.setOxidizing(CopperGolem.Oxidizing.waxed());
	}

	@Override
	protected boolean match(CopperGolem golem) {
		if (!dataMatch(state, golem.getWeatheringState()))
			return false;
		return kleeneanMatch(waxed, Kleenean.get(golem.getOxidizing() instanceof Waxed));
	}

	@Override
	public Class<? extends CopperGolem> getType() {
		return CopperGolem.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CopperGolemData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(state) + waxed.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CopperGolemData other))
			return false;
		return state == other.state && waxed == other.waxed;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CopperGolemData other))
			return false;
		return dataMatch(state, other.state) && kleeneanMatch(waxed, other.waxed);
	}

	@Override
	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		if (waxed.isTrue()) {
			builder.append("waxed ");
		} else if (waxed.isFalse()) {
			builder.append("unwaxed ");
		}
		if (state != null)
			builder.append(Language.getList("weathering copper states." + state.name())[0] + " ");
		builder.append("copper golem");
		if (isPlural().isTrue())
			builder.append("s");
		return builder.toString();
	}

}
