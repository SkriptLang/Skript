package org.skriptlang.skript.bukkit.entity.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtEntityTransform extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtEntityTransform.class, "Entity Transform")
			.supplier(EvtEntityTransform::new)
			.addEvent(EntityTransformEvent.class)
			.addPatterns("[%*-entitydatas%] transform[ing] [due to %-transformreasons%]")
			.addDescription("""
				Called when an entity is about to be replaced by another entity.
				e.g. when a zombie gets cured and a villager spawns,\s
				an entity drowns in water like a zombie that turns to a drown,\s
				an entity that gets frozen in powder snow,\s
				a mooshroom that when sheared, spawns a new cow.
				""")
			.addExample("""
				on a zombie transforming due to curing:
					broadcast "Another one cured from this madness.."
				""")
			.addExample("""
				on mooshroom transforming:
					cancel event
					broadcast "forever a mooshroom!"
				""")
			.addSince("2.8.0")
			.build());

		eventValueRegistry.register(EventValue.builder(EntityTransformEvent.class, Entity[].class)
			.getter(event -> event.getTransformedEntities().toArray(Entity[]::new))
			.build());

		eventValueRegistry.register(EventValue.builder(EntityTransformEvent.class, TransformReason.class)
			.getter(EntityTransformEvent::getTransformReason)
			.build());
	}

	private @Nullable Literal<EntityData<?>> entityData;
	private @Nullable Literal<TransformReason> reasons;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[1] != null) {
			reasons = (Literal<TransformReason>) args[1];
			if (reasons.getAnd() && reasons instanceof LiteralList)
				((LiteralList<TransformReason>) reasons).invertAnd();
		}
		if (args[0] != null) {
			entityData = (Literal<EntityData<?>>) args[0];
			if (entityData.getAnd() && entityData instanceof LiteralList)
				((LiteralList<EntityData<?>>) entityData).invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityTransformEvent entityEvent = (EntityTransformEvent) event;
		if (reasons != null && !reasons.check(event, reason -> reason == entityEvent.getTransformReason()))
			return false;
		if (entityData != null && !entityData.check(event, data -> data.isInstance(entityEvent.getEntity())))
			return false;
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(entityData != null ? entityData : "entity")
			.append("transforming")
			.appendIf(reasons != null, "due to", reasons)
			.toString();
	}

}
