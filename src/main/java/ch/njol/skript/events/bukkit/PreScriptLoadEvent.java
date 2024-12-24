package ch.njol.skript.events.bukkit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ch.njol.skript.config.Config;
import ch.njol.util.Validate;
import ch.njol.skript.ScriptLoader;

/**
 * This event has no guarantee of being on the main thread.
 * Please do not use bukkit api before checking {@link Bukkit#isPrimaryThread()}
 * @deprecated Use {@link ScriptLoader.ScriptPreInitEvent}.
 */
@Deprecated
public class PreScriptLoadEvent extends Event {

    private final List<Config> scripts;

    public PreScriptLoadEvent(List<Config> scripts) {
        super(!Bukkit.isPrimaryThread());
        Validate.notNull(scripts);
        this.scripts = scripts;
    }

    private static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public List<Config> getScripts() {
    	return scripts;
	}

}
