package com.bgsoftware.superiorskyblock.module.orestorage;

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

public class CmdKho implements SuperiorCommand {

    private final OreStorageModule module;

    public CmdKho(OreStorageModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("kho", "storage");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage(Locale locale) {
        return "kho";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Mở Kho Chứa Quặng của Đảo.";
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
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();

        if (island == null) {
            player.sendMessage("§cBạn phải có đảo mới dùng được tính năng này!");
            return;
        }

        OreStorageMenu menu = new OreStorageMenu(module, island);
        menu.open(player);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
