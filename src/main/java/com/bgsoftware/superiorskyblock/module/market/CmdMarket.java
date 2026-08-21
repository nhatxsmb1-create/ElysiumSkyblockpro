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
        return java.util.Arrays.asList("shop", "market", "chungkhoan");
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage(Locale locale) {
        return "shop";
    }

    @Override
    public String getDescription(Locale locale) {
        return "M\u1edf Trung T\u00e2m Giao Th\u01b0\u01a1ng";
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
            String cmd = args.length > 0 ? args[0].toLowerCase() : "";
            MarketMenu menu = new MarketMenu(module, plugin);
            if (cmd.equals("market")) {
                menu.openBuyShop((Player) sender);
            } else if (cmd.equals("chungkhoan")) {
                menu.openMarket((Player) sender);
            } else {
                // Mặc định (/is shop) sẽ mở Menu Ở Giữa (Trade Center)
                menu.open((Player) sender);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
