package org.skriptlang.skript.bukkit.mannequin.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.function.Consumer;

@Name("Mannequin Skin Parts")
@Description("Enable or disable a skin part of a mannequin.")
@Example("enable the mannequin cape for {_mannequin}")
@Example("show the mannequin hat skin part for {_mannequin}")
@Example("reveal the mannequin jacket for {_mannequin}")
@Example("disable the mannequin left pants skin for {_mannequin}")
@Example("hide the mannequin left sleeve for {_mannequin}")
@Example("enable all of the mannequin skin parts for {_mannequin}")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class EffMannequinParts extends Effect {

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
			pattern = "((enable|show|reveal)|disable:(disable|hide)) all [[of] the] mannequin skin parts (of|for) %entities%";
		} else {
			pattern = "((enable|show|reveal)|disable:(disable|hide)) [the] mannequin " + part.part + " [skin [part[s]]] (of|for) %entities%";
		}
		return new Object[] {pattern, part};
	}

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffMannequinParts.class)
				.addPatterns(PATTERNS.getPatterns())
				.supplier(EffMannequinParts::new)
				.build()
		);
	}

	public enum MannequinPart {
		CAPE("cape"),
		HAT("hat"),
		JACKET("jacket"),
		LEFT_PANTS("left pants"),
		LEFT_SLEEVE("left sleeve"),
		RIGHT_PANTS("right pants"),
		RIGHT_SLEEVE("right sleeve"),
		PANTS("pants"),
		SLEEVES("sleeves"),
		ALL("all")
		;

		public final String part;

		MannequinPart(String part) {
			this.part = part;
		}

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
	protected void execute(Event event) {
		Consumer<SkinParts.Mutable> changer = switch (mannequinPart) {
			case CAPE -> skin -> skin.setCapeEnabled(enable);
			case HAT -> skin -> skin.setHatsEnabled(enable);
			case JACKET -> skin -> skin.setJacketEnabled(enable);
			case LEFT_PANTS -> skin -> skin.setLeftPantsEnabled(enable);
			case LEFT_SLEEVE -> skin -> skin.setLeftSleeveEnabled(enable);
			case RIGHT_PANTS -> skin -> skin.setRightPantsEnabled(enable);
			case RIGHT_SLEEVE -> skin -> skin.setRightSleeveEnabled(enable);
			case PANTS -> skin -> {
				skin.setRightPantsEnabled(enable);
				skin.setLeftPantsEnabled(enable);
			};
			case SLEEVES -> skin -> {
				skin.setRightSleeveEnabled(enable);
				skin.setLeftSleeveEnabled(enable);
			};
			case ALL -> skin -> {
				skin.setCapeEnabled(enable);
				skin.setHatsEnabled(enable);
				skin.setJacketEnabled(enable);
				skin.setLeftPantsEnabled(enable);
				skin.setLeftSleeveEnabled(enable);
				skin.setRightPantsEnabled(enable);
				skin.setRightSleeveEnabled(enable);
			};
		};

		for (Entity entity : entities.getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			SkinParts.Mutable skinParts = mannequin.getSkinParts();
			changer.accept(skinParts);
			mannequin.setSkinParts(skinParts);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (enable) {
			builder.append("enable");
		} else {
			builder.append("disable");
		}
		if (mannequinPart == MannequinPart.ALL) {
			builder.append("all of the mannequin skin parts");
		} else {
			builder.append("the mannequin", mannequinPart.part);
		}
		builder.append("of", entities);
		return builder.toString();
	}

}
