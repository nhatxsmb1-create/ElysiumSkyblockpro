package com.bgsoftware.superiorskyblock.module.market;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDate;
import java.util.*;

public class DealManager {
    private final MarketModule module;
    
    private boolean active = false;
    private Material targetMaterial = Material.WHEAT;
    private long targetAmount = 50000;
    private long currentAmount = 0;
    private String dealDate = "";
    
    private Map<UUID, Long> contributors = new HashMap<>();
    
    public DealManager(MarketModule module) {
        this.module = module;

        Bukkit.getScheduler().runTaskTimer((org.bukkit.plugin.Plugin) module.getPlugin(), () -> {
            if (active) {
                Bukkit.getServer().broadcastMessage("\u00a7e\u00a7l\u272a TH\u01af\u01a0NG V\u1ee4 B\u1ea0C T\u1ef6 \u0111ang g\u1ecdi v\u1ed1n \u272a");
                Bukkit.getServer().broadcastMessage("\u00a7fTi\u1ebfn \u0111\u1ed9 thu mua: \u00a7a" + currentAmount + " \u00a78/ \u00a7a" + targetAmount + " " + MarketModule.getVietnameseName(targetMaterial));
                Bukkit.getServer().broadcastMessage("\u00a77\u27A4 G\u00f5 \u00a7a/is thuongvu \u00a77\u0111\u1ec3 b\u00e1n \u0111\u1ed3 cho T\u1eadp \u0111o\u00e0n v\u00e0 nh\u1eadn th\u01b0\u1edfng li\u1ec1n tay!");
            }
        }, 20 * 60 * 15L, 20 * 60 * 15L);

    }
    
    public void load(org.bukkit.configuration.file.YamlConfiguration dataConfig) {
        String today = LocalDate.now().toString();
        this.dealDate = dataConfig.getString("deal.date", "");
        
        if (!dealDate.equals(today)) {
            generateNewDeal(today);
        } else {
            this.active = dataConfig.getBoolean("deal.active", true);
            try { this.targetMaterial = Material.valueOf(dataConfig.getString("deal.material", "WHEAT")); } catch (Exception e) {}
            this.targetAmount = dataConfig.getLong("deal.target", 50000);
            this.currentAmount = dataConfig.getLong("deal.current", 0);
            
            ConfigurationSection sec = dataConfig.getConfigurationSection("deal.contributors");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    contributors.put(UUID.fromString(key), sec.getLong(key));
                }
            }
        }
    }
    
    public void save(org.bukkit.configuration.file.YamlConfiguration dataConfig) {
        dataConfig.set("deal.date", dealDate);
        dataConfig.set("deal.active", active);
        dataConfig.set("deal.material", targetMaterial.name());
        dataConfig.set("deal.target", targetAmount);
        dataConfig.set("deal.current", currentAmount);
        
        dataConfig.set("deal.contributors", null);
        for (Map.Entry<UUID, Long> entry : contributors.entrySet()) {
            dataConfig.set("deal.contributors." + entry.getKey().toString(), entry.getValue());
        }
    }
    
    public void generateNewDeal(String date) {
        this.dealDate = date;
        this.active = true;
        this.currentAmount = 0;
        this.contributors.clear();
        
        List<Map<?, ?>> possible = module.getConfiguration().getConfig().getMapList("deal-settings.possible-deals");
        if (possible != null && !possible.isEmpty()) {
            Map<?, ?> chosen = possible.get(new Random().nextInt(possible.size()));
            try { this.targetMaterial = Material.valueOf(chosen.get("material").toString()); } catch (Exception e) {}
            try { this.targetAmount = Long.parseLong(chosen.get("target").toString()); } catch (Exception e) {}
        }
    }
    
    public void addContribution(org.bukkit.entity.Player player, long amount) {
        if (!active) return;
        UUID uuid = player.getUniqueId();
        
        long oldAmount = contributors.getOrDefault(uuid, 0L);
        long newAmount = oldAmount + amount;
        contributors.put(uuid, newAmount);
        
        currentAmount += amount;
        
        int chunkSize = module.getConfiguration().getConfig().getInt("deal-settings.chunk-size", 100);
        long oldChunks = oldAmount / chunkSize;
        long newChunks = newAmount / chunkSize;
        
        if (newChunks > oldChunks) {
            long chunksToReward = newChunks - oldChunks;
            List<String> chunkRewards = module.getConfiguration().getConfig().getStringList("deal-settings.chunk-rewards");
            for (int i = 0; i < chunksToReward; i++) {
                for (String cmd : chunkRewards) {
                    executeReward(player.getName(), cmd);
                }
            }
        }
        
        if (currentAmount >= targetAmount) {
            completeDeal();
        }
        module.saveData();
    }
    
    private void completeDeal() {
        this.active = false;
        
        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(contributors.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<String> top1Rewards = module.getConfiguration().getConfig().getStringList("deal-settings.top1-rewards");
        List<String> top2Rewards = module.getConfiguration().getConfig().getStringList("deal-settings.top2-rewards");
        List<String> top3Rewards = module.getConfiguration().getConfig().getStringList("deal-settings.top3-rewards");
        
        if (sorted.size() > 0) rewardTop(sorted.get(0).getKey(), top1Rewards);
        if (sorted.size() > 1) rewardTop(sorted.get(1).getKey(), top2Rewards);
        if (sorted.size() > 2) rewardTop(sorted.get(2).getKey(), top3Rewards);
    }
    
    private void rewardTop(UUID uuid, List<String> rewards) {
        org.bukkit.OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p.getName() == null) return;
        for (String cmd : rewards) {
            executeReward(p.getName(), cmd);
        }
    }
    
    private void executeReward(String playerName, String cmd) {
        String finalCmd = cmd.replace("%player%", playerName);
        if (finalCmd.startsWith("msg ")) {
            String msg = finalCmd.substring(4).replace("&", "\u00a7");
            org.bukkit.entity.Player p = Bukkit.getPlayerExact(playerName);
            if (p != null) p.sendMessage(msg);
        } else if (finalCmd.startsWith("broadcast ")) {
            Bukkit.getServer().broadcastMessage(finalCmd.substring(10).replace("&", "\u00a7"));
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
    }
    
    public boolean isActive() { return active; }
    public Material getTargetMaterial() { return targetMaterial; }
    public long getTargetAmount() { return targetAmount; }
    public long getCurrentAmount() { return currentAmount; }
    
    public List<Map.Entry<UUID, Long>> getTopContributors() {
        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(contributors.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sorted;
    }
    
    public long getPlayerContribution(UUID uuid) {
        return contributors.getOrDefault(uuid, 0L);
    }
}
