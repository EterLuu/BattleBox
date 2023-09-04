package ink.ziip.hammer.battlebox.api.object;

import ink.ziip.hammer.teams.api.object.Team;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public enum PlayerKitsEnum {
    Axe, Crossbow, Sword, Bow;

    private static final Map<String, PlayerKitsEnum> playerKits = new ConcurrentHashMap<>();
    private static final PlayerKitsEnum[] playerKitsEnums = {
            PlayerKitsEnum.Axe,
            PlayerKitsEnum.Crossbow,
            PlayerKitsEnum.Sword,
            PlayerKitsEnum.Bow,
    };

    public static final String[] allPlayerKits = {
            "Axe",
            "Crossbow",
            "Sword",
            "Bow",
    };

    public static void setKit(Player player, PlayerKitsEnum playerKitsEnum) {
        playerKits.put(player.getName(), playerKitsEnum);
    }

    public static PlayerKitsEnum getRandom() {
        Random random = new Random();
        int num = random.nextInt(0, 3);
        return playerKitsEnums[num];
    }

    public static PlayerKitsEnum getPlayerKit(String name) {
        if (playerKits.containsKey(name))
            return playerKits.get(name);

        return getRandom();
    }

    public static Color hex2Rgb(String colorStr) {
        try {
            return Color.fromBGR(
                    Integer.valueOf(colorStr.substring(1, 3), 16),
                    Integer.valueOf(colorStr.substring(3, 5), 16),
                    Integer.valueOf(colorStr.substring(5, 7), 16));
        } catch (Exception ignored) {
            return Color.fromBGR(0, 0, 0);
        }
    }

    public static void getKit(Player player) {
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta meta = leggings.hasItemMeta() ? leggings.getItemMeta() : Bukkit.getItemFactory().getItemMeta(leggings.getType());
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
        Team team = Team.getTeamByPlayer(player);
        if (leatherArmorMeta != null && team != null) {
            leatherArmorMeta.setColor(hex2Rgb(team.getColorCode()));
            leggings.setItemMeta(leatherArmorMeta);
            player.getInventory().setLeggings(leggings);
        }

        PlayerKitsEnum kitsEnum = getPlayerKit(player.getName());
        if (kitsEnum == PlayerKitsEnum.Bow) {
            ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
            ItemStack bow = new ItemStack(Material.BOW);
            bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
            ItemStack arrow = new ItemStack(Material.ARROW);
            arrow.setAmount(8);
            ItemStack shears = new ItemStack(Material.SHEARS);
            player.getInventory().addItem(sword, bow, arrow, shears);
        }
        if (kitsEnum == PlayerKitsEnum.Axe) {
            ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
            axe.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            ItemStack bow = new ItemStack(Material.BOW);
            ItemStack arrow = new ItemStack(Material.ARROW);
            arrow.setAmount(4);
            ItemStack shears = new ItemStack(Material.SHEARS);
            player.getInventory().addItem(axe, bow, arrow, shears);
        }
        if (kitsEnum == PlayerKitsEnum.Crossbow) {
            ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
            ItemStack crossbow = new ItemStack(Material.CROSSBOW);
            crossbow.addEnchantment(Enchantment.QUICK_CHARGE, 1);
            ItemStack arrow = new ItemStack(Material.ARROW);
            arrow.setAmount(6);
            ItemStack shears = new ItemStack(Material.SHEARS);
            player.getInventory().addItem(sword, crossbow, arrow, shears);
        }
        if (kitsEnum == PlayerKitsEnum.Sword) {
            ItemStack sword = new ItemStack(Material.STONE_SWORD);
            sword.addEnchantment(Enchantment.SWEEPING_EDGE, 1);
            ItemStack bow = new ItemStack(Material.BOW);
            ItemStack arrow = new ItemStack(Material.ARROW);
            arrow.setAmount(4);
            ItemStack shears = new ItemStack(Material.SHEARS);
            player.getInventory().addItem(sword, bow, arrow, shears);
        }
    }
}
