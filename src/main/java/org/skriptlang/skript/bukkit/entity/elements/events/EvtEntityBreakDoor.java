package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityBreakDoor extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityBreakDoor.class, "Entity Break Door")
			.supplier(EvtEntityBreakDoor::new)
			.addEvent(EntityBreakDoorEvent.class)
			.addPatterns("[%-entitydatas%] break[ing] [a] [wood[en]] door")
			.addDescription("""
				Called when an entity (usually a zombie, husk, zombie pigman or zombie villager) is breaking a door.
				Can be cancelled to prevent the entity from breaking the door.
				""")
			.addExample("""
				on entity breaking a door:
					type of event-entity is zombie
					broadcast "A zombie is about to murder a villager!"
				""")
			.addExample("""
				on husk breaking a door:
					broadcast "Rest in peace villagers.."
				""")
			.addSince("1.0")
			.addSince("INSERT VERSION (entity types)")
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList<EntityData<?>> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityData == null)
			return true;

		EntityBreakDoorEvent entityEvent = (EntityBreakDoorEvent) event;
		return entityData.check(event, data -> data.isInstance((entityEvent.getEntity())));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(entityData != null ? entityData : "entity")
			.append("breaking a wooden door")
			.toString();
	}

}
