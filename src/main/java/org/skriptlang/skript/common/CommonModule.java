package org.skriptlang.skript.common;

import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.elements.expressions.ExprColorFromHexCode;
import org.skriptlang.skript.common.elements.expressions.ExprHexCode;
import org.skriptlang.skript.common.elements.expressions.ExprRecursiveSize;
import org.skriptlang.skript.common.elements.sections.ExprSecFunction;
import org.skriptlang.skript.common.properties.PropertiesModule;
import org.skriptlang.skript.common.types.QuaternionClassInfo;
import org.skriptlang.skript.common.types.QueueClassInfo;
import org.skriptlang.skript.common.types.ScriptClassInfo;

import java.util.List;

public class CommonModule extends HierarchicalAddonModule {

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
				new PropertiesModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new ScriptClassInfo());
		// joml type - for display entities
		Classes.registerClass(new QuaternionClassInfo());
		Classes.registerClass(new QueueClassInfo());
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
				ExprColorFromHexCode::register,
				ExprHexCode::register,
				ExprRecursiveSize::register,
				ExprSecFunction::register
		);
	}

	@Override
	public String name() {
		return "common";
	}

}
