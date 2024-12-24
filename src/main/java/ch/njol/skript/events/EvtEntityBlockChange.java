package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EvtEntityBlockChange extends SkriptEvent {

	static {
		Skript.registerEvent("Enderman/Sheep/Silverfish/Falling Block", EvtEntityBlockChange.class, EntityChangeBlockEvent.class, ChangeEvent.patterns)
				.description(
						"Called when an enderman places or picks up a block, a sheep eats grass, " +
						"a silverfish boops into/out of a block or a falling block lands and turns into a block respectively.",
						"event-block represents the old block and event-blockdata represents the new replacement that'll be applied to the block."
				)
				.examples(
						"on sheep eat:",
							"\tkill event-entity",
							"\tbroadcast \"A sheep stole some grass!\"",
						"",
						"on falling block land:",
							"\tevent-entity is a falling dirt",
							"\tcancel event"
				)
				.since("<i>unknown</i>, 2.5.2 (falling block), 2.8.0 (any entity support)");
	}

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
		private final Checker<EntityChangeBlockEvent> checker;
		private final String pattern;

		ChangeEvent(String pattern) {
			this(pattern, null);
		}

		ChangeEvent(String pattern, @Nullable Checker<EntityChangeBlockEvent> checker) {
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
		return this.event.checker.check((EntityChangeBlockEvent) event);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.event.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
	}

}
