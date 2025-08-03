package ch.njol.skript.command.brigadier;

import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Event for retrieving command suggestions (tab completions).
 */
public class BrigadierSuggestionsEvent extends BrigadierCommandEvent {

	private final List<String> suggestions = new ArrayList<>();

	public BrigadierSuggestionsEvent(CommandContext<CommandSender> context) {
		super(context);
	}

	/**
	 * @return suggestions
	 */
	public String[] getSuggestions() {
		return suggestions.toArray(String[]::new);
	}

	/**
	 * @param suggestions new suggestions
	 */
	public void setSuggestions(List<String> suggestions) {
		this.suggestions.clear();
		if (suggestions != null)
			this.suggestions.addAll(suggestions);
	}

	/**
	 * @param suggestions new suggestions
	 */
	public void setSuggestions(String... suggestions) {
		setSuggestions(List.of(suggestions));
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
