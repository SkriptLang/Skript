package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Axis;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry and utility class for game effects that require data.
 */
public class DataGameEffects {

	private static final List<EffectInfo<Effect, ?>> GAME_EFFECT_INFOS = new ArrayList<>();

	/**
	 * Registers a new effect with a single data parameter.
	 * @param effect the effect
	 * @param pattern the pattern to use (must contain exactly one non-null expression)
	 * @param toString the toString function for this pattern
	 * @param <D> the data type
	 */
	@SuppressWarnings("unchecked")
	private static <D> void registerEffect(Effect effect, String pattern, ToString toString) {
		DataGameEffects.registerEffect(effect, pattern, (event, expressions, parseResult) -> (D) expressions[0].getSingle(event), toString);
	}

	/**
	 * Registers a new effect with a custom data supplier.
	 * @param effect the effect
	 * @param pattern the pattern to use
	 * @param dataSupplier the data supplier for this effect
	 * @param toString the toString function for this pattern
	 * @param <D> the data type
	 */
	private static <D> void registerEffect(Effect effect, String pattern, DataSupplier<D> dataSupplier, ToString toString) {
		GAME_EFFECT_INFOS.add(new EffectInfo<>(effect, pattern, dataSupplier, toString));
	}

	/**
	 * @return An unmodifiable list of all registered game effect infos.
	 */
	public static @Unmodifiable List<EffectInfo<Effect, ?>> getGameEffectInfos() {
		if (GAME_EFFECT_INFOS.isEmpty()) {
			registerAll();
		}
		return Collections.unmodifiableList(GAME_EFFECT_INFOS);
	}

	/**
	 * Looks up and calls the toString function for the given effect, using the provided context.
	 * @param effect the effect
	 * @param exprs the expressions to use in the toString function
	 * @param parseResult the parse result
	 * @param event the event
	 * @param debug whether to include debug information
	 * @return the string representation, or null if no matching effect was found
	 */
	public static @Nullable String toString(Effect effect, Expression<?> @NotNull [] exprs, ParseResult parseResult, Event event, boolean debug) {
		for (EffectInfo<Effect, ?> info : getGameEffectInfos()) {
			if (info.effect() == effect) {
				return info.toStringFunction().toString(exprs, parseResult, new SyntaxStringBuilder(event, debug)).toString();
			}
		}
		return null;
	}

	/**
	 * Registers all game effects that require data.
	 * Game effects without data are automatically handled by the enum parser.
	 */
	private static void registerAll() {
		registerEffect(Effect.RECORD_PLAY, "[record] song (of|using) %itemtype%",
			//<editor-fold desc="Material data supplier that only accepts music discs" defaultstate="collapsed">
			(event, expressions, parseResult) -> {
				Material material = DataSupplier.getMaterialData(event, expressions, parseResult);
				if (material == null || !MaterialTags.MUSIC_DISCS.isTagged(material))
					return null;
				return material;
			},
			//</editor-fold>
			(exprs, parseResult, builder) -> builder.append("record song of", exprs[0]));

		registerEffect(Effect.SMOKE, "[dispenser] black smoke effect [(in|with|using) [the] direction] %direction%",
			DataSupplier::getBlockFaceData,
			(exprs, parseResult, builder) -> builder.append("black smoke effect in direction", exprs[0]));

		registerEffect(Effect.SHOOT_WHITE_SMOKE, "[dispenser] white smoke effect [(in|with|using) [the] direction] %direction%",
			DataSupplier::getCartesianBlockFaceData,
			(exprs, parseResult, builder) -> builder.append("white smoke effect in direction", exprs[0]));

		registerEffect(Effect.STEP_SOUND, "%itemtype/blockdata% [foot]step[s] sound [effect]",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "footstep sound")); // handle version changes

		registerEffect(Effect.POTION_BREAK, "%color% [splash] potion break effect",
			DataSupplier::getColorData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "splash potion break effect"));

		registerEffect(Effect.INSTANT_POTION_BREAK, "%color% instant [splash] potion break effect",
			DataSupplier::getColorData,
			(exprs, parseResult, builder) -> builder.append(exprs[0], "instant splash potion break effect"));

		registerEffect(Effect.COMPOSTER_FILL_ATTEMPT, "compost[er] [fill[ing]] (succe(ss|ed)|1:fail[ure|ed]) sound [effect]",
			(event, expressions, parseResult) -> parseResult.mark == 0,
			(exprs, parseResult, builder) -> builder.append((parseResult.mark == 0 ? "composter filling success sound effect" : "composter filling failure sound effect")));

		//noinspection removal
		registerEffect(Effect.VILLAGER_PLANT_GROW, "villager plant grow[th] effect [(with|using) %-number% particles]",
			DataSupplier::getNumberDefault10,
			(exprs, parseResult, builder) -> builder.append("villager plant growth effect")
													.appendIf(exprs[0] != null, "with", exprs[0], "particles"));

		registerEffect(Effect.BONE_MEAL_USE, "[fake] bone meal effect [(with|using) %-number% particles]",
			DataSupplier::getNumberDefault10,
			(exprs, parseResult, builder) -> builder.append("bone meal effect with", exprs[0], "particles"));

		registerEffect(Effect.ELECTRIC_SPARK, "(electric|lightning[ rod]|copper) spark effect [(in|using|along) the (1:x|2:y|3:z) axis]",
			(event, expressions, parseResult) -> (parseResult.mark == 0 ? null : Axis.values()[parseResult.mark - 1]),
			(exprs, parseResult, builder) -> builder.append("electric spark effect")
													.appendIf(parseResult.mark != 0, "along the", Axis.values()[parseResult.mark - 1], "axis"));

		// 'data' is explicitly vague because the data value is extremely complex and not easily represented in Skript
		// The upper 26 bits represent the charge level, while the lower 6 bits represent a map of which block faces the
		// particles will appear on, but if no charge level is used, the particles appear to just be a standard sound
		// with some particles with offsets depending on the block's size. The upper 26 bits is intended to be a maximum
		// of 7, based on wiki max charge of 1000 and the formula `floor(ln(1 + charge of the block) ) + 1`.
		// there's more to it with how the particles roll and how often the sound plays, but I can't be bothered to
		// figure it out to a tee.
		registerEffect(Effect.PARTICLES_SCULK_CHARGE, "sculk (charge|spread) effect [(with|using) data %integer%]",
			(exprs, parseResult, builder) -> builder.append("sculk charge effect with data", exprs[0]));

		registerEffect(Effect.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE, "[finish] brush[ing] %itemtype/blockdata% effect",
			DataSupplier::getBlockData,
			(exprs, parseResult, builder) -> builder.append("brushing", exprs[0], "effect"));

		registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER, "trial spawner detect[ing|s] [%-number%] player[s] effect",
			DataSupplier::getNumberDefault1,
			(exprs, parseResult, builder) -> builder.append("trial spawner detecting")
													.appendIf(exprs[0] != null, exprs[0])
													.append("players effect"));

		registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS, "ominous trial spawner detect[ing|s] [%-number%] player[s] effect",
			DataSupplier::getNumberDefault1,
			(exprs, parseResult, builder) -> builder.append("ominous trial spawner detecting")
				.appendIf(exprs[0] != null, exprs[0])
				.append("players effect"));

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN, "[:ominous] trial spawner spawn[ing] [mob] effect",
			DataSupplier::isOminous,
			(exprs, parseResult, builder) -> builder.appendIf(parseResult.hasTag("ominous"), "ominous")
													.append("trial spawner spawning effect"));

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN_MOB_AT, "[:ominous] trial spawner spawn[ing] [mob] effect with sound",
			DataSupplier::isOminous,
			(exprs, parseResult, builder) -> builder.append((parseResult.hasTag("ominous") ? "ominous trial spawner spawning mob effect with sound" : "trial spawner spawning mob effect with sound")));

		registerEffect(Effect.BEE_GROWTH, "bee growth effect [(with|using) %-number% particles]",
			DataSupplier::getNumberDefault10,
			(exprs, parseResult, builder) -> builder.append("bee [plant] grow[th] effect with", exprs[0], "particles"));

		registerEffect(Effect.VAULT_ACTIVATE, "[:ominous] [trial] vault activate effect",
			DataSupplier::isOminous,
			(exprs, parseResult, builder) -> builder.appendIf(parseResult.hasTag("ominous"), "ominous")
													.append("trial vault activate effect"));

		registerEffect(Effect.VAULT_DEACTIVATE, "[:ominous] [trial] vault deactivate effect",
			DataSupplier::isOminous,
			(exprs, parseResult, builder) -> builder.appendIf(parseResult.hasTag("ominous"), "ominous")
													.append("trial vault deactivate effect"));

		registerEffect(Effect.TRIAL_SPAWNER_BECOME_OMINOUS, "trial spawner become[ing] [:not] ominous effect",
			(event, expressions, parseResult) -> !parseResult.hasTag("not"),
			(exprs, parseResult, builder) -> builder.append("trial spawner becoming")
													.appendIf(parseResult.hasTag("not"), "not")
													.append("ominous effect"));

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN_ITEM, "[:ominous] trial spawner spawn[ing] item effect",
			DataSupplier::isOminous,
			(exprs, parseResult, builder) -> builder.appendIf(parseResult.hasTag("ominous"), "ominous")
													.append("trial spawner spawning item effect"));

		registerEffect(Effect.TURTLE_EGG_PLACEMENT, "place turtle egg effect [(with|using) %-number% particles]",
			DataSupplier::getNumberDefault10,
			(exprs, parseResult, builder) -> builder.append("place turtle egg effect with", exprs[0], "particles"));

		registerEffect(Effect.SMASH_ATTACK, "[mace] smash attack effect [(with|using) %-number% particles]",
			DataSupplier::getNumberDefault10,
			(exprs, parseResult, builder) -> builder.append("smash attack effect with", exprs[0], "particles"));
	}

}
