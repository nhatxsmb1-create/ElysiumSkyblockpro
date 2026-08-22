package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdThuongVu implements SuperiorCommand {

    private final MarketModule module;

    public CmdThuongVu(MarketModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("thuongvu");
    }

    @Override
    public String getPermission() {
        return "superiorskyblock.thuongvu";
    }

    @Override
    public String getUsage(Locale locale) {
        return "thuongvu";
    }

    @Override
    public String getDescription(Locale locale) {
        return "M\u1edf giao di\u1ec7n Th\u01b0\u01a1ng V\u1ee5 B\u1ea1c T\u1ef7";
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
        if (sender instanceof Player) {
            DealMenu menu = new DealMenu(module, plugin);
            menu.open((Player) sender);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
