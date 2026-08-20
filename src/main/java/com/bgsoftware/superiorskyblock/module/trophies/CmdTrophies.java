package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdTrophies implements SuperiorCommand {

    private final TrophiesModule module;

    public CmdTrophies(TrophiesModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("trophies", "trophy", "thanh-tich");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage(Locale locale) {
        return "trophies";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Xem Trophy Hall của đảo.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
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
        if (!(sender instanceof Player))
            return;
        Player player = (Player) sender;

        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();

        if (island == null) {
            player.sendMessage("\u00a7cBạn phải có đảo mới dùng được tính năng này!");
            return;
        }

        new TrophiesMainMenu(module, island, player).open();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
