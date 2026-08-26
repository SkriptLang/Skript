package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.TreeSpecies;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtGrow extends SkriptEvent {

	/**
	 * Growth event restriction.
	 * ANY means any grow event goes.
	 * Structure/block restrict for structure/block grow events only.
	 * <p>
	 * STRUCTURE = 1, BLOCK = 2
	 */
	private static final int STRUCTURE = 1, BLOCK = 2;
	// Of (involves x in any way), From (x -> something), Into (something -> x), From_Into (x -> y)
	private static final int OF = 0, FROM = 1, INTO = 2, FROM_INTO = 3;

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtGrow.class, "Grow")
			.supplier(EvtGrow::new)
			.addEvents(CollectionUtils.array(StructureGrowEvent.class, BlockGrowEvent.class))
			.addPatterns(
				"grow[th] [of (1:%-treetypes%|2:%-itemtypes/blockdatas%)]",
				"grow[th] from %itemtypes/blockdatas%",
				"grow[th] [in]to (1:%-treetypes%|2:%-itemtypes/blockdatas%)",
				"grow[th] from %itemtypes/blockdatas% [in]to (1:%-treetypes%|2:%-itemtypes/blockdatas%)"
			)
			.addDescription("""
				Called when a tree, giant mushroom or plant grows to next stage.
				"of" matches any grow event, "from" matches only the old state, "into" matches only the new state,\
				and "from into" requires matching both the old and new states.
				Using "and" lists in this event is equivalent to using "or" lists.
				The event will trigger if any one of the elements is what grew.
				""")
			.addExample("""
				on grow:
				broadcast "Something grew!"
				""")
			.addExample("""
				on grow of tree:
				broadcast "A tree grew!"
				""")
			.addExample("""
				on grow of wheat[age=7]:
				broadcast "Wheat is fully grown!"
				""")
			.addExample("""
				on grow from a sapling:
				broadcast "A sapling started growing!"
				""")
			.addExample("""
				on grow into tree:
				broadcast "Something grew into a tree!"
				""")
			.addExample("""
				on grow from a sapling into tree:
				broadcast "A sapling grew into a tree!"
				""")
			.addExample("""
				on grow of wheat, carrots, or potatoes:
				broadcast "A crop grew!"
				""")
			.addExample("""
				on grow into tree, giant mushroom, cactus:
				broadcast "Something big grew!"
				""")
			.addExample("""
				on grow from wheat[age=0] to wheat[age=1] or wheat[age=2]:
				broadcast "Wheat advanced a growth stage!"
				""")
			.addSince("1.0")
			.addSince("2.2-dev20 (plants)")
			.addSince("2.8.0 (from, into, blockdata)")
			.build());

		// BlockFormEvent is covered in #EvtBlockGrow

		eventValueRegistry.register(EventValue.builder(StructureGrowEvent.class, Block.class)
			.getter(event -> event.getLocation().getBlock())
			.build());

		eventValueRegistry.register(EventValue.builder(StructureGrowEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
			.map(BlockState::getBlock)
			.toArray(Block[]::new))
			.build());

		eventValueRegistry.register(EventValue.builder(StructureGrowEvent.class, Block.class)
			.getter(event -> {
				for (BlockState blockState : event.getBlocks()) {
					if (blockState.getLocation().equals(event.getLocation()))
						return new BlockStateBlock(blockState);
				}
				return event.getLocation().getBlock();
			})
			.time(Time.FUTURE)
			.build());

		eventValueRegistry.register(EventValue.builder(StructureGrowEvent.class, Block[].class)
			.getter(event -> event.getBlocks().stream()
				.map(BlockStateBlock::new)
				.toArray(Block[]::new))
			.time(Time.FUTURE)
			.build());
	}

	private @Nullable Literal<Object> toTypes;

	private @Nullable Literal<Object> fromTypes;

	// Restriction on the type of grow event, ANY, STRUCTURE or BLOCK
	private int eventRestriction;

	// Restriction on the type of action, OF, FROM, INTO, or FROM_INTO
	private int actionRestriction;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		eventRestriction = parseResult.mark; // ANY, STRUCTURE or BLOCK
		actionRestriction = matchedPattern; // OF, FROM, INTO, or FROM_INTO

		switch (actionRestriction) {
			case OF -> {
				if (eventRestriction == STRUCTURE) {
					fromTypes = (Literal<Object>) args[0];
				} else if (eventRestriction == BLOCK) {
					fromTypes = (Literal<Object>) args[1];
				}
			}
			case FROM -> fromTypes = (Literal<Object>) args[0];
			case INTO -> {
				if (eventRestriction == STRUCTURE) {
					toTypes = (Literal<Object>) args[0];
				} else if (eventRestriction == BLOCK) {
					toTypes = (Literal<Object>) args[1];
				}
			}
			case FROM_INTO -> {
				fromTypes = (Literal<Object>) args[0];
				if (eventRestriction == STRUCTURE) {
					toTypes = (Literal<Object>) args[1];
				} else if (eventRestriction == BLOCK) {
					toTypes = (Literal<Object>) args[2];
				}
			}
			default -> {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		// Exit early if we need fromTypes, but don't have it
		if (fromTypes == null && actionRestriction != INTO)
			// We want true for "on grow:", false for anything else
			// So check against "OF", which is the first pattern; the one that allows "on grow:"
			return actionRestriction == OF;

		// Can exit early if we're checking against a structure, but the event isn't a structure grow event
		// Can also exit early if we're checking against a block, but the event isn't a block grow event AND we're not checking for OF
		// With OF, we can have `on grow of sapling` or `big mushroom` be a StructureGrowEvent
		if (eventRestriction == STRUCTURE && !(event instanceof StructureGrowEvent)) {
			return false;
		} else if (eventRestriction == BLOCK && !(event instanceof BlockGrowEvent) && actionRestriction != OF) {
			return false;
		}

		return switch (actionRestriction) {
			case OF -> checkFrom(event, fromTypes) || checkTo(event, fromTypes);
			case FROM -> checkFrom(event, fromTypes);
			case INTO -> checkTo(event, toTypes);
			case FROM_INTO -> checkFrom(event, fromTypes) && checkTo(event, toTypes);
			default -> false;
		};
	}

	private static boolean checkFrom(Event event, Literal<Object> types) {
		// treat and lists as or lists
		if (types.getAnd() && types instanceof LiteralList<Object> list)
			list.invertAnd();

		if (event instanceof StructureGrowEvent structureEvent) {
			Material sapling = ItemUtils.getTreeSapling(structureEvent.getSpecies());
			return types.check(event, type -> {
				if (type instanceof ItemType itemType) {
					return itemType.isOfType(sapling);
				} else if (type instanceof BlockData blockData) {
					return blockData.getMaterial() == sapling;
				}
				return false;
			});
		} else if (event instanceof BlockGrowEvent blockEvent) {
			BlockState oldState = blockEvent.getBlock().getState();
			return types.check(event, type -> {
				if (type instanceof ItemType itemType) {
					return itemType.isOfType(oldState.getBlockData());
				} else if (type instanceof BlockData blockData) {
					return blockData.matches(oldState.getBlockData());
				}
				return false;
			});
		}
		return false;
	}

	private static boolean checkTo(Event event, Literal<Object> types) {
		// treat and lists as or lists
		if (types.getAnd() && types instanceof LiteralList<Object> list)
			list.invertAnd();

		if (event instanceof StructureGrowEvent structureEvent) {
			TreeType species = structureEvent.getSpecies();
			return types.check(event, type -> {
				if (type instanceof TreeSpecies treeSpecies) {
					return treeSpecies.is(species);
				}
				return false;
			});
		} else if (event instanceof BlockGrowEvent blockEvent) {
			BlockState newState = blockEvent.getNewState();
			return types.check(event, type -> {
				if (type instanceof ItemType itemType) {
					return itemType.isOfType(newState.getBlockData());
				} else if (type instanceof BlockData blockData) {
					return blockData.matches(newState.getBlockData());
				}
				return false;
			});
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("grow")
			.appendIf(actionRestriction == FROM || actionRestriction == FROM_INTO, "from", fromTypes)
			.appendIf(actionRestriction == INTO || actionRestriction == FROM_INTO, "into", toTypes)
			.appendIf(actionRestriction == OF, "of", fromTypes)
			.toString();
	}

}
