package ink.ziip.hammer.battlebox.api.task;

import ink.ziip.hammer.battlebox.BattleBox;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BaseTask extends BukkitRunnable {

    protected boolean started;
    protected int period;

    public BaseTask(int period) {
        this.started = false;
        this.period = period;
    }

    public void start() {
        this.runTaskTimerAsynchronously(BattleBox.getInstance(), 1, period);
        started = true;
    }

    public abstract void stop();
}
