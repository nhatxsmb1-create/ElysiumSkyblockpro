package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdSpirits implements SuperiorCommand {

    private final SpiritsModule module;

    public CmdSpirits(SpiritsModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("spirit");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage(Locale locale) {
        return "spirit";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Qu\u1ea3n l\u00fd Tinh Linh tr\u00ean \u0111\u1ea3o.";
    }

    @Override
    public int getMinArgs() {
        return 0;
    }

    @Override
    public int getMaxArgs() {
        return 0;
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
        Player player = (Player) sender;
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();
        if (island == null) {
            player.sendMessage("\u00a7cB\u1ea1n ph\u1ea3i c\u00f3 \u0111\u1ea3o \u0111\u1ec3 d\u00f9ng l\u1ec7nh n\u00e0y!");
            return;
        }

        new PlayerSpiritsMenu(module, island).open(player);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
