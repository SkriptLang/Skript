/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.entity;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

public class TropicalFishData extends EntityData<TropicalFish> {

	@SuppressWarnings("null")
	private static Object[] patterns;

	static {
		register(TropicalFishData.class, "tropical fish", TropicalFish.class, 0,
				"tropical fish", "kob", "sunstreak", "snooper",
				"dasher", "brinely", "spotty", "flopper",
				"stripey", "glitter", "blockfish", "betty", "clayfish");
		patterns = Pattern.values();
	}

	public TropicalFishData() {
		this(0);
	}

	public TropicalFishData(Pattern pattern) {
		matchedPattern = pattern.ordinal() + 1;
	}

	private TropicalFishData(int pattern) {
		matchedPattern = pattern;
	}

	@Nullable
	private DyeColor patternColor;
	@Nullable
	private DyeColor bodyColor;

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
			matchedPattern = tropicalFish.getPattern().ordinal() + 1;
			bodyColor = tropicalFish.getBodyColor();
			patternColor = tropicalFish.getPatternColor();
		}
		return true;
	}

	@Override
	public void set(TropicalFish entity) {
		if (matchedPattern == 0)
			entity.setPattern((Pattern) patterns[ThreadLocalRandom.current().nextInt(patterns.length)]);
		else
			entity.setPattern((Pattern) patterns[matchedPattern]);

		if (bodyColor != null)
			entity.setBodyColor(bodyColor);
		if (patternColor != null)
			entity.setPatternColor(patternColor);
	}

	@Override
	protected boolean match(TropicalFish entity) {
		boolean samePattern = matchedPattern == 0 || matchedPattern == entity.getPattern().ordinal() + 1;
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
		if (!(obj instanceof TropicalFishData))
			return false;

		TropicalFishData other = (TropicalFishData) obj;
		return matchedPattern == other.matchedPattern
			&& bodyColor == other.bodyColor && patternColor == other.patternColor;
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(matchedPattern, bodyColor, patternColor);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof TropicalFishData))
			return false;

		TropicalFishData other = (TropicalFishData) e;
		return matchedPattern == other.matchedPattern
			&& bodyColor == other.bodyColor && patternColor == other.patternColor;
	}

	@Override
	public EntityData getSuperType() {
		return new TropicalFishData(matchedPattern);
	}
}
