package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Locale;

@Name("Picked Item/Block/Entity")
@Description("The item, block, or entity picked by a player using the pick block key (default middle mouse button).")
@Example("""
	on player pick item:
		send "You picked %the picked item%!" to the player
	""")
@Since("INSERT VERSION")
@RequiredPlugins("1.21.5+")
@Keywords({"pick", "picked", "picked item", "picked block", "picked entity"})
public class ExprPickedItem extends SimpleExpression<Object> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry, Origin origin) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPickedItem.class, Object.class)
			.supplier(ExprPickedItem::new)
			.origin(origin)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] picked (item|1:block|2:entity)")
			.build());
	}

	private PickType pickType;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pickType = PickType.values()[parseResult.mark];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerPickItemEvent.class);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		return switch (pickType) {
			case ITEM -> {
				if (event instanceof PlayerPickBlockEvent pickBlockEvent) {
					yield new ItemType[] {new ItemType(pickBlockEvent.getBlock())};
				} else if (event instanceof PlayerPickEntityEvent pickEntityEvent) {
					yield new ItemType[] {new ItemType(pickEntityEvent.getEntity().getPickItemStack())};
				} else {
					yield null;
				}
			}
			case BLOCK -> {
				if (event instanceof PlayerPickBlockEvent pickBlockEvent) {
					yield new Block[] {pickBlockEvent.getBlock()};
				} else {
					yield null;
				}
			}
			case ENTITY -> {
				if (event instanceof PlayerPickEntityEvent pickEntityEvent) {
					yield new Entity[] {pickEntityEvent.getEntity()};
				} else {
					yield null;
				}
			}
		};
	}

	@Override
	public Class<?> getReturnType() {
		return switch (pickType) {
			case ITEM -> ItemType.class;
			case BLOCK -> Block.class;
			case ENTITY -> Entity.class;
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the picked " + pickType.name().toLowerCase(Locale.ENGLISH);
	}

	private enum PickType {
		ITEM,
		BLOCK,
		ENTITY,
	}

}
