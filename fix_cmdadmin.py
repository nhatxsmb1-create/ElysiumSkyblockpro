import os
import re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/CmdAdminSpirit.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replacement = """        if (args.length >= 3 && args[2].equalsIgnoreCase("simulate")) {
            Player target = null;
            long minutes = 60;
            
            if (args.length == 3) {
                // /is admin spirit simulate
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    sender.sendMessage("\u00a7cVui l\u00f2ng nh\u1eadp t\u00ean ng\u01b0\u1eddi ch\u01a1i.");
                    return;
                }
            } else if (args.length == 4) {
                // /is admin spirit simulate <minutes> OR /is admin spirit simulate <player>
                try {
                    minutes = Long.parseLong(args[3]);
                    if (sender instanceof Player) {
                        target = (Player) sender;
                    }
                } catch (Exception e) {
                    target = org.bukkit.Bukkit.getPlayer(args[3]);
                }
            } else if (args.length == 5) {
                // /is admin spirit simulate <player> <minutes>
                target = org.bukkit.Bukkit.getPlayer(args[3]);
                try { minutes = Long.parseLong(args[4]); } catch (Exception ignored) {}
            }
            
            if (target == null) {
                sender.sendMessage("\u00a7cKh\u00f4ng t\u00ecm th\u1ea5y ng\u01b0\u1eddi ch\u01a1i.");
                return;
            }
            
            long ticks = minutes * TICKS_PER_MINUTE;
            sender.sendMessage("\u00a7aM\u00f4 ph\u1ecfng " + minutes + " ph\u00fat offline cho " + target.getName() + "...");
            module.getSpiritTask().simulateOffline(target, ticks);
            return;
        }"""

text = re.sub(
    r'        if \(args\.length >= 4 && args\[2\]\.equalsIgnoreCase\("simulate"\)\) \{.*?return;\n        \}',
    replacement,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
