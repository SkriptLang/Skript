package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.damagesource.elements.expressions.ExprSecDamageSource.DamageSourceSectionEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Forged Damage Source")
@Description("Obtain the damage source being forged or amended within a 'custom damage source' section.")
@Example("""
    set {_source} to a custom damage source:
    	set the damage type of the forged damage source to magic
    """)
@Since("2.12")
public class ExprCreatedDamageSource extends EventValueExpression<DamageSource> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprCreatedDamageSource.class,
				DamageSource.class,
				"forged damage source"
			)
				.supplier(ExprCreatedDamageSource::new)
				.build()
		);
	}

	public ExprCreatedDamageSource() {
		super(DamageSource.class);
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(DamageSourceSectionEvent.class);
	}

}
