package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Sheep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class SheepData extends EntityData<Sheep> {

	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"sheep", Kleenean.UNKNOWN},
		{"sheared sheep", Kleenean.TRUE},
		{"unsheared sheep", Kleenean.FALSE}
	});

	static {
		EntityData.register(SheepData.class, "sheep", Sheep.class, 0, PATTERNS.getPatterns());
	}

	private Color @Nullable [] colors = null;
	private Kleenean sheared = Kleenean.UNKNOWN;
	private Adjective @Nullable [] adjectives = null;

	public SheepData() {}

	public SheepData(@Nullable Kleenean sheared, Color @Nullable [] colors) {
		this.sheared = sheared != null ? sheared : Kleenean.UNKNOWN;
		this.colors = colors;
		super.codeNameIndex = PATTERNS.getMatchedPattern(this.sheared, 0).orElse(0);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		sheared = PATTERNS.getInfo(matchedCodeName);
		if (exprs[0] != null) {
			//noinspection unchecked
			colors = ((Literal<Color>) exprs[0]).getAll();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Sheep> entityClass, @Nullable Sheep sheep) {
		if (sheep != null) {
			sheared = Kleenean.get(sheep.isSheared());
			colors = CollectionUtils.array(SkriptColor.fromDyeColor(sheep.getColor()));
			super.codeNameIndex = PATTERNS.getMatchedPattern(sheared, 0).orElse(0);
		}
		return true;
	}

	@Override
	public void set(Sheep sheep) {
		if (colors != null) {
			Color color = CollectionUtils.getRandom(colors);
			assert color != null;
			sheep.setColor(color.asDyeColor());
		}
		sheep.setSheared(sheared.isTrue());
	}

	@Override
	public boolean match(Sheep sheep) {
		if (!kleeneanMatch(sheared, sheep.isSheared()))
			return false;
		return colors == null || SimpleExpression.check(colors, c -> sheep.getColor() == c.asDyeColor(), false, false);
	}

	@Override
	public Class<Sheep> getType() {
		return Sheep.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new SheepData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(colors);
		result = prime * result + sheared.hashCode();
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof SheepData other))
			return false;
		if (!Arrays.equals(colors, other.colors))
			return false;
        return sheared == other.sheared;
    }

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof SheepData other))
			return false;
		if (!kleeneanMatch(sheared, other.sheared))
			return false;
		return colors == null || CollectionUtils.isSubset(colors, other.colors);
	}

	@Override
	public String toString(int flags) {
		Color[] colors = this.colors;
		if (colors == null)
			return super.toString(flags);
		Adjective[] adjectives = this.adjectives;
		if (adjectives == null) {
			this.adjectives = adjectives = new Adjective[colors.length];
			for (int i = 0; i < colors.length; i++)
				if (colors[i] instanceof SkriptColor skriptColor)
					adjectives[i] = skriptColor.getAdjective();
		}
		Noun name = getName();
		Adjective age = getAgeAdjective();
		return name.getArticleWithSpace(flags) + (age == null ? "" : age.toString(name.getGender(), flags) + " ")
			+ Adjective.toString(adjectives, name.getGender(), flags, false) + " " + name.toString(flags & Language.NO_ARTICLE_MASK);
	}

}
