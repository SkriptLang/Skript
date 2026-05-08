package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class TropicalFishData extends EntityData<TropicalFish> {

	private static final Pattern[] FISH_PATTERNS = Pattern.values();

	private static final EntityDataPatterns<Pattern> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "tropical fish¦es @a", getPatterns("")),
		new PatternGroup<>(1, "betty¦s @a", Pattern.BETTY, getPatterns("betty")),
		new PatternGroup<>(2, "blockfish¦s @a", Pattern.BLOCKFISH, getPatterns("blockfish")),
		new PatternGroup<>(3, "brinely¦s @a", Pattern.BRINELY, getPatterns("brinely")),
		new PatternGroup<>(4, "clayfish¦s @a", Pattern.CLAYFISH, getPatterns("clayfish")),
		new PatternGroup<>(5, "dasher¦s @a", Pattern.DASHER, getPatterns("dasher")),
		new PatternGroup<>(6, "flopper¦s @a", Pattern.FLOPPER, getPatterns("flopper")),
		new PatternGroup<>(7, "glitter¦s @a", Pattern.GLITTER, getPatterns("glitter")),
		new PatternGroup<>(8, "kob¦s @a", Pattern.KOB, getPatterns("kob")),
		new PatternGroup<>(9, "snooper¦s @a", Pattern.SNOOPER, getPatterns("snooper")),
		new PatternGroup<>(10, "spotty¦s @a", Pattern.SPOTTY, getPatterns("spotty")),
		new PatternGroup<>(11, "stripey¦s @a", Pattern.STRIPEY, getPatterns("stripey")),
		new PatternGroup<>(12, "sunstreak¦s @a", Pattern.SUNSTREAK, getPatterns("sunstreak"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "[%-color%[-%-color%]] tropical fish[plural:es]";
		String second = "fully %-color% tropical fish[plural:es]";
		if (!prefix.isEmpty()) {
			first = "[%-color%[-%-color%]] " + prefix + "[plural:s]";
			second = "fully %-color% " + prefix + "[plural:s]";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(TropicalFishData.class, "tropical fish")
				.dataPatterns(GROUPS)
				.entityType(EntityType.TROPICAL_FISH)
				.entityClass(TropicalFish.class)
				.supplier(TropicalFishData::new)
				.build()
		);

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
		super.groupIndex = GROUPS.getIndex(fishPattern);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		fishPattern = GROUPS.getData(matchedGroup);
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
			super.groupIndex = GROUPS.getIndex(fishPattern);
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
