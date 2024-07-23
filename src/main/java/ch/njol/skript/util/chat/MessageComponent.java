/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util.chat;

import java.lang.reflect.Type;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.md_5.bungee.api.ChatColor;

/**
 * Component for chat messages. This can be serialized with GSON and then
 * sent to client.
 */
public class MessageComponent {
	
	public String text = "";
	
	/**
	 * Tells that this component is supposed to reset styling.
	 */
	public boolean reset = false;
	
	/**
	 * Makes text <b>bold</b>.
	 */
	public boolean bold = false;
	
	/**
	 * Makes text <i>italic</i>.
	 */
	public boolean italic = false;
	
	/**
	 * Makes texxt <u>underlined</u>.
	 */
	public boolean underlined = false;
	
	/**
	 * Makes text <s>strikethrough</s>
	 */
	public boolean strikethrough = false;
	
	/**
	 * Makes text obfuscated, i.e. each tick the client will scramble
	 * all letters with random ones.
	 */
	public boolean obfuscated = false;
	
	/**
	 * Color of this text. Defaults to reseting it.
	 */
	public @Nullable ChatColor color;
	
	/**
	 * Value of this, if present, will appended on what player is currently
	 * writing to chat.
	 */
	@Nullable
	public String insertion;
	
	@Nullable
	public ClickEvent clickEvent;
	
	/**
	 * Font of the component. 1.16+. Null for BaseComponents means default font.
	 */
	@Nullable
	public String font;

	@Nullable
	public String translation;

	@Nullable
	public String keybind;

	public static class ClickEvent {
		public ClickEvent(ClickEvent.Action action, String value) {
			this.action = action;
			this.value = value;
		}
		
		public enum Action  {
			open_url,
			
			run_command,
			
			suggest_command,
			
			change_page,

			copy_to_clipboard;
			
			public final String spigotName;
			
			@SuppressWarnings("null")
			Action() {
				spigotName = this.name().toUpperCase(Locale.ENGLISH);
			}
		}
		
		public ClickEvent.Action action;
		
		public String value;
	}
	
	public static class HoverEvent {
		public HoverEvent(HoverEvent.Action action, String value) {
			this.action = action;
			this.value = value;
		}
		
		public static enum Action {
			
			show_text,
			
			show_item,
			
			show_entity,
			
			show_achievement;
			
			public final String spigotName;
			
			@SuppressWarnings("null")
			Action() {
				spigotName = this.name().toUpperCase(Locale.ENGLISH);
			}
		}
		
		public HoverEvent.Action action;
		
		public String value;
	}
	
	@Nullable
	public HoverEvent hoverEvent;
	
	public static class BooleanSerializer implements JsonSerializer<Boolean> {
		@Override
		public @Nullable JsonElement serialize(@Nullable Boolean src, @Nullable Type typeOfSrc, @Nullable JsonSerializationContext context) {
			return src ? new JsonPrimitive(true) : null;
		}
	}

	public MessageComponent copy() {
		MessageComponent messageComponent = new MessageComponent();
		messageComponent.text = this.text;
		messageComponent.reset = this.reset;
		messageComponent.bold = this.bold;
		messageComponent.italic = this.italic;
		messageComponent.underlined = this.underlined;
		messageComponent.strikethrough = this.strikethrough;
		messageComponent.obfuscated = this.obfuscated;
		messageComponent.color = this.color;
		messageComponent.insertion = this.insertion;
		messageComponent.clickEvent = this.clickEvent;
		messageComponent.font = this.font;
		messageComponent.hoverEvent = this.hoverEvent;
		messageComponent.translation = translation;
		messageComponent.keybind = keybind;
		return messageComponent;
	}

}
