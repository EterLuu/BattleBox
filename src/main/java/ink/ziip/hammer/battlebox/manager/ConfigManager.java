package ink.ziip.hammer.battlebox.manager;

import ink.ziip.hammer.battlebox.BattleBox;
import ink.ziip.hammer.battlebox.api.manager.BaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class ConfigManager extends BaseManager {

    private final FileConfiguration config;

    public static Location spawnLocation;

    public ConfigManager() {
        config = BattleBox.getInstance().getConfig();
    }

    @Override
    public void load() {
        loadConfig();
    }

    @Override
    public void unload() {

    }

    @Override
    public void reload() {
        BattleBox.getInstance().reloadConfig();
        load();
    }

    private void loadConfig() {


        World world = Bukkit.getWorld(Objects.requireNonNull(config.getString("spawn.world")));
        double x, y, z;
        float yaw, pitch;
        x = Double.parseDouble(Objects.requireNonNull(config.getString("spawn.x")));
        y = Double.parseDouble(Objects.requireNonNull(config.getString("spawn.y")));
        z = Double.parseDouble(Objects.requireNonNull(config.getString("spawn.z")));
        yaw = Float.parseFloat(Objects.requireNonNull(config.getString("spawn.yaw")));
        pitch = Float.parseFloat(Objects.requireNonNull(config.getString("spawn.pitch")));

        spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }
}
