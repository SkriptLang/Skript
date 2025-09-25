package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("Equipment Drop Chance")
@Description("""
    Returns the drop chance for the specified equipment slot(s) in a spawner entry. \
    The drop chance is a float between 0 and 1, where 0 means the item will never drop \
    and 1 means it will always drop.

    Setting drop chances without an equipment loot table defined will have no effect.
    """)
@Example("""
	set {_entry} to the spawner entry of a zombie:
		set the spawner entry equipment to loot table "minecraft:equipment/trial_chamber"
		set the drop chances for helmet, legs and boots to 100%
		remove 50% from the drop chance for legs
		clear the drop chances for all equipment slots
	""")
@Since("INSERT VERSION")
public class ExprSpawnerEntryDropChances extends SimpleExpression<Float> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSpawnerEntryDropChances.class, Float.class)
			.supplier(ExprSpawnerEntryDropChances::new)
			.priority(SyntaxInfo.COMBINED)
			.addPatterns(
				"[the] drop chance[s] [of %spawnerentries%] for %equipmentslots%",
				"%spawnerentries%'[s] drop chance[s] for %equipmentslots%")
			.build()
		);
	}

	private Expression<SkriptSpawnerEntry> entries;
	private Expression<EquipmentSlot> slots;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entries = (Expression<SkriptSpawnerEntry>) exprs[0];
		//noinspection unchecked
		slots = (Expression<EquipmentSlot>) exprs[1];
		return true;
	}

	@Override
	protected Float @Nullable [] get(Event event) {
		SkriptSpawnerEntry[] entries = this.entries.getArray(event);
		EquipmentSlot[] slots = this.slots.getArray(event);

		List<Float> dropChances = new ArrayList<>(slots.length * entries.length);

		for (SkriptSpawnerEntry entry : entries) {
			Map<EquipmentSlot, Float> dropChanceMap = entry.getDropChances();
			for (EquipmentSlot slot : slots) {
				Float chance = dropChanceMap.get(slot);
				if (chance == null)
					continue;

				dropChances.add(chance);
			}
		}

		return dropChances.toArray(Float[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, DELETE -> CollectionUtils.array(Float.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		float chance = delta != null ? (float) delta[0] : 0;

		for (SkriptSpawnerEntry entry : this.entries.getArray(event)) {
			for (EquipmentSlot slot : this.slots.getArray(event)) {
				if (mode == ChangeMode.DELETE)
					entry.removeDropChance(slot);

				entry.setDropChance(slot, switch (mode) {
					case SET -> chance;
					case ADD -> entry.getDropChances().getOrDefault(slot, 0f) + chance;
					case REMOVE -> entry.getDropChances().getOrDefault(slot, 0f) - chance;
					default -> 0;
				});
			}
		}
	}

	@Override
	public boolean isSingle() {
		return entries.isSingle() && slots.isSingle();
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the drop chances of", entries, "for", slots);
		return builder.toString();
	}

}
