package ink.ziip.hammer.battlebox.command.sub;

import ink.ziip.hammer.battlebox.api.command.BaseSubCommand;
import ink.ziip.hammer.battlebox.api.object.PlayerKitsEnum;
import ink.ziip.hammer.battlebox.api.object.user.User;
import ink.ziip.hammer.battlebox.api.util.Utils;
import ink.ziip.hammer.teams.api.object.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KitCommand extends BaseSubCommand {

    public KitCommand() {
        super("kit");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return true;
        }

        User user = User.getUser(sender);
        if (user == null)
            return true;

        Team team = Team.getTeamByPlayer(sender.getName());
        if (team != null) {
            try {
                PlayerKitsEnum playerKitsEnum = PlayerKitsEnum.valueOf(args[0]);
                PlayerKitsEnum.setKit((Player) sender, playerKitsEnum);
                sender.sendMessage(Utils.translateColorCodes("&6[Battle Box] &c装备选择成功。"));
            } catch (Exception ignored) {
                sender.sendMessage(Utils.translateColorCodes("&6[Battle Box] &c装备选择失败，对应装备不存在。"));
            }

        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            List<String> returnList = new java.util.ArrayList<>(Arrays.stream(PlayerKitsEnum.allPlayerKits).toList());
            try {
                returnList.removeIf(s -> !s.startsWith(args[0]));
            } catch (Exception ignored) {
            }
            return returnList;
        }

        return Collections.singletonList("");
    }
}
