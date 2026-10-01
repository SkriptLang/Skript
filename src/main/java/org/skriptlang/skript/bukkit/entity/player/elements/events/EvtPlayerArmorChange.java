package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerArmorChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerArmorChange.class, "Player Armor Change")
			.supplier(EvtPlayerArmorChange::new)
			.addEvent(PlayerArmorChangeEvent.class)
			.addPatterns("[player] armo[u]r change[d]",
				"[player] %equipmentslot% change[d]")
			.addKeywords("armor", "armour")
			.addDescription("""
				Called when armor pieces of a player are changed.
				""")
			.addExample("""
				on armor change:
					broadcast the old armor item
				""")
			.addExample("""
				on helmet change:
					broadcast "hmm.."
				""")
			.addSince("2.5, 2.11 (equipment slots)")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerArmorChangeEvent.class, EquipmentSlot.class)
			.getter(PlayerArmorChangeEvent::getSlot)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerArmorChangeEvent.class, ItemStack.class)
			.getter(PlayerArmorChangeEvent::getOldItem)
			.time(Time.PAST)
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerArmorChangeEvent.class, ItemStack.class)
			.getter(PlayerArmorChangeEvent::getNewItem)
			.time(Time.FUTURE)
			.build());
	}

	private @Nullable EquipmentSlot slot = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args.length == 1) {
			Literal<EquipmentSlot> slotLiteral = (Literal<EquipmentSlot>) args[0];
			slot = slotLiteral.getSingle();
			if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND || (slot == EquipmentSlot.BODY)) {
				Skript.error("You can't detect an armor change event for " + Classes.toString(slot) + ".");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerArmorChangeEvent playerEvent = (PlayerArmorChangeEvent) event;
		if (slot == null)
			return true;
		return slot == playerEvent.getSlot();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.appendIf(slot == null, "armor change")
			.appendIf(slot != null, switch (slot) {
				case HAND -> "hand";
				case OFF_HAND -> "off hand";
				default -> "unknown";
			}, "changed")
			.toString();
	}

}
