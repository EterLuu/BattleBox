package ink.ziip.hammer.battlebox.api.object.area;

import ink.ziip.hammer.battlebox.BattleBox;
import ink.ziip.hammer.battlebox.api.object.PlayerKitsEnum;
import ink.ziip.hammer.battlebox.api.object.team.TeamCard;
import ink.ziip.hammer.battlebox.api.object.user.User;
import ink.ziip.hammer.battlebox.api.util.Utils;
import ink.ziip.hammer.battlebox.manager.ConfigManager;
import ink.ziip.hammer.teams.api.object.GameTypeEnum;
import ink.ziip.hammer.teams.api.object.Team;
import ink.ziip.hammer.teams.manager.TeamRecordManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class Area implements Listener {

    private static final Map<String, Area> areas = new ConcurrentHashMap<>();

    private String areaName;
    private int areaTimer;
    private int areaDefaultTimer;
    private BoundingBox areaBoundingBox;
    private BoundingBox woolAreaBoundingBox;
    private Location team1SpawnPoint;
    private Location team2SpawnPoint;

    private Boolean status = false;
    private Boolean started = false;

    private final List<TeamCard> teamCards = new ArrayList<>();
    private final List<User> spectators = new ArrayList<>();
    private final List<String> deathPlayer = new ArrayList<>();
    private final Map<Team, Integer> teamPoints = new ConcurrentHashMap<>();
    private final List<Location> potionLocations = new ArrayList<>();
    private Location woolAreaPos1;
    private Location woolAreaPos2;
    private World world;

    private int runTaskId;

    private void resetData() {
        started = false;
        resetBlocksInRegion(woolAreaPos1.getWorld(), woolAreaPos1.getBlockX(), woolAreaPos1.getBlockY(), woolAreaPos1.getBlockZ(),
                woolAreaPos2.getBlockX(), woolAreaPos2.getBlockY(), woolAreaPos2.getBlockZ());

        world.getNearbyEntities(areaBoundingBox).forEach(entity -> {
            if (entity instanceof Item) {
                entity.remove();
            }
        });

        teamCards.clear();
        areaTimer = 0;
        deathPlayer.clear();
        teamPoints.clear();
    }

    public String getAreaName() {
        return areaName;
    }

    public void loadFromConfig(String name) {
        File file = new File(BattleBox.getInstance().getDataFolder() + File.separator + "areas" + File.separator + name + ".yml");

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        areaName = config.getString("area.name");
        areaTimer = 0;
        areaDefaultTimer = config.getInt("area.timer");
        Location areaPos1 = Utils.getLocation(config.getString("area.pos1"));
        Location areaPos2 = Utils.getLocation(config.getString("area.pos2"));
        areaBoundingBox = new BoundingBox(
                areaPos1.getX(), areaPos1.getY(), areaPos1.getZ(),
                areaPos2.getX(), areaPos2.getY(), areaPos2.getZ());

        woolAreaPos1 = Utils.getLocation(config.getString("area.wool-pos1"));
        woolAreaPos2 = Utils.getLocation(config.getString("area.wool-pos2"));
        woolAreaBoundingBox = new BoundingBox(
                woolAreaPos1.getX(), woolAreaPos1.getY() + 1, woolAreaPos1.getZ(),
                woolAreaPos2.getX(), woolAreaPos2.getY(), woolAreaPos2.getZ());

        world = woolAreaPos1.getWorld();

        team1SpawnPoint = Utils.getLocation(config.getString("area.team1-spawn-point"));
        team2SpawnPoint = Utils.getLocation(config.getString("area.team2-spawn-point"));

        for (String content : config.getStringList("area.potions")) {
            potionLocations.add(Utils.getLocation(content));
        }

        resetData();

        status = true;
        Bukkit.getPluginManager().registerEvents(this, BattleBox.getInstance());
    }

    public static Area getArea(String name) {
        if (areas.containsKey(name)) {
            return areas.get(name);
        }
        return null;
    }

    public static void createArea(String name) {
        if (!areas.containsKey(name)) {
            Area area = new Area();
            area.loadFromConfig(name);
            areas.put(name, area);
        }
    }

    public static List<String> getAreaList() {
        return new ArrayList<>(areas.keySet().stream().toList());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (!isAreaPlayer(event.getPlayer())) {
            return;
        }

        Location location = event.getPlayer().getLocation();
        if (areaBoundingBox.contains(location.getX(), location.getY(), location.getZ())) {
            if (!woolAreaBoundingBox.contains(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if (!isAreaPlayer(event.getPlayer())) {
            return;
        }

        Location location = event.getPlayer().getLocation();
        if (areaBoundingBox.contains(location.getX(), location.getY(), location.getZ())) {
            if (!woolAreaBoundingBox.contains(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockY(), event.getBlock().getLocation().getBlockZ())) {
                event.setCancelled(true);
            }
            event.setDropItems(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isAreaPlayer(event.getPlayer())) {
            return;
        }

        Location location = event.getPlayer().getLocation();
        if (areaBoundingBox.contains(location.getX(), location.getY(), location.getZ())) {
            if (areaTimer >= areaDefaultTimer) {
                event.setCancelled(true);
            }
            return;
        }
        User user = User.getUser(event.getPlayer());
        user.teleport(team1SpawnPoint);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isAreaPlayer(event.getEntity())) {
            return;
        }
        Location location = event.getEntity().getLocation();
        if (areaBoundingBox.contains(location.getX(), location.getY(), location.getZ())) {
            Team playerTeam = Team.getTeamByPlayer(event.getEntity());
            if (playerTeam != null) {
                if (playerTeam.equals(getTeam1().getTeam())) {
                    addPointsToTeam(getTeam2(), 15);
                } else if (playerTeam.equals(getTeam2().getTeam())) {
                    addPointsToTeam(getTeam1(), 15);
                }
                event.setDeathMessage(null);
                deathPlayer.add(event.getEntity().getName());
                sendMessageToAllUsers("&c[Battle Box] " + playerTeam.getColoredName() + " &b玩家 " + event.getEntity().getName() + " 被击杀。（+15分）");
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getEntity().spigot().respawn();
                    event.getEntity().teleport(team1SpawnPoint);
                    event.getEntity().setGameMode(GameMode.SPECTATOR);
                }
            }.runTask(BattleBox.getInstance());
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event) {
        User user = User.getUser(event.getPlayer());
        spectatorLeave(user);

        if (isAreaPlayer(event.getPlayer())) {
            Team playerTeam = Team.getTeamByPlayer(event.getPlayer());
            if (playerTeam != null) {
                if (playerTeam.equals(getTeam1().getTeam())) {
                    addPointsToTeam(getTeam2(), 15);
                } else if (playerTeam.equals(getTeam2().getTeam())) {
                    addPointsToTeam(getTeam1(), 15);
                }
                deathPlayer.add(event.getPlayer().getName());
                sendMessageToAllUsers("&c[Battle Box] " + playerTeam.getColoredName() + " &b玩家 " + event.getPlayer().getName() + " 离开游戏（被击杀）。（+15分）");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isAreaPlayer(event.getPlayer())) {
            return;
        }
        if (deathPlayer.contains(event.getPlayer().getName())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().setGameMode(GameMode.SPECTATOR);
                }
            }.runTask(BattleBox.getInstance());
        }
    }

    private boolean checkJoin(TeamCard teamCard) {
        if (!teamCard.getArea().equals(this))
            return false;
        if (teamCards.contains(teamCard))
            return false;
        if (teamCard.getOnlinePlayers().isEmpty())
            return false;

        return true;
    }

    public boolean joinGame(TeamCard teamCard1, TeamCard teamCard2) {
        if (!status)
            return false;
        if (started)
            return false;
        if (!teamCards.isEmpty())
            return false;

        if (!checkJoin(teamCard1))
            return false;

        if (!checkJoin(teamCard2))
            return false;

        teamCards.add(teamCard1);
        teamCards.add(teamCard2);

        return true;
    }

    public boolean joinAsSpectator(User user) {
        if (!status) {
            return false;
        }

        if (spectators.contains(user)) {
            return false;
        }

        for (TeamCard teamCard : teamCards) {
            if (teamCard.getOnlineUsers().contains(user)) {
                return false;
            }
        }

        spectators.add(user);
        user.setGameMode(GameMode.SPECTATOR);
        user.teleport(team1SpawnPoint);

        return true;
    }

    public boolean spectatorLeave(User user) {
        for (TeamCard teamCard : teamCards) {
            if (teamCard.getOnlineUsers().contains(user)) {
                return false;
            }
        }

        if (spectators.contains(user)) {
            spectators.remove(user);
            user.teleport(ConfigManager.spawnLocation);
            user.setGameMode(GameMode.ADVENTURE);

            return true;
        }
        return false;
    }

    public boolean checkStart() {
        if (!status)
            return false;
        if (started)
            return false;
        if (teamCards.size() != 2)
            return false;
        started = true;
        startGame();
        return true;
    }

    public void summonPotions() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location location : potionLocations) {
                    ItemStack item = new ItemStack(Material.SPLASH_POTION);
                    PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                    if (potionMeta != null) {
                        PotionEffect potionEffect = new PotionEffect(PotionEffectType.HARM, 2, 1);
                        potionMeta.addCustomEffect(potionEffect, true);
                        item.setItemMeta(potionMeta);
                        world.dropItem(location, item);
                    }
                }
            }
        }.runTask(BattleBox.getInstance());
    }

    private void startGame() {

        areaTimer = areaDefaultTimer + 10;

        teleportTeamsToSpawnPoint();

        runTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(BattleBox.getInstance(), new Runnable() {
            @Override
            public void run() {

                setAllPlayerLevel(areaTimer);
                sendActionBarToAllSpectators(" &6游戏剩余时间：&c" + areaTimer);

                if (areaTimer > areaDefaultTimer) {
                    sendTitleToAllUsers("&b游戏即将开始", "&c倒计时：&6" + String.valueOf(areaTimer - areaDefaultTimer));
                    playerSoundToAllUsers(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.5F);
                }

                if (Objects.equals(areaTimer, areaDefaultTimer)) {
                    sendTitleToAllUsers("&c[Battle Box]", "&b游戏开始！");
                    playerSoundToAllUsers(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                    summonPotions();
                }

                if (areaTimer == 5) {
                    playerSoundToAllUsers(Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5F);
                } else if (areaTimer == 4) {
                    playerSoundToAllUsers(Sound.ENTITY_PLAYER_LEVELUP, 1, 0.6F);
                } else if (areaTimer == 3) {
                    playerSoundToAllUsers(Sound.ENTITY_PLAYER_LEVELUP, 1, 0.7F);
                } else if (areaTimer == 2) {
                    playerSoundToAllUsers(Sound.ENTITY_PLAYER_LEVELUP, 1, 0.8F);
                } else if (areaTimer == 1) {
                    playerSoundToAllUsers(Sound.ENTITY_PLAYER_LEVELUP, 1, 0.9F);
                }

                if (areaTimer == 0) {
                    Bukkit.getScheduler().cancelTask(runTaskId);
                    endGame();
                    return;
                }

                checkEnd();

                areaTimer = areaTimer - 1;
            }
        }, 0L, 20L);
    }

    public HashMap<Material, Integer> countBlocksInRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        HashMap<Material, Integer> blockTypeCounts = new HashMap<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = block.getType();
                    // 忽略空气方块
                    if (material == Material.AIR) {
                        continue;
                    }

                    // 统计方块类型
                    blockTypeCounts.put(material, blockTypeCounts.getOrDefault(material, 0) + 1);
                }
            }
        }

        return blockTypeCounts;
    }

    public void resetBlocksInRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        for (int x = minX; x <= maxX - 1; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ - 1; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.BLACK_WOOL);
                    block.getState().setType(Material.BLACK_WOOL);
                    block.getState().update(true);
                }
            }
        }
    }


    public void checkEnd() {
        if (started) {
            HashMap<Material, Integer> blockTypeCounts = countBlocksInRegion(woolAreaPos1.getWorld(), woolAreaPos1.getBlockX(), woolAreaPos1.getBlockY(), woolAreaPos1.getBlockZ(),
                    woolAreaPos2.getBlockX(), woolAreaPos2.getBlockY(), woolAreaPos2.getBlockZ());

            int team1Wools = blockTypeCounts.getOrDefault(Material.getMaterial(getTeam1().getTeam().getColorName() + "_WOOL"), 0);
            int team2Wools = blockTypeCounts.getOrDefault(Material.getMaterial(getTeam2().getTeam().getColorName() + "_WOOL"), 0);

            if (team1Wools == 9) {
                directGame(getTeam1());
            } else if (team2Wools == 9) {
                directGame(getTeam2());
            }
        }
    }

    public void directGame(TeamCard win) {
        if (started) {
            Bukkit.getScheduler().cancelTask(runTaskId);

            addPointsToTeam(win, 200);
            sendMessageToAllUsers("&c[Battle Box] " + getTeam1().getTeam().getColoredName() + " &b获得了积分 " + getTeamPoints(getTeam1()) + " 分。");
            sendMessageToAllUsers("&c[Battle Box] " + getTeam2().getTeam().getColoredName() + " &b获得了积分 " + getTeamPoints(getTeam2()) + " 分。");

            TeamRecordManager.addTeamRecord(getTeam1().getTeam(), getTeam2().getTeam(), GameTypeEnum.BattleBox.name(), areaName, getTeamPoints(getTeam1()), false);
            TeamRecordManager.addTeamRecord(getTeam2().getTeam(), getTeam1().getTeam(), GameTypeEnum.BattleBox.name(), areaName, getTeamPoints(getTeam2()), false);
            teleportTeamsToLobby();
            resetData();
        }
    }

    public void endGame() {
        if (started) {
            Bukkit.getScheduler().cancelTask(runTaskId);
            HashMap<Material, Integer> blockTypeCounts = countBlocksInRegion(woolAreaPos1.getWorld(), woolAreaPos1.getBlockX(), woolAreaPos1.getBlockY(), woolAreaPos1.getBlockZ(),
                    woolAreaPos2.getBlockX(), woolAreaPos2.getBlockY(), woolAreaPos2.getBlockZ());

            int team1Wools = blockTypeCounts.getOrDefault(Material.getMaterial(getTeam1().getTeam().getColorName() + "_WOOL"), 0);
            int team2Wools = blockTypeCounts.getOrDefault(Material.getMaterial(getTeam2().getTeam().getColorName() + "_WOOL"), 0);

            sendMessageToAllUsers("&c[Battle Box] " + "&b结算羊毛数，" + getTeam1().getTeam().getColoredName() + "：" + team1Wools +
                    "&b，" + getTeam2().getTeam().getColoredName() + "：" + team2Wools);

            if (team1Wools > team2Wools) {
                addPointsToTeam(getTeam1(), 200);
            } else if (team1Wools < team2Wools) {
                addPointsToTeam(getTeam2(), 200);
            }

            sendMessageToAllUsers("&c[Battle Box] " + getTeam1().getTeam().getColoredName() + " &b获得了积分 " + getTeamPoints(getTeam1()) + " 分。");
            sendMessageToAllUsers("&c[Battle Box] " + getTeam2().getTeam().getColoredName() + " &b获得了积分 " + getTeamPoints(getTeam2()) + " 分。");

            TeamRecordManager.addTeamRecord(getTeam1().getTeam(), getTeam2().getTeam(), GameTypeEnum.BattleBox.name(), areaName, getTeamPoints(getTeam1()), false);
            TeamRecordManager.addTeamRecord(getTeam2().getTeam(), getTeam1().getTeam(), GameTypeEnum.BattleBox.name(), areaName, getTeamPoints(getTeam2()), false);
            teleportTeamsToLobby();
        }
        resetData();
    }

    private void teleportTeamsToSpawnPoint() {
        teamCards.get(0).getOnlineUsers().forEach(user -> {
            user.teleport(team1SpawnPoint);
            user.setGameMode(GameMode.SURVIVAL);
            user.getPlayer().getInventory().clear();
            PlayerKitsEnum.getKit(user.getPlayer());
            ItemStack team = new ItemStack(Material.valueOf(getTeam1().getTeam().getColorName() + "_WOOL"));
            team.setAmount(64);
            user.getPlayer().getInventory().addItem(team);
        });
        teamCards.get(1).getOnlineUsers().forEach(user -> {
            user.teleport(team2SpawnPoint);
            user.setGameMode(GameMode.SURVIVAL);
            user.getPlayer().getInventory().clear();
            PlayerKitsEnum.getKit(user.getPlayer());
            ItemStack team = new ItemStack(Material.valueOf(getTeam2().getTeam().getColorName() + "_WOOL"));
            team.setAmount(64);
            user.getPlayer().getInventory().addItem(team);
        });
    }

    private void playerSoundToAllUsers(Sound sound, float volume, float pitch) {
        for (User user : spectators) {
            user.playSound(sound, volume, pitch);
        }
        teamCards.forEach(teamCard -> {
            for (User user : teamCard.getOnlineUsers()) {
                user.playSound(sound, volume, pitch);
            }
        });
    }

    private void sendTitleToAllUsers(String title, String subTitle) {
        for (User user : spectators) {
            user.sendTitle(title, subTitle);
        }
        teamCards.forEach(teamCard -> {
            for (User user : teamCard.getOnlineUsers()) {
                user.sendTitle(title, subTitle);
            }
        });
    }

    private void setAllPlayerLevel(int level) {
        teamCards.forEach(teamCard -> {
            for (User user : teamCard.getOnlineUsers()) {
                user.setLevel(level);
            }
        });
    }

    private void sendActionBarToAllSpectators(String content) {
        for (User user : spectators) {
            user.sendActionBar(content, false);
        }
        teamCards.forEach(teamCard -> {
            for (User user : teamCard.getOnlineUsers()) {
                if (user.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                    user.sendActionBar(content, false);
                }
            }
        });
    }

    private void sendMessageToAllUsers(String content) {
        teamCards.forEach(teamCard -> teamCard.sendMessage(content));
        for (User user : spectators) {
            user.sendMessage(content);
        }
        Bukkit.getLogger().log(Level.INFO, areaName + " " + content);
    }

    private boolean isAreaPlayer(Player player) {
        boolean is = false;

        for (TeamCard teamCard : teamCards) {
            if (teamCard.getOnlinePlayers().contains(player)) {
                is = true;
            }
        }

        return is;
    }

    private void teleportTeamsToLobby() {
        teamCards.forEach(teamCard -> {
            teamCard.getOnlineUsers().forEach(user -> {
                user.teleport(ConfigManager.spawnLocation);
                user.setGameMode(GameMode.ADVENTURE);
                user.getPlayer().getInventory().clear();
                user.setLevel(0);
                user.getPlayer().setHealth(20);
                user.getPlayer().getActivePotionEffects().forEach(potionEffect -> {
                    user.getPlayer().removePotionEffect(potionEffect.getType());
                });
            });
            teamCard.setArea(null);
        });
    }

    private void addPointsToTeam(TeamCard teamCard, int points) {
        teamPoints.putIfAbsent(teamCard.getTeam(), 0);
        teamPoints.put(teamCard.getTeam(), teamPoints.get(teamCard.getTeam()) + points);
    }

    private int getTeamPoints(TeamCard teamCard) {
        return teamPoints.getOrDefault(teamCard.getTeam(), 0);
    }

    public TeamCard getTeam1() {
        return teamCards.get(0);
    }

    public TeamCard getTeam2() {
        return teamCards.get(1);
    }

}
