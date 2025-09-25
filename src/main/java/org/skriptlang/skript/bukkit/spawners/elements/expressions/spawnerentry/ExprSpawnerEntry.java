package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawner Entry")
@Description("""
	The spawner entry used in the spawner entry section.
	""")
@Examples("""
	the spawner entry
	""")
@Since("INSERT VERSION")
public class ExprSpawnerEntry extends EventValueExpression<SkriptSpawnerEntry> {

   public static void register(SyntaxRegistry registry) {
	   registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnerEntry.class, SkriptSpawnerEntry.class, "[the] spawner entry")
		   .supplier(ExprSpawnerEntry::new)
		   .build()
	   );
   }

    public ExprSpawnerEntry() {
        super(SkriptSpawnerEntry.class);
    }

    @Override
    public String toString() {
        return "the spawner entry";
    }

}
