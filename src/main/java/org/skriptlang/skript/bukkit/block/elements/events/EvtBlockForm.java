package org.skriptlang.skript.bukkit.block.elements.events;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.util.BlockUtils;
import org.bukkit.event.block.BlockFormEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtBlockForm extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtBlockForm.class, "Block Form")
			.supplier(EvtBlockForm::new)
			.addEvent(BlockFormEvent.class)
			.addPattern("[block] form[ing] [[of] %-itemtypes/blockdatas%]")
			.addDescription("""
				Called when a block is created, but not by a player, e.g. snow forms due to snowfall,\s
				water freezes in cold biomes.
				Note that is not called when block spreads (mushroom growth, water physics etc.),\s
				as it has its own event (see <a href='#spread'>spread event</a>).
				""")
			.addExample("""
				on form of snow:
					broadcast "Great even more snow.."
				""")
			.addSince("1.0, 2.6 (BlockData support)")
			.build());
	}

	private @Nullable Literal<Object> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			types = (Literal<Object>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		return BlockUtils.checkEvent(event, types);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("block form")
			.appendIf(types != null, "of", types)
			.toString();
	}

}
