package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
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
        return "trophy give <nguoi-choi> <ma-trophy>";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Tặng trophy cho người chơi.";
    }

    @Override
    public int getMinArgs() {
        return 5;
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
        if (!args[2].equalsIgnoreCase("give")) {
            sender.sendMessage("§cCách dùng: /is admin trophy give <nguoi-choi> <ma-trophy>");
            return;
        }

        SuperiorPlayer target = plugin.getPlayers().getSuperiorPlayer(args[3]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy người chơi " + args[3] + ".");
            return;
        }

        ItemStack trophyItem = module.getTrophyManager().createTrophyItem(args[4]);
        if (trophyItem == null) {
            sender.sendMessage("§cTrophy không hợp lệ: " + args[4] + ".");
            sender.sendMessage("§7Các mã có sẵn: " + String.join(", ", module.getTrophyManager().getTrophies().keySet()));
            return;
        }

        Player player = target.asPlayer();
        if (player != null) {
            player.getInventory().addItem(trophyItem);
            player.sendMessage("§6🏆 §eBạn đã nhận được " + trophyItem.getItemMeta().getDisplayName() + "§e!");
        } else {
            sender.sendMessage("§cNgười chơi không trực tuyến.");
            return;
        }

        sender.sendMessage("§aĐã tặng trophy " + args[4] + " cho " + args[3] + ".");
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
