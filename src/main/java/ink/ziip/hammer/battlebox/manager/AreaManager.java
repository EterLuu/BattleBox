package ink.ziip.hammer.battlebox.manager;

import ink.ziip.hammer.battlebox.BattleBox;
import ink.ziip.hammer.battlebox.api.manager.BaseManager;
import ink.ziip.hammer.battlebox.api.object.area.Area;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class AreaManager extends BaseManager {
    @Override
    public void load() {
        final File areasFolder = new File(BattleBox.getInstance().getDataFolder() + File.separator + "areas");
        new BukkitRunnable() {
            @Override
            public void run() {
                List<String> areaList = Arrays.asList(areasFolder.list());
                for (String file : areaList) {
                    Area.createArea(file.substring(0, file.length() - 4));
                }
            }
        }.runTaskLater(BattleBox.getInstance(), 20L);
    }

    @Override
    public void unload() {
        Area.getAreaList().forEach(area -> {
            Area.getArea(area).endGame();
        });
    }

    @Override
    public void reload() {

    }
}
