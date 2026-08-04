package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CmdAdminSetInstability implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminSetInstability(WorldEventsModule module) { this.module = module; }

    @Override public List<String> getAliases()       { return Arrays.asList("setinstability", "setInstability"); }
    @Override public String getPermission()          { return "superior.admin.setinstability"; }
    @Override public String getUsage(Locale l)       { return "admin setinstability <người chơi> <0-100>"; }
    @Override public String getDescription(Locale l) { return "Đặt độ bất ổn đảo cho người chơi."; }
    @Override public int getMinArgs()                { return 4; }
    @Override public int getMaxArgs()                { return 4; }
    @Override public boolean canBeExecutedByConsole(){ return true; }
    @Override public boolean displayCommand()        { return true; }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        SuperiorPlayer target = SuperiorSkyblockAPI.getPlayer(args[2]);
        if (target == null) { sender.sendMessage("§cKhông tìm thấy người chơi: §e" + args[2]); return; }

        Island island = target.getIsland();
        if (island == null) { sender.sendMessage("§cNgười chơi này chưa có đảo."); return; }

        int value;
        try { value = Integer.parseInt(args[3]); }
        catch (NumberFormatException e) { sender.sendMessage("§cGiá trị phải là số từ 0 đến 100."); return; }

        int set = module.getInstabilityManager().setInstability(island.getUniqueId(), value);
        sender.sendMessage("§aĐã đặt độ bất ổn đảo của §e" + target.getName() + " §athành §e" + set + "%§a.");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
