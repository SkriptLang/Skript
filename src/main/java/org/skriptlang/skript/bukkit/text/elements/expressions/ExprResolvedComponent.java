package org.skriptlang.skript.bukkit.text.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Name("Resolve Text Component")
@Description("""
	Resolves a component with the context of a command sender or other entity.
	Certain kinds of components, such as NBT components, Score components, and Selector components, are required to be resolved to display a proper output.
	Note that the option to bypass required permissions only applies for players.
	""")
@Example("""
	send formatted "Hello <selector:@e[limit=5]>, I'm <selector:@s>!" resolved for player
	""")
@Since("INSERT VERSION")
public class ExprResolvedComponent extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprResolvedComponent.class, Component.class)
			.supplier(ExprResolvedComponent::new)
			.addPattern("%textcomponents% resolved for %commandsender% [bypass:(bypassing|ignoring) required permissions]")
			.build());
	}

	private Expression<Component> components;
	private Expression<CommandSender> contextProvider;
	private boolean bypassPermissions;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		this.components = (Expression<Component>) expressions[0];
		//noinspection unchecked
		this.contextProvider = (Expression<CommandSender>) expressions[1];
		this.bypassPermissions = parseResult.hasTag("bypass");
		return true;
	}

	@Override
	protected Component @Nullable [] get(Event event) {
		CommandSender contextProvider = this.contextProvider.getSingle(event);
		if (contextProvider == null) {
			return new Component[0];
		}
		Entity scoreboardSubject = contextProvider instanceof Entity entity ? entity : null;
		// If the context provider is not a player, say a Pig, don't require it to have permissions
		boolean bypassPermissions = this.bypassPermissions || !(contextProvider instanceof OfflinePlayer);
		List<Component> components = new ArrayList<>();
		for (Component component : this.components.getArray(event)) {
			try {
				components.add(PaperComponents.resolveWithContext(component, contextProvider, scoreboardSubject, bypassPermissions));
			} catch (IOException | RuntimeException ignored) {
				// IOException is thrown for lack of permission
				// RuntimeException is thrown for something like an invalid selector
				components.add(component);
			}
		}
		return components.toArray(Component[]::new);
	}

	@Override
	public boolean isSingle() {
		return components.isSingle();
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(components, "resolved for", contextProvider)
			.appendIf(bypassPermissions, "bypassing required permissions")
			.toString();
	}

}
