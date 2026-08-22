package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminEventItems implements SuperiorCommand {

    private final WorldEventsModule module;

    public CmdAdminEventItems(WorldEventsModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("eventitems", "eventItems");
    }

    @Override
    public String getPermission() {
        return "superior.admin.eventitems";
    }

    @Override
    public String getUsage(Locale locale) {
        return "admin eventitems";
    }

    @Override
    public String getDescription(Locale locale) {
        return "M\u1edf GUI l\u1ea5y v\u1eadt ph\u1ea9m S\u1ef1 ki\u1ec7n";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            AdminEventItemsMenu menu = new AdminEventItemsMenu(module, plugin);
            menu.open((Player) sender);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
