package ci.polyevent;

import org.bukkit.plugin.java.JavaPlugin;

public final class PolyEvent extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("events").setExecutor(new EventsCommand());
        getCommand("events").setTabCompleter(new EventsCompleter());
    }
}
