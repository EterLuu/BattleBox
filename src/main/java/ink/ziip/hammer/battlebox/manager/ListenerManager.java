package ink.ziip.hammer.battlebox.manager;

import ink.ziip.hammer.battlebox.BattleBox;
import ink.ziip.hammer.battlebox.api.listener.BaseListener;
import ink.ziip.hammer.battlebox.api.manager.BaseManager;
import ink.ziip.hammer.battlebox.listener.OtherListener;
import org.bukkit.event.HandlerList;

public class ListenerManager extends BaseManager {

    private final BaseListener otherListener;

    public ListenerManager() {
        otherListener = new OtherListener();
    }

    @Override
    public void load() {

        otherListener.register();

    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(BattleBox.getInstance());
    }

    @Override
    public void reload() {

    }
}
