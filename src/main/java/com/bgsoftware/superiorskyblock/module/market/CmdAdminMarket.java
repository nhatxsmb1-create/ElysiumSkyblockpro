package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminMarket implements SuperiorCommand {

    private final MarketModule module;

    public CmdAdminMarket(MarketModule module) {
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return java.util.Arrays.asList("market", "chungkhoan");
    }

    @Override
    public String getPermission() {
        return "elysium.admin.market";
    }

    @Override
    public String getUsage(Locale locale) {
        return "market recover";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Qu\u1ea3n l\u00fd S\u00e0n Ch\u1ee9ng Kho\u00e1n";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean displayCommand() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 3 && args[2].equalsIgnoreCase("recover")) {
            for (MarketModule.MarketItemInfo info : module.getConfiguration().getItems().values()) {
                info.addPriceHistory(info.getCurrentPrice());
                info.addPoolSize(-info.getRecoveryRate());
            }
            module.saveData();
            sender.sendMessage("\u00a7a\u0110\u00e3 ti\u1ebfn h\u00e0nh ph\u1ee5c h\u1ed3i gi\u00e1 th\u1ecb tr\u01b0\u1eddng th\u1ee7 c\u00f4ng!");
            return;
        }
        sender.sendMessage("\u00a7cS\u1eed d\u1ee5ng: /is admin market recover");
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (args.length == 3) {
            return Collections.singletonList("recover");
        }
        return Collections.emptyList();
    }
}
