package org.skriptlang.skript.bukkit.entity.player.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Player List Name")
@Description("The name of a player in the player list in the tab menu.")
@Example("""
	on join:
		player has permission "name.red"
		set the player's tab list name to "<red>%player's name%"
	""")
@Since("Before 2.1")
@Keywords({"tablist", "tab list"})
public class ExprPlayerListName extends SimplePropertyExpression<Player, Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPlayerListName.class, Component.class,
			"(player|tab)[ ]list name[s]", "players", false)
				.supplier(ExprPlayerListName::new)
				.build());
	}

	@Override
	public Component convert(Player player) {
		return player.playerListName();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Component name = delta == null ? (mode == ChangeMode.RESET ? null : Component.empty()) : (Component) delta[0];
		for (Player player : getExpr().getArray(event)) {
			player.playerListName(name);
		}
	}

	@Override
	public Class<Component> getReturnType() {
		return Component.class;
	}

	@Override
	protected String getPropertyName() {
		return "tablist name";
	}

}
