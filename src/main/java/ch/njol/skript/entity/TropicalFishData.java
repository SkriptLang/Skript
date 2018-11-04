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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.entity;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;

public class TropicalFishData extends EntityData<TropicalFish> {

	@SuppressWarnings("null")
	private static Object[] patterns;

	static {
		if (Skript.isRunningMinecraft(1, 13)) {
			register(TropicalFishData.class, "tropical fish", TropicalFish.class, 0,
					"tropical fish", "kob", "sunstreak", "snooper",
					"dasher", "brinely", "spotty", "flopper",
					"stripey", "glitter", "blockfish", "betty", "clayfish");
			patterns = Pattern.values();
		}
	}

	public TropicalFishData() {
		this(0);
	}

	public TropicalFishData(int pattern) {
		this.pattern = pattern;
	}

	@Nullable
	private DyeColor patternColor;
	@Nullable
	private DyeColor bodyColor;
	private int pattern = -1;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern != 0)
			pattern = matchedPattern - 1;
		
		if (exprs[2] != null) {
			bodyColor = ((Literal<Color>) exprs[2]).getSingle().getWoolColor();
			patternColor = bodyColor;
		}

		if (exprs[0] != null)
			bodyColor = ((Literal<Color>) exprs[0]).getSingle().getWoolColor();
		if (exprs[1] != null)
			patternColor = ((Literal<Color>) exprs[1]).getSingle().getWoolColor();

		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends TropicalFish> c, @Nullable TropicalFish tropicalFish) {
		if (tropicalFish != null) {
			pattern = tropicalFish.getPattern().ordinal();
			bodyColor = tropicalFish.getBodyColor();
			patternColor = tropicalFish.getPatternColor();
		}
		return true;
	}

	@Override
	public void set(TropicalFish entity) {
		if (pattern == -1)
			entity.setPattern((Pattern) patterns[ThreadLocalRandom.current().nextInt(patterns.length)]);
		else
			entity.setPattern((Pattern) patterns[pattern]);

		if (bodyColor != null)
			entity.setBodyColor(bodyColor);
		if (patternColor != null)
			entity.setPatternColor(patternColor);
	}

	@Override
	protected boolean match(TropicalFish entity) {
		boolean samePattern = pattern == -1 || pattern == entity.getPattern().ordinal();
		boolean sameBody = bodyColor != null ? bodyColor == entity.getBodyColor() : true;

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
		return pattern == other.pattern && bodyColor == other.bodyColor && patternColor == other.patternColor;
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(pattern, bodyColor, patternColor);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> e) {
		if (!(e instanceof TropicalFishData))
			return false;

		TropicalFishData other = (TropicalFishData) e;
		return pattern == other.pattern && bodyColor == other.bodyColor && patternColor == other.patternColor;
	}

	@Override
	public EntityData getSuperType() {
		return new TropicalFishData(pattern);
	}
}
