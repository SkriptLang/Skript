package ch.njol.skript.command;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.BooleanDialogInput;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickCallback.Options;
import org.bukkit.entity.Player;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.njol.skript.command.EffectCommandUtils.finalizeEffectCommand;

public class DialogTerminal {

	private static final Map<Player, TerminalData> SAVED_DATA = new HashMap<>();
	private static final String CODE_INPUT_KEY = "Skript_DialogEffectCommand_Code";
	private static final String SAVE_INPUT_KEY = "Skript_DialogEffectCommand_Save";

	/**
	 * Ensures the {@code player} can use effect commands before opening the terminal.
	 * @param player The {@code player} to open the terminal for.
	 * @return {@code true} if the player can use the terminal, otherwise {@code} false.
	 */
	public static boolean attemptTerminal(Player player) {
		if (!EffectCommandUtils.canUse(player))
			return false;
		openTerminal(player);
		return true;
	}

	/**
	 * Opens a {@link Dialog} to {@code player} for typing out multilined code.
	 * @param player The {@link Player} to open the {@link Dialog} to.
	 */
	private static void openTerminal(Player player) {
		TextComponentParser parser = TextComponentParser.instance();

		TerminalData data = SAVED_DATA.computeIfAbsent(player, p -> new TerminalData());

		TextDialogInput codeInput = DialogInput.text(
			CODE_INPUT_KEY,
			500,
			parser.parse("Code"),
			true,
			data.emptyCode(),
			5000,
			MultilineOptions.create(255, 200)
		);
		BooleanDialogInput saveInput = DialogInput.bool(
			SAVE_INPUT_KEY,
			parser.parse("Save Code on Execution/Close"),
			data.doesSave(),
			"True",
			"False"
		);
		DialogBase base = DialogBase.builder(parser.parse("Terminal"))
			.inputs(List.of(codeInput, saveInput))
			.build();
		ActionButton executeButton = ActionButton.builder(parser.parse("<green>Execute"))
			.action(DialogAction.customClick((response, audience) -> {
				String code = response.getText(CODE_INPUT_KEY);
				Boolean toSave = response.getBoolean(SAVE_INPUT_KEY);
				if (toSave != null) {
					data.setSave(toSave);
					if (toSave)
						data.setCode(code);
				}
				finalizeEffectCommand(player, code, true);
			}, Options.builder().build()))
			.build();
		ActionButton cancelButton = ActionButton.builder(parser.parse("<red>Cancel"))
			.action(DialogAction.customClick((response, audience) -> {
					String code = response.getText(CODE_INPUT_KEY);
					Boolean toSave = response.getBoolean(SAVE_INPUT_KEY);
					if (toSave != null) {
						data.setSave(toSave);
						if (toSave)
							data.setCode(code);
					}
				},
				ClickCallback.Options.builder().build()))
			.build();

		Dialog dialog = Dialog.create(factory -> factory.empty()
			.base(base)
			.type(DialogType.confirmation(executeButton, cancelButton)));

		player.showDialog(dialog);
	}

	private static class TerminalData {

		private String code = "";
		private boolean save = true;

		TerminalData() {}

		public String emptyCode() {
			String code = this.code;
			this.code = "";
			return code;
		}

		public boolean doesSave() {
			return save;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public void setSave(boolean save) {
			this.save = save;
		}

	}

}
