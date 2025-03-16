package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletionException;

public class EvtPlayerArmorChange extends SkriptEvent {

	private static boolean BODY_SLOT_EXISTS;

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
			Skript.registerEvent("Armor Change", EvtPlayerArmorChange.class, PlayerArmorChangeEvent.class,
				"[player] armo[u]r change[d]",
					"[player] %equipmentslot% change[d]")
				.description("Called when armor pieces of a player are changed.")
				.requiredPlugins("Paper")
				.keywords("armor", "armour")
				.examples(
					"on armor change:",
						"\tbroadcast the old armor item",
					"on helmet change:"
				)
				.since("2.5, INSERT VERSION (equipment slots)");

			EquipmentSlot bodySlot = null;
			try {
				bodySlot = EquipmentSlot.valueOf("BODY");
			} catch (IllegalArgumentException | NoSuchFieldError | CompletionException ignored) {};
			BODY_SLOT_EXISTS = bodySlot != null;
		}
	}

	private @Nullable EquipmentSlot slot = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args.length == 1) {
			//noinspection unchecked
			Literal<EquipmentSlot> slotLiteral = (Literal<EquipmentSlot>) args[0];
			slot = slotLiteral.getSingle();
			if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND || (BODY_SLOT_EXISTS && slot == EquipmentSlot.BODY)) {
				Skript.error("You can't detect an armor change event for a '" + Classes.toString(slot) + "'.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerArmorChangeEvent changeEvent = (PlayerArmorChangeEvent) event;
		if (slot == null)
			return true;
		EquipmentSlot changedSlot = switch (changeEvent.getSlotType()) {
			case HEAD -> EquipmentSlot.HEAD;
			case CHEST -> EquipmentSlot.CHEST;
			case LEGS -> EquipmentSlot.LEGS;
			case FEET -> EquipmentSlot.FEET;
		};
        return slot == changedSlot;
    }

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (slot == null) {
			builder.append("armor change");
		} else {
			builder.append(switch (slot) {
				case HEAD -> "helmet";
				case CHEST -> "chestplate";
				case LEGS -> "legging";
				case FEET -> "boots";
				default -> "";
			});
			builder.append("changed");
		}
		return builder.toString();
	}

}
