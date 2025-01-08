package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Title - Send")
@Description({
	"Sends a title/subtitle to the given player(s) with optional fadein/stay/fadeout times for Minecraft versions 1.11 and above. ",
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
@Since("2.3")
public class EffSendTitle extends Effect {
	
	private final static boolean TIME_SUPPORTED = Skript.methodExists(Player.class,"sendTitle", String.class, String.class, int.class, int.class, int.class);
	
	static {
		if (TIME_SUPPORTED)
			Skript.registerEffect(EffSendTitle.class,
					"send title %string% [with subtitle %-string%] [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]",
					"send subtitle %string% [to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]");
		else
			Skript.registerEffect(EffSendTitle.class,
					"send title %string% [with subtitle %-string%] [to %players%]",
					"send subtitle %string% [to %players%]");
	}

	private @Nullable Expression<String> title;
	private @Nullable Expression<String> subtitle;
	private Expression<Player> recipients;
	private @Nullable Expression<Timespan> fadeIn, stay, fadeOut;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		title = matchedPattern == 0 ? (Expression<String>) exprs[0] : null;
		subtitle = (Expression<String>) exprs[1 - matchedPattern];
		recipients = (Expression<Player>) exprs[2 - matchedPattern];
		if (TIME_SUPPORTED) {
			stay = (Expression<Timespan>) exprs[3 - matchedPattern];
			fadeIn = (Expression<Timespan>) exprs[4 - matchedPattern];
			fadeOut = (Expression<Timespan>) exprs[5 - matchedPattern];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		String title = this.title != null ? this.title.getSingle(event) : null;
		String subtitle = this.subtitle != null ? this.subtitle.getSingle(event) : null;
		
		if (TIME_SUPPORTED) {
			int fadeIn, stay, fadeOut;
			fadeIn = stay = fadeOut = -1;

			if (this.fadeIn != null) {
				Timespan t = this.fadeIn.getSingle(event);
				fadeIn = t != null ? (int) t.getAs(Timespan.TimePeriod.TICK) : -1;
			}

			if (this.stay != null) {
				Timespan t = this.stay.getSingle(event);
				stay = t != null ? (int) t.getAs(Timespan.TimePeriod.TICK) : -1;
			}

			if (this.fadeOut != null) {
				Timespan t = this.fadeOut.getSingle(event);
				fadeOut = t != null ? (int) t.getAs(Timespan.TimePeriod.TICK) : -1;
			}
			
			for (Player recipient : recipients.getArray(event))
				recipient.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		} else {
			for (Player recipient : recipients.getArray(event))
				recipient.sendTitle(title, subtitle);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug).append("send title");
		if (title != null)
			builder.append(title);
		if (subtitle != null)
			builder.append("with subtitle", subtitle);
		builder.append("to", recipients);
		if (TIME_SUPPORTED) {
			if (stay != null)
				builder.append("for", stay);
			if (fadeIn != null)
				builder.append("with fade in", fadeIn);
			if (fadeOut != null)
				builder.append("and fade out", fadeOut);
		}
		return builder.toString();
	}
	
}
