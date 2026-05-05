package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.pdc.elements.SecEditContainer;
import org.skriptlang.skript.bukkit.pdc.elements.conditions.CondHasPersistentDataTag;
import org.skriptlang.skript.bukkit.pdc.elements.expressions.ExprAllPersistentDataKeys;
import org.skriptlang.skript.bukkit.pdc.elements.expressions.ExprPersistentData;

import java.io.IOException;

public class PDCModule extends HierarchicalAddonModule {

	public PDCModule(@Nullable AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(PersistentDataContainer.class, "persistentdatacontainer")
			.user("(?:persistent ?)data ?container|pdc")
			.name("Persistent Data Container")
			.parser(new Parser<>() {
				@Override
				public boolean canParse(ParseContext context) {
					return false;
				}

				@Override
				public String toString(PersistentDataContainer o, int flags) {
					try {
						// I don't see any good way to do this
						return new String(o.serializeToBytes());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String toVariableNameString(PersistentDataContainer o) {
					return toString(o, 0);
				}
			}));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondHasPersistentDataTag::register,
			ExprAllPersistentDataKeys::register,
			ExprPersistentData::register,
			SecEditContainer::register);
	}

	@Override
	public String name() {
		return "persistent data containers";
	}

}
