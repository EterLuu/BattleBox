package ink.ziip.hammer.battlebox.command.sub;

import ink.ziip.hammer.battlebox.api.command.BaseSubCommand;
import ink.ziip.hammer.battlebox.api.object.area.Area;
import ink.ziip.hammer.battlebox.api.object.user.User;
import ink.ziip.hammer.battlebox.api.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveCommand extends BaseSubCommand {

    public LeaveCommand() {
        super("leave");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            User user = User.getUser(sender);
            if (user == null)
                return true;

            for (String name : Area.getAreaList()) {
                if (Area.getArea(name).spectatorLeave(user)) {
                    sender.sendMessage(Utils.translateColorCodes("&c[Battle Box] &b退出旁观模式。"));
                }
            }
            sender.sendMessage(Utils.translateColorCodes("&c[Battle Box] &b你现在没有旁观任何游戏。"));
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.singletonList("");
    }
}
