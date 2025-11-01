package org.skriptlang.skript.bukkit.mannequin.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.SkinParts;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.mannequin.elements.EffMannequinParts.MannequinPart;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.function.Function;

@Name("Mannequin Skin Part is Enabled")
@Description("Whether a specific skin part of a mannequin is enabled or disabled.")
@Example("")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class CondMannequinParts extends Condition {

	private static final Patterns<MannequinPart> PATTERNS = new Patterns<>(new Object[][]{
		getPattern(MannequinPart.CAPE),
		getPattern(MannequinPart.HAT),
		getPattern(MannequinPart.JACKET),
		getPattern(MannequinPart.LEFT_PANTS),
		getPattern(MannequinPart.LEFT_SLEEVE),
		getPattern(MannequinPart.RIGHT_PANTS),
		getPattern(MannequinPart.RIGHT_SLEEVE),
		getPattern(MannequinPart.PANTS),
		getPattern(MannequinPart.SLEEVES),
		getPattern(MannequinPart.ALL)
	});

	private static Object[] getPattern(MannequinPart part) {
		String pattern;
		if (part == MannequinPart.ALL) {
			pattern = "all [[of] the] mannequin skin parts of %entities% are ((enabled|showing|revealed)|disable:(disabled|hidden))";
		} else {
			pattern = "[the] mannequin " + part.part + " [skin] [part[s]] of %entities% (is|are) ((enabled|showing|revealed)|disable:(disabled|hidden))";
		}
		return new Object[] {pattern, part};
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondMannequinParts.class)
				.addPatterns(PATTERNS.getPatterns())
				.supplier(CondMannequinParts::new)
				.build()
		);
	}

	private Expression<Entity> entities;
	private boolean enable;
	private MannequinPart mannequinPart;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[0];
		enable = !parseResult.hasTag("disable");
		mannequinPart = PATTERNS.getInfo(matchedPattern);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Function<SkinParts, Boolean> checker = switch (mannequinPart) {
			case CAPE -> SkinParts::hasCapeEnabled;
			case HAT -> SkinParts::hasHatsEnabled;
			case JACKET -> SkinParts::hasJacketEnabled;
			case LEFT_PANTS -> SkinParts::hasLeftPantsEnabled;
			case LEFT_SLEEVE -> SkinParts::hasLeftSleeveEnabled;
			case RIGHT_PANTS -> SkinParts::hasRightPantsEnabled;
			case RIGHT_SLEEVE -> SkinParts::hasRightSleeveEnabled;
			case PANTS -> skin -> skin.hasRightPantsEnabled() && skin.hasRightPantsEnabled();
			case SLEEVES -> skin -> skin.hasRightSleeveEnabled() && skin.hasLeftSleeveEnabled();
			case ALL -> skin -> skin.hasCapeEnabled() && skin.hasHatsEnabled() && skin.hasJacketEnabled()
				&& skin.hasLeftPantsEnabled() && skin.hasLeftSleeveEnabled() && skin.hasRightPantsEnabled()
				&& skin.hasRightSleeveEnabled();
		};
		return entities.check(event, entity -> {
			if (!(entity instanceof Mannequin mannequin))
				return false;
			return checker.apply(mannequin.getSkinParts()) == enable;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (mannequinPart == MannequinPart.ALL) {
			builder.append("all of the mannequin skin parts");
		} else {
			builder.append("the mannequin", mannequinPart.part, "skin part");
		}
		builder.append("of", entities, "are");
		if (enable) {
			builder.append("enabled");
		} else {
			builder.append("disabled");
		}
		return builder.toString();
	}

}
