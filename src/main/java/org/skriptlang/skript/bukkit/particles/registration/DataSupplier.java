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

	static @Nullable Material getMaterialData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof ItemType itemType))
			return null;
		return itemType.getMaterial();
	}

	static @Nullable BlockFace getBlockFaceData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Direction direction))
			return null;
		return Direction.toNearestBlockFace(direction.getDirection());
	}

	static @Nullable BlockData getBlockData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (input instanceof ItemType itemType)
			return itemType.getMaterial().createBlockData();
		if (input instanceof BlockData blockData)
			return blockData;
		return null;
	}

	static @Nullable Color getColorData(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof ch.njol.skript.util.Color color))
			return null;
		return color.asBukkitColor();
	}

	static boolean isOminous(Event event, Expression<?>[] expressions, @NotNull ParseResult parseResult) {
		return parseResult.hasTag("ominous");
	}

	static int defaultTo10Particles(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Number number))
			return 10;
		return number.intValue();
	}

	static int defaultTo1Player(Event event, Expression<?> @NotNull [] expressions, ParseResult parseResult) {
		Object input = expressions[0].getSingle(event);
		if (!(input instanceof Number number))
			return 1;
		return number.intValue();
	}

}
