package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminTrophy implements SuperiorCommand {

    private final TrophiesModule module;

    public CmdAdminTrophy(TrophiesModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("trophy");
    }

    @Override
    public String getPermission() {
        return "superior.admin.trophy";
    }

    @Override
    public String getUsage(Locale locale) {
        return "trophy [give <nguoi-choi> <ma-trophy>]";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Quản lý và tặng trophy.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        // Open Admin GUI if just typing `/is admin trophy`
        if (args.length <= 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00a7cConsole vui lòng dùng: /is admin trophy give <nguoi-choi> <ma-trophy>");
                return;
            }
            Player player = (Player) sender;
            new AdminTrophiesMenu(module).open(player);
            return;
        }

        if (args.length < 5 || !args[2].equalsIgnoreCase("give")) {
            sender.sendMessage("\u00a7cCách dùng:");
            sender.sendMessage("\u00a7c- Mở Menu: /is admin trophy");
            sender.sendMessage("\u00a7c- Tặng Trophy: /is admin trophy give <nguoi-choi> <ma-trophy>");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            sender.sendMessage("\u00a7cKhông tìm thấy người chơi " + args[3] + ".");
            return;
        }

        ItemStack trophyItem = module.getTrophyManager().createTrophyItem(args[4]);
        if (trophyItem == null) {
            sender.sendMessage("\u00a7cTrophy không hợp lệ: " + args[4] + ".");
            sender.sendMessage("\u00a77Các mã có sẵn: " + String.join(", ", module.getTrophyManager().getTrophies().keySet()));
            return;
        }

        Player player = target.asPlayer();
        if (player != null) {
            player.getInventory().addItem(trophyItem);
            player.sendMessage("\u00a76\ud83c\udfc6 \u00a7eBạn đã nhận được " + trophyItem.getItemMeta().getDisplayName() + "\u00a7e!");
        } else {
            sender.sendMessage("\u00a7cNgười chơi không trực tuyến.");
            return;
        }

        sender.sendMessage("\u00a7aĐã tặng trophy " + args[4] + " cho " + args[3] + ".");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 3)
            return Collections.singletonList("give");
        if (args.length == 5)
            return new ArrayList<>(module.getTrophyManager().getTrophies().keySet());
        return Collections.emptyList();
    }

}
