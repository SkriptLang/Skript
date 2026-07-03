package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.function.Predicate;

public class EvtEntityBlockChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityBlockChange.class, "Entity Change Block")
			.supplier(EvtEntityBlockChange::new)
			.addEvent(EntityChangeBlockEvent.class)
			.addPatterns(ChangeEvent.patterns)
			.addDescription("""
				Called when an enderman places or picks up a block, a sheep eats grass,\s
				a silverfish boops into/out of a block or a falling block lands and turns into a block respectively.
				event-block represents the old block and event-blockdata represents the new replacement that will be applied to the block.
				""")
			.addExample("""
				on sheep eat:
				    kill event-entity
				    broadcast "A sheep stole some grass!"
				""")
			.addExample("""
				on falling block land:
				    event-entity is a falling dirt
				    cancel event
				""")
			.addSince("unknown, 2.5.2 (falling block), 2.8.0 (any entity support)")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityChangeBlockEvent.class, Block.class)
			.getter(EntityChangeBlockEvent::getBlock)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityChangeBlockEvent.class, Block.class)
			.getter(EntityChangeBlockEvent::getBlock)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityChangeBlockEvent.class, BlockData.class)
			.getter(EntityChangeBlockEvent::getBlockData)
			.build());

		eventValueRegistry.register(EventValue.builder(EntityChangeBlockEvent.class, BlockData.class)
			.getter(EntityChangeBlockEvent::getBlockData)
			.time(Time.FUTURE)
			.build());
	}

    // this is pretty messy but was unsure of a way to really clean it up?
	private enum ChangeEvent {

		ENDERMAN_PLACE("enderman place", event -> event.getEntity() instanceof Enderman && !ItemUtils.isAir(event.getTo())),
		ENDERMAN_PICKUP("enderman pickup", event -> event.getEntity() instanceof Enderman && ItemUtils.isAir(event.getTo())),

		SHEEP_EAT("sheep eat", event -> event.getEntity() instanceof Sheep),

		SILVERFISH_ENTER("silverfish enter", event -> event.getEntity() instanceof Silverfish && !ItemUtils.isAir(event.getTo())),
		SILVERFISH_EXIT("silverfish exit", event -> event.getEntity() instanceof Silverfish && ItemUtils.isAir(event.getTo())),

		FALLING_BLOCK_FALLING("falling block fall[ing]", event -> event.getEntity() instanceof FallingBlock && ItemUtils.isAir(event.getTo())),
		FALLING_BLOCK_LANDING("falling block land[ing]", event -> event.getEntity() instanceof FallingBlock && !ItemUtils.isAir(event.getTo())),

		// Covers all possible entity block changes.
		GENERIC("(entity|%*-entitydatas%) chang(e|ing) block[s]");

		@Nullable
		private final Predicate<EntityChangeBlockEvent> checker;
		private final String pattern;

		ChangeEvent(String pattern) {
			this(pattern, null);
		}

		ChangeEvent(String pattern, @Nullable Predicate<EntityChangeBlockEvent> checker) {
			this.pattern = pattern;
			this.checker = checker;
		}

		private static final String[] patterns;

		static {
			patterns = new String[ChangeEvent.values().length];
			for (int i = 0; i < patterns.length; i++)
				patterns[i] = values()[i].pattern;
		}
	}

	@Nullable
	private Literal<EntityData<?>> entityData;
	private ChangeEvent event;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		event = ChangeEvent.values()[matchedPattern];
		if (event == ChangeEvent.GENERIC)
			entityData = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityChangeBlockEvent entityEvent = (EntityChangeBlockEvent) event;
		if (entityData != null)
			return !entityData.check(event, data -> data.isInstance((entityEvent.getEntity())));
		if (this.event.checker == null)
			return true;
		return this.event.checker.test(entityEvent);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.event.name();
	}

}
