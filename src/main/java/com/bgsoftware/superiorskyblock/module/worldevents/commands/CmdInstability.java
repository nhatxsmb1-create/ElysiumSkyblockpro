package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CmdInstability implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdInstability(WorldEventsModule module) { this.module = module; }

    @Override public List<String> getAliases()       { return Arrays.asList("baodon", "unstable"); }
    @Override public String getPermission()          { return ""; }
    @Override public String getUsage(Locale l)       { return "baodon"; }
    @Override public String getDescription(Locale l) { return "Xem mức độ bất ổn đảo của bạn."; }
    @Override public int getMinArgs()                { return 1; }
    @Override public int getMaxArgs()                { return 1; }
    @Override public boolean canBeExecutedByConsole(){ return false; }
    @Override public boolean displayCommand()        { return true; }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();
        if (island == null) {
            player.sendMessage("§cBạn chưa có đảo!");
            return;
        }

        java.util.UUID id = island.getUniqueId();
        int instability   = module.getInstabilityManager().getInstability(id);
        boolean active    = module.getScheduler().isActive(id);
        boolean cd        = module.getScheduler().isOnCooldown(id);
        long cdSec        = module.getScheduler().getCooldownRemaining(id);

        int filled = instability / 5;
        String color = instability < 25 ? "§a" : instability < 50 ? "§e" : instability < 75 ? "§c" : "§4";

        // Build progress bar without using String.repeat() for Java 8 compatibility
        int filledCount = Math.max(0, filled);
        int emptyCount = Math.max(0, 20 - filledCount);
        StringBuilder filledBuilder = new StringBuilder(filledCount);
        for (int i = 0; i < filledCount; i++) filledBuilder.append('█');
        StringBuilder emptyBuilder = new StringBuilder(emptyCount);
        for (int i = 0; i < emptyCount; i++) emptyBuilder.append('█');
        String bar = color + filledBuilder.toString() + "§8" + emptyBuilder.toString();

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§d§l  ĐỘ BẤT ỔN ĐẢO");
        player.sendMessage("  " + bar + " " + color + instability + "%");
        player.sendMessage("");
        player.sendMessage("  §7Trạng thái: " + (active ? "§c⚡ Đang có sự kiện"
                : cd ? "§e⏱ Hồi chiêu §7(" + cdSec + "s)" : "§a✔ Sẵn sàng"));
        player.sendMessage("  §7Cấp đảo: §f" + island.getIslandLevel().intValue());
        player.sendMessage("");

        if (instability < 25)
            player.sendMessage("  §7Mức nguy hiểm: §aThấp §8— Chỉ sự kiện nhẹ");
        else if (instability < 50)
            player.sendMessage("  §7Mức nguy hiểm: §eTrung bình §8— Lốc Xoáy, Xâm Lược");
        else if (instability < 75)
            player.sendMessage("  §7Mức nguy hiểm: §cCao §8— Núi Lửa có thể xuất hiện");
        else
            player.sendMessage("  §7Mức nguy hiểm: §4§lCỰC KỲ NGUY HIỂM §8— Cổng Không Gian sắp đến!");

        player.sendMessage("  §7Bất ổn giảm §f" + module.getConfiguration().getInstabilityDecayPerCheck()
                + "% §7mỗi §f" + module.getConfiguration().getCheckIntervalSeconds() / 60 + " phút §7tự động.");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
