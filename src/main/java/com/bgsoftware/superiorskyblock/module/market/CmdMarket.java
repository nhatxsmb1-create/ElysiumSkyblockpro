package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdMarket implements SuperiorCommand {

    private final MarketModule module;

    public CmdMarket(MarketModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return java.util.Arrays.asList("market", "chungkhoan");
    }

    @Override
    public String getPermission() {
        return "elysium.player.market";
    }

    @Override
    public String getUsage(Locale locale) {
        return "market";
    }

    @Override
    public String getDescription(Locale locale) {
        return "M\u1edf S\u00e0n Ch\u1ee9ng Kho\u00e1n T\u00e0i Nguy\u00ean";
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
            new MarketMenu(module, plugin).open((Player) sender);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
