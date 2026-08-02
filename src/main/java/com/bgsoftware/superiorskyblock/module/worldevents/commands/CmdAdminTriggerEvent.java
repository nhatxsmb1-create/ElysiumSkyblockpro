package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /is admin triggerevent <player> <eventType>
 */
public class CmdAdminTriggerEvent implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminTriggerEvent(WorldEventsModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("triggerevent", "triggerEvent");
    }

    @Override
    public String getPermission() {
        return "superior.admin.triggerevent";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin triggerevent <player> <" + eventTypeList() + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Force a World Event on a player's island.";
    }

    @Override
    public int getMinArgs() { return 3; }

    @Override
    public boolean onlyPlayers() { return false; }

    @Override
    public void execute(com.bgsoftware.superiorskyblock.api.SuperiorSkyblock plugin,
                        CommandSender sender, String[] args) {
        // args: [0]=admin [1]=triggerevent [2]=playerName [3]=eventType
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /" + getUsage(java.util.Locale.ENGLISH));
            return;
        }

        SuperiorPlayer target = SuperiorSkyblockAPI.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + args[2] + "' not found.");
            return;
        }

        Island island = target.getIsland();
        if (island == null) {
            sender.sendMessage("§cThat player doesn't have an island.");
            return;
        }

        WorldEventType type;
        try {
            type = WorldEventType.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cUnknown event type. Options: " + eventTypeList());
            return;
        }

        module.getScheduler().triggerEvent(island, type);
        sender.sendMessage("§aTriggered §e" + type.getDisplayName()
                + " §aon §e" + target.getName() + "§a's island.");
    }

    @Override
    public List<String> tabComplete(com.bgsoftware.superiorskyblock.api.SuperiorSkyblock plugin,
                                     CommandSender sender, String[] args) {
        if (args.length == 3) {
            return SuperiorSkyblockAPI.getGrid().getIslands().stream()
                    .map(i -> i.getOwner().getName())
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4) {
            return Arrays.stream(WorldEventType.values())
                    .map(Enum::name)
                    .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    private String eventTypeList() {
        return Arrays.stream(WorldEventType.values()).map(Enum::name)
                .collect(Collectors.joining("|"));
    }
}
