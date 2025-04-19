package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.Axis;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;

import java.util.ArrayList;
import java.util.List;

public class ExprGameEffect extends SimpleExpression<GameEffect> {

	private static final Patterns<EffectPattern> PATTERNS;

	private static final List<EffectPattern> REGISTERED_PATTERNS = new ArrayList<>();

	private static void registerEffect(Effect effect, String pattern) {
	 		registerEffect(effect, pattern, (event, expressions, parseResult) -> expressions[0].getSingle(event));
	}

	private static void registerEffect(Effect effect, String pattern, GetData<?> getData) {
		REGISTERED_PATTERNS.add(new EffectPattern(effect, pattern, getData));
	}

	static {
		registerEffect(Effect.RECORD_PLAY, "[record] song (of|using) %itemtype%", GetData::getMaterialData);
		registerEffect(Effect.SMOKE, "[dispenser] black smoke effect [(in|with|using) direction] %direction%",
				GetData::getBlockFaceData);
		registerEffect(Effect.SHOOT_WHITE_SMOKE, "[dispenser] white smoke effect [(in|with|using) direction] %direction%",
				GetData::getBlockFaceData);
		registerEffect(Effect.STEP_SOUND, "[foot]step sound [effect] (on|of|using) %itemtype/blockdata%"); // handle version changes
		registerEffect(Effect.POTION_BREAK, "[splash] potion break effect (with|of|using) [colour] %color%",
				GetData::getColorData); // paper changes this type from potion data to color
		registerEffect(Effect.INSTANT_POTION_BREAK, "instant [splash] potion break effect (with|of|using) [colour] %color%", 
				GetData::getColorData);
		registerEffect(Effect.COMPOSTER_FILL_ATTEMPT, "[composter] fill[ing] (succe[ss|ed]|1:fail[ure]) sound [effect]",
				(event, expressions, parseResult) -> parseResult.mark == 0);
		
		if (!Skript.isRunningMinecraft(1, 20, 5)) {
			//noinspection removal
			registerEffect(Effect.VILLAGER_PLANT_GROW, "villager plant grow[th] effect [(with|using) %-number% particles]",
					GetData::defaultTo10Particles);
		}
		
		registerEffect(Effect.BONE_MEAL_USE, "[fake] bone meal effect [(with|using) %-number% particles]", 
				GetData::defaultTo10Particles);
		registerEffect(Effect.ELECTRIC_SPARK, "(electric|lightning[ rod]|copper) spark effect [(in|using|along) the (1:x|2:y|3:z) axis]",
				(event, expressions, parseResult) -> (parseResult.mark == 0 ? null : Axis.values()[parseResult.mark - 1]));

		// All modern ones are Paper only
		if (Skript.fieldExists(Effect.class, "PARTICLES_SCULK_CHARGE")) {
			if (Skript.isRunningMinecraft(1, 20, 1)) {
				registerEffect(Effect.PARTICLES_SCULK_CHARGE, "sculk (charge|spread) effect [(with|using) data %number%]"); // data explanation here https://discord.com/channels/135877399391764480/836220422223036467/1211040434852208660
				registerEffect(Effect.PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE, "[finish] brush[ing] effect (with|using) %itemtype/blockdata%");
			}
			if (Skript.isRunningMinecraft(1, 20, 4)) {
				registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER, "trial spawner detect[ing|s] [%-number%] player[s] effect", 
						GetData::defaultTo1Player);
				registerEffect(Effect.TRIAL_SPAWNER_SPAWN, "[:ominous] trial spawner spawn[ing] effect", 
						GetData::isOminous);
				registerEffect(Effect.TRIAL_SPAWNER_SPAWN_MOB_AT, "[:ominous] trial spawner spawn[ing] mob effect with sound", 
						GetData::isOminous);
			}
			if (Skript.isRunningMinecraft(1, 20, 5)) {
				registerEffect(Effect.BEE_GROWTH, "bee growth effect [(with|using) %-number% particles]", 
						GetData::defaultTo10Particles);
				registerEffect(Effect.VAULT_ACTIVATE, "[:ominous] [trial] vault activate effect",
						GetData::isOminous);
				registerEffect(Effect.VAULT_DEACTIVATE, "[:ominous] [trial] vault deactivate effect", 
						GetData::isOminous);
				registerEffect(Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS, "ominous trial spawner detect[ing|s] [%-number%] player[s] effect", 
						GetData::defaultTo1Player);
				registerEffect(Effect.TRIAL_SPAWNER_BECOME_OMINOUS, "trial spawner become[ing] [:not] ominous effect",
						(event, expressions, parseResult) -> !parseResult.hasTag("not"));
				registerEffect(Effect.TRIAL_SPAWNER_SPAWN_ITEM, "[:ominous] trial spawner spawn[ing] item effect", 
						GetData::isOminous);
				registerEffect(Effect.TURTLE_EGG_PLACEMENT, "place turtle egg effect [(with|using) %-number% particles]", 
						GetData::defaultTo10Particles);
				registerEffect(Effect.SMASH_ATTACK, "[mace] smash attack effect [(with|using) %-number% particles]", 
						GetData::defaultTo10Particles);
			}
		}

		// create Patterns object
		Object[][] patterns = new Object[REGISTERED_PATTERNS.size()][2];
		int i = 0;
		for (EffectPattern effectPattern : REGISTERED_PATTERNS) {
			patterns[i][0] = effectPattern.pattern;
			patterns[i][1] = effectPattern;
			i++;
		}
		PATTERNS = new Patterns<>(patterns);

		Skript.registerExpression(ExprGameEffect.class, GameEffect.class, ExpressionType.COMBINED, PATTERNS.getPatterns());
	}

	private GameEffect gameEffect;
	private GetData<?> getData;
	private Expression<?>[] expressions;
	private ParseResult parseResult;


	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		gameEffect = new GameEffect(PATTERNS.getInfo(matchedPattern).effect());
		getData = PATTERNS.getInfo(matchedPattern).getData();
		this.expressions = expressions;
		this.parseResult = parseResult;
		return true;
	}

	@Override
	protected GameEffect @Nullable [] get(Event event) {
		return setData(gameEffect, getData.getData(event, expressions, parseResult));
	}

	private GameEffect @Nullable [] setData(GameEffect gameEffect, Object data){
		if (data == null)
			return new GameEffect[0]; // invalid data, must return nothing.
		boolean success = gameEffect.setData(data);
		if (!success)
			return new GameEffect[0]; // invalid data
		return new GameEffect[]{gameEffect};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends GameEffect> getReturnType() {
		return GameEffect.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		// TODO: handle properly
		return "game effect " + gameEffect.getEffect().name();
	}

	/**
	 * A helper class to store the effect and its pattern.
	 * 
	 * @param effect The effect
	 * @param pattern The pattern
	 * @param getData The function to get the data from the event
	 */
	private record EffectPattern(Effect effect, String pattern, GetData<?> getData) {}

	/**
	 * A functional interface to get the data from the event.
	 *
	 * @param <T> The type of the data
	 */
	@FunctionalInterface
	interface GetData<T> {
		/**
		 * Get the data from the event.
		 *
		 * @param event The event to evaluate with
		 * @param expressions Any expressions that are used in the pattern
		 * @param parseResult The parse result from parsing
		 * @return The data to use for the effect
		 */
		T getData(Event event, Expression<?>[] expressions, ParseResult parseResult);

		//
		// Helper functions for common data types
		//

		private static @Nullable Material getMaterialData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
			Object input = expressions[0].getSingle(event);
			if (!(input instanceof ItemType itemType))
				return null;
			return itemType.getMaterial();
		}

		private static @Nullable BlockFace getBlockFaceData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
			Object input = expressions[0].getSingle(event);
			if (!(input instanceof Direction direction))
				return null;
			return Direction.toNearestBlockFace(direction.getDirection());
		}

		private static @Nullable Color getColorData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
			Object input = expressions[0].getSingle(event);
			if (!(input instanceof ch.njol.skript.util.Color color))
				return null;
			return color.asBukkitColor();
		}

		private static boolean isOminous(Event event, Expression<?>[] expressions, @NotNull ParseResult parseResult) {
			return parseResult.hasTag("ominous");
		}

		private static int defaultTo10Particles(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
			Object input = expressions[0].getSingle(event);
			if (!(input instanceof Number number))
				return 10;
			return number.intValue();
		}

		private static int defaultTo1Player(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
			Object input = expressions[0].getSingle(event);
			if (!(input instanceof Number number))
				return 1;
			return number.intValue();
		}

	}
	
}
