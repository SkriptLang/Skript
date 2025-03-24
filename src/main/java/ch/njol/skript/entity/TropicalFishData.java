package ch.njol.skript.entity;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

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
		matchedPattern = pattern.ordinal();
	}

	private TropicalFishData(int pattern) {
		matchedPattern = pattern;
	}

	private @Nullable DyeColor patternColor;
	private @Nullable DyeColor bodyColor;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs.length == 0)
			return true; // FIXME aliases reloading must work
		
		if (exprs[2] != null) {
			bodyColor = ((Literal<Color>) exprs[2]).getSingle().asDyeColor();
			patternColor = bodyColor;
		}

		if (exprs[0] != null)
			bodyColor = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
		if (exprs[1] != null)
			patternColor = ((Literal<Color>) exprs[1]).getSingle().asDyeColor();

		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends TropicalFish> c, @Nullable TropicalFish tropicalFish) {
		if (tropicalFish != null) {
			matchedPattern = tropicalFish.getPattern().ordinal();
			bodyColor = tropicalFish.getBodyColor();
			patternColor = tropicalFish.getPatternColor();
		}
		return true;
	}

	@Override
	public void set(TropicalFish entity) {
		if (matchedPattern == patterns.length)
			entity.setPattern(patterns[ThreadLocalRandom.current().nextInt(patterns.length)]);
		else
			entity.setPattern(patterns[matchedPattern]);

		if (bodyColor != null)
			entity.setBodyColor(bodyColor);
		if (patternColor != null)
			entity.setPatternColor(patternColor);
	}

	@Override
	protected boolean match(TropicalFish entity) {
		boolean samePattern = matchedPattern == patterns.length || matchedPattern == entity.getPattern().ordinal();
		boolean sameBody = bodyColor == null || bodyColor == entity.getBodyColor();

		if (patternColor == null)
			return samePattern && sameBody;
		else
			return samePattern && sameBody && patternColor == entity.getPatternColor();
	}

	@Override
	public Class<? extends TropicalFish> getType() {
		return TropicalFish.class;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof TropicalFishData fishData))
			return false;

		return matchedPattern == fishData.matchedPattern
			&& bodyColor == fishData.bodyColor && patternColor == fishData.patternColor;
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(matchedPattern, bodyColor, patternColor);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof TropicalFishData fishData))
			return false;

		return matchedPattern == fishData.matchedPattern
			&& bodyColor == fishData.bodyColor && patternColor == fishData.patternColor;
	}

	@Override
	public EntityData<TropicalFish> getSuperType() {
		return new TropicalFishData(matchedPattern);
	}
}
