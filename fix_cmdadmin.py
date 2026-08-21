import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/CmdAdminSpirit.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('return 4; // args[0] = "admin", args[1] = "spirit", args[2] = "<player>", args[3] = "<type>"', 'return 5;')
text = text.replace('public class CmdAdminSpirit implements SuperiorCommand {', 'public class CmdAdminSpirit implements SuperiorCommand {\n\n    private static final long TICKS_PER_MINUTE = 1200L;')

simulate_logic = '''
        if (args.length >= 4 && args[2].equalsIgnoreCase("simulate")) {
            Player target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage("\u00a7cKh\u00f4ng t\u00ecm th\u1ea5y ng\u01b0\u1eddi ch\u01a1i.");
                return;
            }
            long minutes = 60;
            if (args.length == 5) {
                try { minutes = Long.parseLong(args[4]); } catch (Exception ignored) {}
            }
            long ticks = minutes * TICKS_PER_MINUTE;
            sender.sendMessage("\u00a7aM\u00f4 ph\u1ecfng " + minutes + " ph\u00fat offline cho " + target.getName() + "...");
            module.getSpiritTask().simulateOffline(target, ticks);
            return;
        }
'''

text = text.replace('        if (args.length == 4) {', simulate_logic + '\n        if (args.length == 4) {')
text = text.replace('S\u1eed d\u1ee5ng: /is admin spirit \u0111\u1ec3 m\u1edf menu ho\u1eb7c /is admin spirit <player> <type>', 'S\u1eed d\u1ee5ng: /is admin spirit, /is admin spirit <player> <type>, /is admin spirit simulate <player> <minutes>')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
