package ch.njol.skript.effects;

import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;


@Name("Title - Send")
@Description({
	"Sends a title/subtitle to the given player(s) with optional fadein/stay/fadeout times. ",
	"",
	"If you're sending only the subtitle, it will be shown only if there's a title displayed at the moment, otherwise it will " +
	"be sent with the next title. To show only the subtitle, use: <code>send title \" \" with subtitle \"yourtexthere\" to player</code>.",
	"",
	"Note: if no input is given for the times, it will keep the ones from the last title sent, " +
	"use the <a href='effects.html#EffResetTitle'>reset title</a> effect to restore the default values."
})
@Examples({
	"send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds",
	"send title \"Hi %player%\" to player",
	"send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players",
	"send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player for 5 seconds with fadein 1 second and fade out 1 second",
	"send subtitle \"Party!\" to all players"
})
@Since("2.3, INSERT VERSION (object support)")
public class EffSendTitle extends Effect {
	static {
		Skript.registerEffect(EffSendTitle.class,
			"send title %object% [with subtitle %-object%] [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]",
			"send subtitle %object% [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]");
	}

	@Nullable
	private Expression<?> title;
	@Nullable
	private Expression<?> subtitle;
	@SuppressWarnings("null")
	private Expression<Player> recipients;
	@Nullable
	private Expression<Timespan> fadeIn, stay, fadeOut;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (matchedPattern == 0) title = LiteralUtils.defendExpression(exprs[0]);
		subtitle = LiteralUtils.defendExpression(exprs[1 - matchedPattern]);
		recipients = (Expression<Player>) exprs[2 - matchedPattern];
		stay = (Expression<Timespan>) exprs[3 - matchedPattern];
		fadeIn = (Expression<Timespan>) exprs[4 - matchedPattern];
		fadeOut = (Expression<Timespan>) exprs[5 - matchedPattern];
		if (title != null && subtitle != null) {
			return LiteralUtils.canInitSafely(title, subtitle);
		} else if (title != null) {
			return LiteralUtils.canInitSafely(title);
		} else {
			return LiteralUtils.canInitSafely(subtitle);
		}
	}

	@SuppressWarnings("null")
	@Override
	protected void execute(final Event event) {
		String title = this.title != null ? toString(this.title.getSingle(event)) : "";
		String subtitle = this.subtitle != null ? toString(this.subtitle.getSingle(event)) : null;
		int fadeIn, stay, fadeOut;

		fadeIn = getTicks(this.fadeIn, event);
		stay = getTicks(this.stay, event);
		fadeOut = getTicks(this.fadeOut, event);

		for (Player p : recipients.getArray(event)) {
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		}
	}

	private String toString(Object object) {
		return Classes.toString(object);
	}

	private int getTicks(@Nullable Expression<Timespan> timespan, Event event) {
		Timespan t = timespan != null ? timespan.getSingle(event) : null;
		return t != null ? (int) t.getAs(Timespan.TimePeriod.TICK) : -1;
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (title != null) {
			builder.append("send title");
			builder.append(title);
			if (subtitle != null) {
				builder.append("with subtitle");
			}
		} else {
			builder.append("send subtitle");
		}
		if (subtitle != null)
			builder.append(subtitle);
		if (recipients != null)
			builder.append("to", recipients);
		if (stay != null)
			builder.append("for", stay);
		if (fadeIn != null)
			builder.append("with fade in", fadeIn);
		if (fadeOut != null)
			builder.append("with fade out", fadeOut);
		return builder.toString();
	}
}
