package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.registrations.Classes;
import org.bukkit.Axis;
import org.bukkit.Effect;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataGameEffects {
	private static final List<EffectInfo<Effect, ?>> GAME_EFFECT_INFOS = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private static <D> void registerEffect(Effect effect, String pattern, ToString<D> toString) {
		DataGameEffects.registerEffect(effect, pattern, (event, expressions, parseResult) -> (D) expressions[0].getSingle(event), toString);
	}

	private static <D> void registerEffect(Effect effect, String pattern, DataSupplier<D> dataSupplier, ToString<D> toString) {
		GAME_EFFECT_INFOS.add(new EffectInfo<>(effect, pattern, dataSupplier, toString));
	}

	public static @Unmodifiable List<EffectInfo<Effect, ?>> getGameEffectInfos() {
		if (GAME_EFFECT_INFOS.isEmpty()) {
			registerAll();
		}
		return Collections.unmodifiableList(GAME_EFFECT_INFOS);
	}

	private static void registerAll() {
		registerEffect(Effect.RECORD_PLAY, "[record] song (of|using) %itemtype%",
			DataSupplier::getMaterialData,
			data -> "record song of " + Classes.toString(data));

		registerEffect(Effect.SMOKE, "[dispenser] black smoke effect [(in|with|using) direction] %direction%",
			DataSupplier::getBlockFaceData,
			data -> "black smoke effect in direction " + Classes.toString(data));

		registerEffect(Effect.SHOOT_WHITE_SMOKE, "[dispenser] white smoke effect [(in|with|using) direction] %direction%",
			DataSupplier::getBlockFaceData,
			data -> "white smoke effect in direction " + Classes.toString(data));

		registerEffect(Effect.STEP_SOUND, "[foot]step sound [effect] (on|of|using) %itemtype/blockdata%",
			data -> "footstep sound of " + Classes.toString(data)); // handle version changes

		registerEffect(Effect.POTION_BREAK, "%color% [splash] potion break effect",
			DataSupplier::getColorData,
			data -> Classes.toString(data) + " splash potion break effect");

		registerEffect(Effect.INSTANT_POTION_BREAK, "%color% instant [splash] potion break effect",
			DataSupplier::getColorData,
			data -> Classes.toString(data) + " instant splash potion break effect");

		registerEffect(Effect.COMPOSTER_FILL_ATTEMPT, "[composter] fill[ing] (succe[ss|ed]|1:fail[ure]) sound [effect]",
			(event, expressions, parseResult) -> parseResult.mark == 0,
			data -> (data ? "composter filling success sound effect" : "composter filling failure sound effect"));

		//noinspection removal
		registerEffect(Effect.VILLAGER_PLANT_GROW, "villager plant grow[th] effect [(with|using) %-number% particles]",
			DataSupplier::defaultTo10Particles,
			data -> "villager plant growth effect with " + Classes.toString(data) + " particles");

		registerEffect(Effect.BONE_MEAL_USE, "[fake] bone meal effect [(with|using) %-number% particles]",
			DataSupplier::defaultTo10Particles,
			data -> "bone meal effect with " + Classes.toString(data) + " particles");

		registerEffect(Effect.ELECTRIC_SPARK, "(electric|lightning[ rod]|copper) spark effect [(in|using|along) the (1:x|2:y|3:z) axis]",
			(event, expressions, parseResult) -> (parseResult.mark == 0 ? null : Axis.values()[parseResult.mark - 1]),
			data -> "electric spark effect along the " + (data == null ? "default" : Classes.toString(data)) + " axis");

		registerEffect(Effect.PARTICLES_SCULK_CHARGE, "sculk (charge|spread) effect [(with|using) data %number%]",
			data -> "sculk charge effect with data " + Classes.toString(data)); // data explanation here https://discord.com/channels/135877399391764480/836220422223036467/1211040434852208660

		registerEffect(Effect.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE, "[finish] brush[ing] %itemtype/blockdata% effect",
			DataSupplier::getBlockData,
			data -> "brushing " + Classes.toString(data) + " effect");

		registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER, "trial spawner detect[ing|s] [%-number%] player[s] effect",
			DataSupplier::defaultTo1Player,
			data -> "trial spawner detecting " + Classes.toString(data) + " players effect");

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN, "[:ominous] trial spawner spawn[ing] effect",
			DataSupplier::isOminous,
			data -> (data ? "ominous trial spawner spawning effect" : "trial spawner spawning effect"));

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN_MOB_AT, "[:ominous] trial spawner spawn[ing] mob effect with sound",
			DataSupplier::isOminous,
			data -> (data ? "ominous trial spawner spawning mob effect with sound" : "trial spawner spawning mob effect with sound"));

		registerEffect(Effect.BEE_GROWTH, "bee growth effect [(with|using) %-number% particles]",
			DataSupplier::defaultTo10Particles,
			data -> "bee growth effect with " + Classes.toString(data) + " particles");

		registerEffect(Effect.VAULT_ACTIVATE, "[:ominous] [trial] vault activate effect",
			DataSupplier::isOminous,
			data -> (data ? "ominous trial vault activate effect" : "trial vault activate effect"));

		registerEffect(Effect.VAULT_DEACTIVATE, "[:ominous] [trial] vault deactivate effect",
			DataSupplier::isOminous,
			data -> (data ? "ominous trial vault deactivate effect" : "trial vault deactivate effect"));

		registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS, "ominous trial spawner detect[ing|s] [%-number%] player[s] effect",
			DataSupplier::defaultTo1Player,
			data -> "ominous trial spawner detecting " + Classes.toString(data) + " players effect");

		registerEffect(Effect.TRIAL_SPAWNER_BECOME_OMINOUS, "trial spawner become[ing] [:not] ominous effect",
			(event, expressions, parseResult) -> !parseResult.hasTag("not"),
			data -> (data ? "trial spawner becoming ominous effect" : "trial spawner becoming not ominous effect"));

		registerEffect(Effect.TRIAL_SPAWNER_SPAWN_ITEM, "[:ominous] trial spawner spawn[ing] item effect",
			DataSupplier::isOminous,
			data -> (data ? "ominous trial spawner spawning item effect" : "trial spawner spawning item effect"));

		registerEffect(Effect.TURTLE_EGG_PLACEMENT, "place turtle egg effect [(with|using) %-number% particles]",
			DataSupplier::defaultTo10Particles,
			data -> "place turtle egg effect with " + Classes.toString(data) + " particles");

		registerEffect(Effect.SMASH_ATTACK, "[mace] smash attack effect [(with|using) %-number% particles]",
			DataSupplier::defaultTo10Particles,
			data -> "smash attack effect with " + Classes.toString(data) + " particles");
	}
}
