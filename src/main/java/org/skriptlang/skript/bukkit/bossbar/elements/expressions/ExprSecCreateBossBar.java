package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Color;
import ch.njol.skript.variables.Variables;
import ch.njol.skript.doc.Example;
import ch.njol.util.Kleenean;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

import static org.skriptlang.skript.bukkit.bossbar.BossBarUtils.nearest;

@Name("Create Boss Bar")
@Description("""
	Creates a new boss bar.
	Boss bars can be pink/blue/red/green/yellow/purple/white, and will pick the closest valid color to the one you provide.
	Boss bars can have viewers removed or added to them.
	Making the boss bar 'keyed' will add it to the persistent storage of the server and will be editable by commands and restored after restart.
	""")
@Example("""
	on join:
		set {_bar} to a boss bar:
			set color of event-bossbar to white
			set title of event-bossbar to "<green>Welcome %player%!"
			set progress of event-bossbar to 50%
			set style of event-bossbar to 6 notches
			make event-bossbar darken the sky
		add player to viewers of {_bar}
		wait 5 seconds
		remove player from viewers of {_bar}
	""")
@Example("""
	command /createkeyedbar:
	    set {_bar} to a red keyed bossbar with title "<red>My persistent red bar":
	        set progress of event-bossbar to 14%
	        set style of event-bossbar to 20 notches
	    loop all operators:
	        add loop-value to viewers of {_bar}
	""")
@Since("2.16")
public class ExprSecCreateBossBar extends SectionExpression<BossBar> {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprSecCreateBossBar.class, BossBar.class)
				.addPatterns("[a] [new] [%-color%] boss[ ]bar [(with title|titled) %-textcomponent%]",
					"[a] [new] keyed [%-color%] boss[ ]bar with (id|key) %string% [(with title|titled) %-textcomponent%]")
				.supplier(ExprSecCreateBossBar::new)
				.build()
		);

		eventValueRegistry.register(EventValue.builder(CreateBossBarEvent.class, BossBar.class)
			.getter(CreateBossBarEvent::getBossBar)
			.build());
	}

	private Trigger trigger = null;
	private Expression<String> key;
	private Expression<Color> color;
	private Expression<Component> title;
	private boolean isKeyed;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		color = (Expression<Color>) expressions[0];
		if (matchedPattern == 1) {
			isKeyed = true;
			key = (Expression<String>) expressions[1];
		} else {
			isKeyed = false;
		}
		title = (Expression<Component>) expressions[expressions.length - 1];
		if (node != null) {
			trigger = SectionUtils.loadLinkedCode("create bossbar", (beforeLoading, afterLoading)
				-> loadCode(node, "create bossbar", beforeLoading, afterLoading, CreateBossBarEvent.class));
			return trigger != null;
		}
		return true;
	}

	@Override
	protected BossBar @Nullable [] get(Event event) {
		BossBar bar;
		Color color = null;
		if (this.color != null)
		    color = this.color.getSingle(event);
		BarColor barColor;
		barColor = color != null && nearest(color) != null ? nearest(color) : BarColor.WHITE;
		if (barColor == null)
			return new BossBar[0];

		String legacyTitle = null;
		if (this.title != null) {
			Component title = this.title.getSingle(event);
			if (title == null)
				return new BossBar[0];
			legacyTitle = LegacyComponentSerializer.legacySection().serialize(title);
		}

		if (isKeyed) {
			String stringKey = this.key.getSingle(event);
			if (stringKey == null)
				return new BossBar[0];
			NamespacedKey key = NamespacedUtils.checkValidationAndSend(stringKey, this);
			if (key == null)
				return new BossBar[0];
			Bukkit.createBossBar(key, legacyTitle, barColor, BarStyle.SOLID);
			bar = Bukkit.getBossBar(key);
		} else {
			bar = Bukkit.createBossBar(legacyTitle, barColor, BarStyle.SOLID);
		}

		if (trigger == null)
			return new BossBar[] {bar};
		CreateBossBarEvent bossbarEvent = new CreateBossBarEvent(bar);
		Variables.withLocalVariables(event, bossbarEvent, () -> TriggerItem.walk(trigger, bossbarEvent));
		return new BossBar[] {bossbarEvent.getBossBar()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends BossBar> getReturnType() {
		if (key != null)
			return KeyedBossBar.class;
		return BossBar.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("a")
			.appendIf(key != null, "keyed")
			.appendIf(color != null, color)
			.append("boss bar")
			.appendIf(key != null, "with key", key)
			.appendIf(title != null, "with title", title)
			.toString();
	}

	private static class CreateBossBarEvent extends Event {
		private final BossBar bar;

		public CreateBossBarEvent(BossBar bar) {
			this.bar = bar;
		}

		public BossBar getBossBar() {
			return bar;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}

	}

}
