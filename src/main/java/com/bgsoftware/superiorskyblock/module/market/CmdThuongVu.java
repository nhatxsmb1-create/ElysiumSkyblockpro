package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

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
    public String getUsage(CommandSender sender) {
        return "/is thuongvu";
    }

    @Override
    public String getDescription(CommandSender sender) {
        return "M\u1edf giao di\u1ec7n Th\u01b0\u01a1ng V\u1ee5 B\u1ea1c T\u1ef7";
    }

    @Override
    public int getMinArgs() {
        return 0;
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
    public void execute(SuperiorPlayer superiorPlayer, String[] args) {
        Player player = superiorPlayer.asPlayer();
        DealMenu menu = new DealMenu(module, module.getPlugin());
        menu.open(player);
    }

    @Override
    public List<String> tabComplete(SuperiorPlayer superiorPlayer, String[] args) {
        return Collections.emptyList();
    }
}
