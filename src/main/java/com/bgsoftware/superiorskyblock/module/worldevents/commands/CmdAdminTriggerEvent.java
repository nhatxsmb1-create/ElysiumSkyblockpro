package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class CmdAdminTriggerEvent implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminTriggerEvent(WorldEventsModule module) { this.module = module; }

    @Override public List<String> getAliases()       { return Arrays.asList("triggerevent", "triggerEvent"); }
    @Override public String getPermission()          { return "superior.admin.triggerevent"; }
    @Override public String getUsage(Locale l)       { return "admin triggerevent <người chơi> <" + types() + ">"; }
    @Override public String getDescription(Locale l) { return "Kích hoạt sự kiện thế giới trên đảo của người chơi."; }
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

        WorldEventType type;
        try { type = WorldEventType.valueOf(args[3].toUpperCase()); }
        catch (IllegalArgumentException e) { sender.sendMessage("§cSự kiện không hợp lệ. Danh sách: " + types()); return; }

        module.getScheduler().triggerEvent(island, type);
        sender.sendMessage("§aĐã kích hoạt §e" + type.getDisplayName() + " §atrên đảo của §e" + target.getName() + "§a.");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 3) return SuperiorSkyblockAPI.getGrid().getIslands().stream()
                .map(i -> i.getOwner().getName()).filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        if (args.length == 4) return Arrays.stream(WorldEventType.values()).map(Enum::name)
                .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase())).collect(Collectors.toList());
        return Collections.emptyList();
    }

    private String types() {
        return Arrays.stream(WorldEventType.values()).map(Enum::name).collect(Collectors.joining("|"));
    }
}
