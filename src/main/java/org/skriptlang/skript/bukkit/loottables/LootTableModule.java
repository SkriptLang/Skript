package org.skriptlang.skript.bukkit.loottables;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.IOException;

public class LootTableModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.loottables", "elements");

		// --- CLASSES --- //

		Classes.registerClass(new ClassInfo<>(LootTable.class, "loottable")
			.user("loot ?tables?")
			.name("Loot Table")
			.description("Loot tables represent what items should be in naturally generated containers, + " +
				"what items should be dropped when killing a mob, or what items can be fished. ")
			.since("INSERT VERSION")
			.parser(new Parser<>() {
				@Override
				public @Nullable LootTable parse(String s, ParseContext context) {	
					return Bukkit.getLootTable(NamespacedUtils.parseNamespacedKey(s));
				}

				@Override
				public String toString(LootTable o, int flags) {
					return "loot table '" + o.getKey() + '\'';
				}

				@Override
				public String toVariableNameString(LootTable o) {
					return "loot table:" + o.getKey();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(LootContext.class, "lootcontext")
			.user("loot ?contexts?")
			.name("Loot Context")
			.description("Represents additional information a loot table can use to modify its generated loot.")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(LootContext.class))
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public @Nullable LootContext parse(String s, ParseContext context) {
					return null;
				}

				@Override
				public String toString(LootContext context, int flags) {
					return "loot context at " + Classes.toString(context.getLocation()) +
						((context.getLootedEntity() != null) ? (" with entity " + Classes.toString(context.getLootedEntity())) : "") +
						((context.getKiller() != null) ? " with killer " + Classes.toString(context.getKiller()) : "") +
						((context.getLuck() != 0) ? " with luck " + context.getLuck() : "");
				}

				@Override
				public String toVariableNameString(LootContext context) {
					return "loot context:" + context.hashCode();
				}
			})
		);

		Classes.registerClass(new ClassInfo<>(LootTables.class, "loottabletype")
			.user("loot ?table ?types?")
			.name("Loot Tables Types")
			.description("Represents all the loot tables Mojang offers.")
			.since("INSERT VERSION")
		);

		// --- CONVERTERS --- //

		// String - LootTable
		Converters.registerConverter(String.class, LootTable.class, key -> Bukkit.getLootTable(NamespacedUtils.parseNamespacedKey(key)));
	}

}
