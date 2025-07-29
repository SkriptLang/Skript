package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TropicalFishData extends EntityData<TropicalFish> {

	private static final Patterns<Pattern> PATTERNS = new Patterns<>(new Object[][]{
		{"tropical fish", null},
		{"kob", Pattern.KOB},
		{"sunstreak", Pattern.SUNSTREAK},
		{"snooper", Pattern.SNOOPER},
		{"dasher", Pattern.DASHER},
		{"brinely", Pattern.BRINELY},
		{"spotty", Pattern.SPOTTY},
		{"flopper", Pattern.FLOPPER},
		{"stripey", Pattern.STRIPEY},
		{"glitter", Pattern.GLITTER},
		{"blockfish", Pattern.BLOCKFISH},
		{"betty", Pattern.BETTY},
		{"clayfish", Pattern.CLAYFISH},
	});
	private static final Pattern[] FISH_PATTERNS = Pattern.values();

	static {
		register(TropicalFishData.class, "tropical fish", TropicalFish.class, 0, PATTERNS.getPatterns());

		Variables.yggdrasil.registerSingleClass(Pattern.class, "TropicalFish.Pattern");
	}

	private @Nullable DyeColor bodyColor = null;
	private @Nullable DyeColor patternColor = null;
	private @Nullable Pattern fishPattern = null;

	public TropicalFishData() {}

	public TropicalFishData(@Nullable Pattern fishPattern, @Nullable DyeColor bodyColor, @Nullable DyeColor patternColor) {
		this.fishPattern = fishPattern;
		this.bodyColor = bodyColor;
		this.patternColor = patternColor;
		super.codeNameIndex = PATTERNS.getMatchedPattern(fishPattern, 0).orElse(0);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		fishPattern = PATTERNS.getInfo(matchedCodeName);
		if (exprs.length == 0)
			return true; // FIXME aliases reloading must work

		if (matchedPattern == 0) {
			if (exprs[0] != null) {
				//noinspection unchecked
				bodyColor = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
				if (exprs[1] != null)  {
					//noinspection unchecked
					patternColor = ((Literal<Color>) exprs[1]).getSingle().asDyeColor();
				}
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
			bodyColor = tropicalFish.getBodyColor();
			patternColor = tropicalFish.getPatternColor();
			fishPattern = tropicalFish.getPattern();
			super.codeNameIndex = PATTERNS.getMatchedPattern(fishPattern, 0).orElse(0);
		}
		return true;
	}

	@Override
	public void set(TropicalFish tropicalFish) {
		Pattern fishPattern = this.fishPattern;
		if (fishPattern == null)
			fishPattern = CollectionUtils.getRandom(FISH_PATTERNS);
		assert fishPattern != null;
		tropicalFish.setPattern(fishPattern);

		if (bodyColor != null)
			tropicalFish.setBodyColor(bodyColor);
		if (patternColor != null)
			tropicalFish.setPatternColor(patternColor);
	}

	@Override
	protected boolean match(TropicalFish tropicalFish) {
		if (!dataMatch(bodyColor, tropicalFish.getBodyColor()))
			return false;
		if (!dataMatch(patternColor, tropicalFish.getPatternColor()))
			return false;
		return dataMatch(fishPattern, tropicalFish.getPattern());
	}

	@Override
	public Class<? extends TropicalFish> getType() {
		return TropicalFish.class;
	}

	@Override
	public @NotNull EntityData<TropicalFish> getSuperType() {
		return new TropicalFishData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(fishPattern, bodyColor, patternColor);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof TropicalFishData other))
			return false;

		return fishPattern == other.fishPattern
			&& bodyColor == other.bodyColor
			&& patternColor == other.patternColor;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof TropicalFishData other))
			return false;

		if (!dataMatch(bodyColor, other.bodyColor))
			return false;
		if (!dataMatch(patternColor, other.patternColor))
			return false;
		return dataMatch(fishPattern, other.fishPattern);
	}

}
