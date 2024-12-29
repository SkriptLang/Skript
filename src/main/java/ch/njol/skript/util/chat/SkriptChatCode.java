package ch.njol.skript.util.chat;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.util.chat.MessageComponent.ClickEvent;
import ch.njol.skript.util.chat.MessageComponent.HoverEvent;

/**
 * Chat codes that come with Skript by default.
 */
public enum SkriptChatCode implements ChatCode {
	
	reset {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.reset = true;
		}
	},

	// Colors (Vanilla color code, Skript color code if different)
	
	black("black", '0'),
	dark_blue("dark_blue", '1'),
	dark_green("dark_green", '2'),
	dark_aqua("dark_aqua", "dark_cyan", '3'),
	dark_red("dark_red", '4'),
	dark_purple("dark_purple", '5'),
	gold("gold", "orange", '6'),
	gray("gray", "light_grey", '7'),
	dark_gray("dark_gray", "dark_grey", '8'),
	blue("blue", "light_cyan", '9'),
	green("green", "light_green", 'a'),
	aqua("aqua", "light_cyan", 'b'),
	red("red", "light_red", 'c'),
	light_purple("light_purple", 'd'),
	yellow("yellow", 'e'),
	white("white", 'f'),
	
	// Formatting
	
	bold {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.bold = true;
		}
	},
	
	italic {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.italic = true;
		}
	},
	
	underlined(null, "underline") {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.underlined = true;
		}
	},
	
	strikethrough {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.strikethrough = true;
		}
	},
	
	obfuscated(null, "magic") {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.obfuscated = true;
		}
	},
	
	// clickEvent
	
	open_url(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.open_url, param);
			component.clickEvent = e;
		}
	},
	
	run_command(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.run_command, param);
			component.clickEvent = e;
		}
	},
	
	suggest_command(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.suggest_command, param);
			component.clickEvent = e;
		}
	},
	
	change_page(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			ClickEvent e = new ClickEvent(ClickEvent.Action.change_page, param);
			component.clickEvent = e;
		}
	},
	
	copy_to_clipboard(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.clickEvent = new ClickEvent(ClickEvent.Action.copy_to_clipboard, param);
		}
	},

	// hoverEvent
	
	show_text(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			// TODO component based codes must be supported
			// Especially since 1.13 might break the old ones completely...
			HoverEvent e = new HoverEvent(HoverEvent.Action.show_text, param);
			component.hoverEvent = e;
		}
	},

	font(true) {
		@Override
		public void updateComponent(MessageComponent component, String param) {
			component.font = param;
		}
	},

	// Other

    insertion(true) {
	    @Override
        public void updateComponent(MessageComponent component, String param) { component.insertion = param; }
    },

    translate(true) {
	    @Override
        public void updateComponent(MessageComponent component, String param) {
			component.translation = param;
		}
    },

    keybind(true) {
	    @Override
        public void updateComponent(MessageComponent component, String param) {
			component.keybind = param;
		}
    };

	private boolean hasParam;
	
	@Nullable
	private String colorCode;
	
	@Nullable
	private String langName;
	
	private char colorChar;
	
	SkriptChatCode(@Nullable String colorCode, String langName, char colorChar) {
		this.colorCode = colorCode;
		this.langName = langName;
		this.hasParam = false;
		this.colorChar = colorChar;
	}
	
	SkriptChatCode(@Nullable String colorCode, String langName) {
		this.colorCode = colorCode;
		this.langName = langName;
		this.hasParam = false;
	}
	
	SkriptChatCode(String colorCode, char colorChar) {
		this.colorCode = colorCode;
		this.langName = colorCode;
		this.hasParam = false;
		this.colorChar = colorChar;
	}
	
	SkriptChatCode(boolean hasParam) {
		this.hasParam = hasParam;
		this.langName = this.name(); // Default to enum name
	}
	
	SkriptChatCode() {
		this(false);
	}
	
	@Override
	public boolean hasParam() {
		return hasParam;
	}
	
	@Override
	@Nullable
	public String getColorCode() {
		return colorCode;
	}
	
	@Override
	@Nullable
	public String getLangName() {
		return langName;
	}
	
	@Override
	public boolean isLocalized() {
		return true;
	}
	
	@Override
	public char getColorChar() {
		return colorChar;
	}
	
	@Override
	public void updateComponent(MessageComponent component, String param) {
		// Default: do nothing
	}
}
