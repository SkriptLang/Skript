package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class TropicalFishData extends EntityData<TropicalFish> {

	private static final Pattern[] patterns = Pattern.values();

	static {
		register(TropicalFishData.class, "tropical fish", TropicalFish.class, 12,
				"kob", "sunstreak", "snooper", "dasher",
				"brinely", "spotty", "flopper", "stripey",
				"glitter", "blockfish", "betty", "clayfish", "tropical fish");
	}

	public TropicalFishData() {
		this(0);
	}

	public TropicalFishData(Pattern pattern) {
		matchedCodeName = pattern.ordinal();
	}

	private TropicalFishData(int pattern) {
		matchedCodeName = pattern;
	}

	private @Nullable DyeColor patternColor;
	private @Nullable DyeColor bodyColor;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs.length == 0)
			return true; // FIXME aliases reloading must work

		if (matchedPattern == 0) {
			if (exprs[0] != null) {
				//noinspection unchecked
				bodyColor = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
			}
			if (exprs[1] != null)  {
				//noinspection unchecked
				patternColor = ((Literal<Color>) exprs[1]).getSingle().asDyeColor();
			}
		} else if (exprs[0] != null) {
			//noinspection unchecked
			bodyColor = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
			patternColor = bodyColor;
		}

		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends TropicalFish> entityClass, @Nullable TropicalFish tropicalFish) {
		if (tropicalFish != null) {
			matchedCodeName = tropicalFish.getPattern().ordinal();
			bodyColor = tropicalFish.getBodyColor();
			patternColor = tropicalFish.getPatternColor();
		}
		return true;
	}

	@Override
	public void set(TropicalFish tropicalFish) {
		if (matchedCodeName == patterns.length) {
			tropicalFish.setPattern(patterns[ThreadLocalRandom.current().nextInt(patterns.length)]);
		} else {
			tropicalFish.setPattern(patterns[matchedCodeName]);
		}

		if (bodyColor != null)
			tropicalFish.setBodyColor(bodyColor);
		if (patternColor != null)
			tropicalFish.setPatternColor(patternColor);
	}

	@Override
	protected boolean match(TropicalFish tropicalFish) {
		boolean samePattern = matchedCodeName == patterns.length || matchedCodeName == tropicalFish.getPattern().ordinal();
		boolean sameBody = bodyColor == null || bodyColor == tropicalFish.getBodyColor();

		if (patternColor == null) {
			return samePattern && sameBody;
		} else {
			return samePattern && sameBody && patternColor == tropicalFish.getPatternColor();
		}
	}

	@Override
	public Class<? extends TropicalFish> getType() {
		return TropicalFish.class;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof TropicalFishData fishData))
			return false;

		return matchedCodeName == fishData.matchedCodeName
			&& bodyColor == fishData.bodyColor && patternColor == fishData.patternColor;
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(matchedCodeName, bodyColor, patternColor);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof TropicalFishData fishData))
			return false;

		return matchedCodeName == fishData.matchedCodeName
			&& bodyColor == fishData.bodyColor && patternColor == fishData.patternColor;
	}

	@Override
	public @NotNull EntityData<TropicalFish> getSuperType() {
		return new TropicalFishData(matchedCodeName);
	}

}
