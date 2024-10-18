package org.skriptlang.skript.lang.errors;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.logging.Level;

/**
 * A RuntimeErrorProducer can throw runtime errors in a standardized and controlled manner.
 * Only {@link SyntaxElement}s are intended to implement this interface.
 */
public interface RuntimeErrorProducer {

	// todo: try/catch

	/**
	 * Returns the {@link Node} that this is a part of. Used for accessing the line contents via {@link Node#getKey()}
	 * and the line number via {@link Node#getLine()}.
	 * <br>
	 * A standard implementation is to store the Node during
	 * {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)}
	 * via {@link ParserInstance#getNode()}.
	 * @return The node that produced a runtime error.
	 */
	Node getNode();

	/**
	 * Gets the text that should be underlined within the line contents. This should match the text the user wrote that
	 * was parsed as the syntax that threw the runtime issue. For example, if the skull expression in
	 * {@code give skull of player to all players} throws a runtime error, this method should return
	 * {@code "skull of player"}
	 * <br>
	 * An example implementation for {@link Expression}s is to store {@link SkriptParser.ParseResult#expr} during
	 * {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} and return that.
	 * <br>
	 * For other syntax types, this may vary. Effects, for example, may underline the whole line.
	 *
	 * @return The text to underline in the line that produced a runtime error.
	 */
	String toUnderline();

	/**
	 * Dispatches a runtime error with the given text.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 *
	 * @param message The text to display as the error message.
	 */
	default void error(String message) {
		String formatted = toFormattedString(Level.SEVERE, message, getNode(), toUnderline());
		SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), formatted);
		// todo: check if error should be suppressed, set last error field for users
		// todo: send notif to online players with permission. "Script x.sk produced a runtime error! Check console."
	}

	/**
	 * Dispatches a runtime warning with the given text.
	 * Metadata will be provided along with the message, including line number, the docs name of the producer,
	 * and the line content.
	 *
	 * @param message The text to display as the error message.
	 */
	default void warning(String message) {
		String formatted = toFormattedString(Level.WARNING, message, getNode(), toUnderline());
		SkriptLogger.sendFormatted(Bukkit.getConsoleSender(), formatted);
		// todo: check if error should be suppressed, set last error field for users
		// todo: send notif to online players with permission. "Script x.sk produced a runtime warning! Check console."
	}

	/**
	 * Generates a formatted string based on the provided log level, message, node information, and text to underline.
	 *
	 * @param level The severity level of the log message.
	 * @param message The message to be included in the log.
	 * @param node The node associated with this log message.
	 * @param toUnderline The text in the node's line content that should be underlined in the log message.
	 * @return A formatted string that includes metadata such as the script filename, error message,
	 * 			and the specific line that caused the error.
	 */
	default String toFormattedString(Level level, String message, Node node, String toUnderline) {
		// Replace configured messages chat styles without user variables
		// todo: add lang entries, handle error vs warning
		String skriptInfo = replaceNewline(Utils.replaceEnglishChatStyles("<light red>The script '<gray>%s<light red>' encountered an error while executing the '<gray>%s<light red>' %s:\n"));
		String errorInfo = replaceNewline(Utils.replaceEnglishChatStyles("\t<light red>%s<reset>\n"));
		String lineInfo = replaceNewline(Utils.replaceEnglishChatStyles("\t<gold>Line %s<white>: <gray>%s\n"));

		if (node == null)
			return "handle null nodes";

		Config c = node.getConfig();
		String from = this.getClass().getAnnotation(Name.class).value().trim().replaceAll("\n", "");

		return
			String.format(skriptInfo, c.getFileName(), from, getSyntaxType()) +
			String.format(errorInfo, message.replaceAll("§", "&")) +
			String.format(lineInfo, node.getLine(), node.save().trim().replaceAll("§", "&").replaceAll(toUnderline, "§f§n" + toUnderline + "§7"));
	}

	@Contract(pure = true)
	private @NotNull String getSyntaxType() {
		if (this instanceof Effect) {
			return "effect";
		} else if (this instanceof Condition) {
			return "condition";
		} else if (this instanceof Expression<?>) {
			return "expression";
		} else if (this instanceof Section) {
			return "section";
		} else if (this instanceof Structure) {
			return "structure";
		}
		return "syntax";
	}

	@Contract(pure = true)
	private @NotNull String replaceNewline(@NotNull String s) {
		return s.replaceAll("\\\\n", "\n");
	}

}
