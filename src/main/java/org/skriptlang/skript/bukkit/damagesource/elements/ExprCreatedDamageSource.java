package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.damagesource.elements.ExprSecDamageSource.DamageSourceSectionEvent;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;

@Name("Created Damage Source")
@Description("Get the created damage source being created/modified in a 'custom damage source' section.")
@Example("""
	set {_source} to a custom damage source:
		set the damage type of the created damage source to magic
	""")
@Since("2.12")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprCreatedDamageSource extends EventValueExpression<DamageSource> implements EventRestrictedSyntax {

	public static DefaultSyntaxInfos.Expression<ExprCreatedDamageSource, DamageSource> info() {
		return DefaultSyntaxInfos.Expression.builder(ExprCreatedDamageSource.class, DamageSource.class)
				.supplier(ExprCreatedDamageSource::new)
				.addPatterns("created damage source")
				.build();
	}

	public ExprCreatedDamageSource() {
		super(DamageSource.class);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(DamageSourceSectionEvent.class);
	}

}
