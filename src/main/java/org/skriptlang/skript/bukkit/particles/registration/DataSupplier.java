package org.skriptlang.skript.bukkit.particles.registration;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface for supplying data to effects from parsed expressions.
 * Effectively an {@link Expression} in disguise. Accepts the parsed context of expressions and parse result,
 * spits out the required data value.
 * @param <D> the data type to supply
 */
@FunctionalInterface
public interface DataSupplier<D> {

	/**
	 * Supplies data from the parsed expressions from a pattern.
	 *
	 * @param event       The event to evaluate with
	 * @param expressions Any expressions that are used in the pattern
	 * @param parseResult The parse result from parsing
	 * @return The data to use for the effect, or null if the required data could not be obtained
	 */
	@Nullable D getData(@Nullable Event event, Expression<?>[] expressions, ParseResult parseResult);

	//
	// Helper functions for common data types
	//

	/**
	 * Gets material data from an ItemType expression.
	 * @param event the event
	 * @param expressions Expected to contain a single ItemType expression
	 * @param parseResult the parse result (unused)
	 * @return the material, or null if the input was not an ItemType
	 */
	static @Nullable Material getMaterialData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof ItemType itemType))
			return null;
		return itemType.getMaterial();
	}

	/**
	 * Gets block face data from a Direction expression.
	 * @param event the event
	 * @param expressions Expected to contain a single Direction expression
	 * @param parseResult the parse result (unused)
	 * @return the block face, or null if the input was not a Direction
	 */
	static @Nullable BlockFace getBlockFaceData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Direction direction))
			return null;
		return Direction.toNearestBlockFace(direction.getDirection());
	}

	/**
	 * Gets cartesian block face data from a Direction expression. The white smoke effect only allows
	 * the six cardinal directions, so this function maps any direction to the nearest of those.
	 * @param event the event
	 * @param expressions Expected to contain a single Direction expression
	 * @param parseResult the parse result (unused)
	 * @return the cartesian block face, or null if the input was not a Direction
	 */
	static @Nullable BlockFace getCartesianBlockFaceData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Direction direction))
			return null;
		return Direction.toNearestCartesianBlockFace(direction.getDirection());
	}

	/**
	 * Gets block data from an ItemType or BlockData expression.
	 * @param event the event
	 * @param expressions Expected to contain a single ItemType or BlockData expression
	 * @param parseResult the parse result (unused)
	 * @return the block data, or null if the input was not an ItemType or BlockData
	 */
	static @Nullable BlockData getBlockData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (input instanceof ItemType itemType)
			return itemType.getMaterial().createBlockData();
		if (input instanceof BlockData blockData)
			return blockData;
		return null;
	}

	/**
	 * Gets color data from a skript Color expression.
	 * @param event the event
	 * @param expressions Expected to contain a single Color expression
	 * @param parseResult the parse result (unused)
	 * @return the color, or null if the input was not a Color
	 */
	static @Nullable Color getColorData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof ch.njol.skript.util.Color color))
			return null;
		return color.asBukkitColor();
	}

	/**
	 * Checks if the "ominous" tag was present in the parse result.
	 * @param event the event (unused)
	 * @param expressions the expressions (unused)
	 * @param parseResult the parse result
	 * @return true if the "ominous" tag was present, false otherwise
	 */
	static boolean isOminous(Event event, Expression<?>[] expressions, @NotNull ParseResult parseResult) {
		return parseResult.hasTag("ominous");
	}

	/**
	 * Gets a number from the first expression, defaulting to 10 if not present or invalid.
	 * @param event the event
	 * @param expressions Expected to contain a single Number expression, may be nullable
	 * @param parseResult the parse result (unused)
	 * @return the number, or 10 if not present or invalid
	 */
	static int getNumberDefault10(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		if (expressions[0] == null)
			return 10;
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Number number))
			return 10;
		return number.intValue();
	}

	/**
	 * Gets a number from the first expression, defaulting to 1 if not present or invalid.
	 * @param event the event
	 * @param expressions Expected to contain a single Number expression, may be nullable
	 * @param parseResult the parse result (unused)
	 * @return the number, or 1 if not present or invalid
	 */
	static int getNumberDefault1(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		if (expressions[0] == null)
			return 1;
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Number number))
			return 1;
		return number.intValue();
	}

}
