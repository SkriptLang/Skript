package org.skriptlang.skript.bukkit.itemcomponents.blocking.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.BlockingWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.blocking.DamageReductionWrapper;

import java.util.ArrayList;
import java.util.List;

@Name("Blocking Component - Damage Reductions")
@Description("""
	The damage reductions applied to the item.
	Damage Reductions contain data that attribute to:
		- What damage types can be being blocked
		- The base amount of damage to block when blocking one of the damage types
		- The factor amount of damage to block when blocking one of the damage types
		- The angle at which the item can block when blocking one of the damage types
	NOTE: Blocking component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set {_reductions::*} to the damage reductions of {_item}")
@Example("""
	set {_reduction} to a custom damage reduction
	add {_reduction} to the damage reductions of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.5+")
@Since("INSERT VERSION")
public class ExprBlockCompReductions extends PropertyExpression<BlockingWrapper, DamageReductionWrapper> implements BlockingExperimentalSyntax {

	static {
		registerDefault(ExprBlockCompReductions.class, DamageReductionWrapper.class, "damage reductions", "blockingcomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends BlockingWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected DamageReductionWrapper[] get(Event event, BlockingWrapper[] source) {
		List<DamageReductionWrapper> reductionWrappers = new ArrayList<>();
		for (BlockingWrapper wrapper : source)
			reductionWrappers.addAll(wrapper.getDamageReductions());
		return reductionWrappers.toArray(DamageReductionWrapper[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(DamageReductionWrapper[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		List<DamageReduction> provided = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof DamageReductionWrapper wrapper)
					provided.add(wrapper.getDamageReduction());
			}
		}

		getExpr().stream(event).forEach(wrapper ->
			wrapper.editBuilder(builder -> {
				switch (mode) {
					case SET, DELETE -> builder.damageReductions(provided);
					case ADD -> builder.addDamageReductions(provided);
					case REMOVE -> builder.removeReductions(provided);
				}
			}));
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<DamageReductionWrapper> getReturnType() {
		return DamageReductionWrapper.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the damage reductions of", getExpr())
			.toString();
	}

}
