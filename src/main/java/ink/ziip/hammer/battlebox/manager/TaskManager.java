package ink.ziip.hammer.battlebox.manager;


import ink.ziip.hammer.battlebox.api.manager.BaseManager;
import ink.ziip.hammer.battlebox.task.AreaManagerTask;

public class TaskManager extends BaseManager {

    private AreaManagerTask areaManagerTask;

    @Override
    public void load() {
        areaManagerTask = new AreaManagerTask();
        areaManagerTask.start();
    }

    @Override
    public void unload() {
        areaManagerTask.stop();
    }

    @Override
    public void reload() {

    }
}
