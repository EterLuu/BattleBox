package ink.ziip.hammer.battlebox.api.listener;

import ink.ziip.hammer.battlebox.BattleBox;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, BattleBox.getInstance());
    }

    public void unRegister() {
        HandlerList.unregisterAll(this);
    }
}
