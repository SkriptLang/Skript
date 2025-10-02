package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource.DamageSourceSectionEvent;

@Name("Created Damage Source")
@Description("Get the created damage source being created/modified in a 'custom damage source' section.")
@Example("""
	set {_source} to a custom damage source:
		set the damage type of the created damage source to magic
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
@SuppressWarnings("UnstableApiUsage")
public class ExprCreatedDamageSource extends EventValueExpression<DamageSource> implements EventRestrictedSyntax {

	static {
		register(ExprCreatedDamageSource.class, DamageSource.class, "created damage source");
	}

	public ExprCreatedDamageSource() {
		super(DamageSource.class);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(DamageSourceSectionEvent.class);
	}

}
