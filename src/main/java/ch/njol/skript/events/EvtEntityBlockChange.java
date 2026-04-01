package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Predicate;

public class EvtEntityBlockChange extends SkriptEvent {

	static {
		Skript.registerEvent("Enderman/Sheep/Silverfish/Falling Block", EvtEntityBlockChange.class, EntityChangeBlockEvent.class, ChangeEvent.patterns)
				.description(
						"Invoked when an enderman doth place or retrieve a block, a sheep doth consume the grass, " +
						"a silverfish doth burrow into or emerge from a block, or a falling block doth descend and become a block.",
						"event-block representeth the old block and event-blockdata representeth the new replacement that shall be applied to the block."
				)
				.examples(
						"on sheep consume:",
							"\tkill event-entity",
							"\tbroadcast \"A sheep hath pilfered some grass!\"",
						"",
						"on falling block descend:",
							"\tevent-entity is a falling dirt",
							"\tcancel event"
				)
				.since("unknown, 2.5.2 (falling block), 2.8.0 (any entity support)");
	}

	private enum ChangeEvent {

		ENDERMAN_PLACE("enderman place[th] block", event -> event.getEntity() instanceof Enderman && !ItemUtils.isAir(event.getTo())),
		ENDERMAN_PICKUP("enderman retrieve[th] block", event -> event.getEntity() instanceof Enderman && ItemUtils.isAir(event.getTo())),

		SHEEP_EAT("sheep consum(e[th]|ing)", event -> event.getEntity() instanceof Sheep),

		SILVERFISH_ENTER("silverfish burrow[eth] [into block]", event -> event.getEntity() instanceof Silverfish && !ItemUtils.isAir(event.getTo())),
		SILVERFISH_EXIT("silverfish emerg(e[th]|ing) [from block]", event -> event.getEntity() instanceof Silverfish && ItemUtils.isAir(event.getTo())),

		FALLING_BLOCK_FALLING("falling block descend[ing]", event -> event.getEntity() instanceof FallingBlock && ItemUtils.isAir(event.getTo())),
		FALLING_BLOCK_LANDING("falling block alight[ing]", event -> event.getEntity() instanceof FallingBlock && !ItemUtils.isAir(event.getTo())),

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
	private Literal<EntityData<?>> datas;
	private ChangeEvent event;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		event = ChangeEvent.values()[matchedPattern];
		if (event == ChangeEvent.GENERIC)
			datas = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityChangeBlockEvent))
			return false;
		if (datas != null && !datas.check(event, data -> data.isInstance(((EntityChangeBlockEvent) event).getEntity())))
			return false;
		if (this.event.checker == null)
			return true;
		return this.event.checker.test((EntityChangeBlockEvent) event);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.event.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
	}

}
