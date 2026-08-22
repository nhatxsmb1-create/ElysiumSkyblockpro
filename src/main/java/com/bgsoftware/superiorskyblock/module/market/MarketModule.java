package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MarketModule extends BuiltinModule<MarketModule.Configuration> {

    private static MarketModule instance;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private MarketTask marketTask;
    private DealManager dealManager = new DealManager(this);

    public DealManager getDealManager() { return dealManager; }

    public MarketModule() {
        super("market");
        instance = this;
    }

    public static MarketModule get() {
        return instance;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        File marketFolder = new File(plugin.getDataFolder(), "modules/market");
        if (!marketFolder.exists()) {
            marketFolder.mkdirs();
        }
        dataFile = new File(marketFolder, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
            int poolSize = dataConfig.getInt("pool." + entry.getKey(), 0);
            entry.getValue().setPoolSize(poolSize);
            java.util.List<Double> hist = dataConfig.getDoubleList("history." + entry.getKey());
            if (hist != null) {
                for (double h : hist) entry.getValue().addPriceHistory(h);
            }
        }

        marketTask = new MarketTask(this);
        // Run every hour (72000 ticks)
        marketTask.runTaskTimerAsynchronously(plugin, 72000L, 72000L);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (marketTask != null) {
            marketTask.cancel();
        }
        saveData();
    }

    public void saveData() {
        if (dealManager != null) dealManager.save(dataConfig);

        if (dataConfig != null && dataFile != null) {
            for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
                dataConfig.set("pool." + entry.getKey(), entry.getValue().getPoolSize());
                dataConfig.set("history." + entry.getKey(), entry.getValue().getPriceHistory());
            }

            try {
                dataConfig.save(dataFile);
            } catch (IOException ignored) {}
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
        if (dealManager != null) dealManager.load(dataConfig);
    }

@Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[0];
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdMarket(this), new CmdThuongVu(this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminMarket(this)};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration configuration) {
        return new Configuration(configuration);
    }

    public static class Configuration implements IModuleConfiguration {
        private com.bgsoftware.common.config.CommentedConfiguration config;
        public com.bgsoftware.common.config.CommentedConfiguration getConfig() { return config; }
        @Override
        public boolean isEnabled() { return true; }
        private final Map<String, MarketItemInfo> items = new LinkedHashMap<>();
        private final Map<String, ShopItemInfo> shopItems = new LinkedHashMap<>();
        
        public Map<String, ShopItemInfo> getShopItems() {
            return shopItems;
        }

        public static class ShopItemInfo {
            private final String category;
            private final String materialName;
            private final double buyPrice;

            public ShopItemInfo(String category, String materialName, double buyPrice) {
                this.category = category;
                this.materialName = materialName;
                this.buyPrice = buyPrice;
            }

            public String getCategory() { return category; }
            public double getBuyPrice() { return buyPrice; }
            public Material getMaterial() {
                try {
                    return Material.valueOf(materialName);
                } catch (Exception ex) {
                    return Material.matchMaterial(materialName);
                }
            }
        }


        public Configuration(CommentedConfiguration config) {
            this.config = config;
            
            if (config.contains("buy-shop")) {
                org.bukkit.configuration.ConfigurationSection shopSection = config.getConfigurationSection("buy-shop");
                for (String key : shopSection.getKeys(false)) {
                    String category = shopSection.getString(key + ".category", "BUILDING");
                    double buyPrice = shopSection.getDouble(key + ".buy-price");
                    shopItems.put(key, new ShopItemInfo(category, key, buyPrice));
                }
            }
org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("items");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String category = section.getString(key + ".category", "MINERAL");
                    double basePrice = section.getDouble(key + ".base-price");
                    double minPrice = section.getDouble(key + ".min-price");
                    double maxPrice = section.getDouble(key + ".max-price");
                    double dropRate = section.getDouble(key + ".drop-rate");
                    int recoveryRate = section.getInt(key + ".recovery-rate");
                    items.put(key, new MarketItemInfo(category, key, basePrice, minPrice, maxPrice, dropRate, recoveryRate));
                }
            }
        }

        public Map<String, MarketItemInfo> getItems() {
            return items;
        }


    }

    public static class MarketItemInfo {
        private final String category;
        private final String materialName;
        private final double basePrice;
        private final double minPrice;
        private final double maxPrice;
        private final double dropRate;
        private final int recoveryRate;
        private int poolSize = 0;

        private final java.util.LinkedList<Double> priceHistory = new java.util.LinkedList<>();

        public MarketItemInfo(String category, String materialName, double basePrice, double minPrice, double maxPrice, double dropRate, int recoveryRate) {
            this.category = category;
            this.materialName = materialName;
            this.basePrice = basePrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.dropRate = dropRate;
            this.recoveryRate = recoveryRate;
        }


        public String getCategory() { return category; }
        public java.util.List<Double> getPriceHistory() { return priceHistory; }
        public void addPriceHistory(double price) {
            priceHistory.addLast(price);
            if (priceHistory.size() > 10) {
                priceHistory.removeFirst();
            }
        }

        public Material getMaterial() {
            return Material.matchMaterial(materialName);
        }

        public double getBasePrice() { return basePrice; }
        public double getMinPrice() { return minPrice; }
        public double getMaxPrice() { return maxPrice; }
        public int getRecoveryRate() { return recoveryRate; }

        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public void addPoolSize(int amount) { this.poolSize = this.poolSize + amount; }

        public double getCurrentPrice() {
            // If poolSize > 0 (many sold), price drops. If poolSize < 0 (can we have negative pool?), let's allow negative pool for price increase!
            // Wait, setPoolSize uses Math.max(0). Let's allow negative pool so price can go up!
            // Actually, if we want price to go up to maxPrice, poolSize should be allowed to go negative.
            // Let's remove Math.max(0) from setPoolSize.
            
            double current = basePrice - (poolSize * dropRate);
            if (current < minPrice) return minPrice;
            if (current > maxPrice) return maxPrice;
            return current;
        }
    }

    public static String getVietnameseName(Material mat) {
        switch (mat.name()) {
            case "DIAMOND_ORE": return "Quặng Kim Cương";
            case "DIAMOND": return "Kim Cương";
            case "DIAMOND_BLOCK": return "Khối Kim Cương";
            case "EMERALD_ORE": return "Quặng Lục Bảo";
            case "EMERALD": return "Ngọc Lục Bảo";
            case "EMERALD_BLOCK": return "Khối Lục Bảo";
            case "GOLD_ORE": return "Quặng Vàng";
            case "GOLD_INGOT": return "Thỏi Vàng";
            case "GOLD_BLOCK": return "Khối Vàng";
            case "IRON_ORE": return "Quặng Sắt";
            case "IRON_INGOT": return "Thỏi Sắt";
            case "IRON_BLOCK": return "Khối Sắt";
            case "COAL_ORE": return "Quặng Than";
            case "COAL": return "Than";
            case "COAL_BLOCK": return "Khối Than";
            case "LAPIS_ORE": return "Quặng Lapis";
            case "LAPIS_LAZULI": return "Lapis Lazuli";
            case "LAPIS_BLOCK": return "Khối Lapis";
            case "REDSTONE_ORE": return "Quặng Redstone";
            case "REDSTONE": return "Redstone";
            case "REDSTONE_BLOCK": return "Khối Redstone";
            case "NETHER_QUARTZ_ORE": return "Quặng Thạch Anh";
            case "QUARTZ": return "Thạch Anh";
            case "QUARTZ_BLOCK": return "Khối Thạch Anh";
            case "NETHERITE_INGOT": return "Phôi Netherite";
            case "NETHERITE_BLOCK": return "Khối Netherite";
            case "NETHERITE_SCRAP": return "Mảnh Netherite";
            case "ANCIENT_DEBRIS": return "Mảnh Vỡ Cổ Đại";
            case "WHEAT": return "Lúa Mì";
            case "CARROT": return "Cà Rốt";
            case "POTATO": return "Khoai Tây";
            case "BEETROOT": return "Củ Cải Đường";
            case "SUGAR_CANE": return "Mía";
            case "MELON": return "Dưa Hấu";
            case "PUMPKIN": return "Bí Ngô";
            case "CACTUS": return "Xương Rồng";
            case "NETHER_WART": return "Bướu Nether";
            case "DIRT": return "Đất";
            case "COBBLESTONE": return "Đá Cuội";
            case "OAK_LOG": return "Gỗ Sồi";
            case "GLASS": return "Kính";
            case "STONE_BRICKS": return "Gạch Đá";
            case "SEA_LANTERN": return "Đèn Biển";
            case "GLOWSTONE": return "Đá Phát Sáng";
            case "OAK_LEAVES": return "Lá Cây";
            case "ELYTRA": return "Cánh Elytra";
            case "TOTEM_OF_UNDYING": return "Totem Bất Tử";
            case "NETHER_STAR": return "Sao Nether";
            case "SPONGE": return "Mút Xốp";
            case "STONE": return "Đá";
            case "GRANITE": return "Đá Granite";
            case "DIORITE": return "Đá Diorite";
            case "ANDESITE": return "Đá Andesite";
            case "DEEPSLATE": return "Đá Đen";
            case "TUFF": return "Đá Ngưng Thạch";
            case "SAND": return "Cát";
            case "RED_SAND": return "Cát Đỏ";
            case "GRAVEL": return "Sỏi";
            case "SPRUCE_LOG": return "Gỗ Vân Sam";
            case "BIRCH_LOG": return "Gỗ Bạch Dương";
            case "JUNGLE_LOG": return "Gỗ Rừng";
            case "ACACIA_LOG": return "Gỗ Xiêm Gai";
            case "DARK_OAK_LOG": return "Gỗ Sồi Sẫm";
            case "CHERRY_LOG": return "Gỗ Anh Đào";
            case "MANGROVE_LOG": return "Gỗ Đước";
            case "BAMBOO_BLOCK": return "Khối Tre";
            case "OBSIDIAN": return "Hắc Óc Thạch";
            case "CRYING_OBSIDIAN": return "Hắc Óc Thạch Khóc";
            case "LANTERN": return "Đèn Lồng";
            case "SOUL_LANTERN": return "Đèn Lồng Linh Hồn";
            case "CAMPFIRE": return "Lửa Trại";
            case "SOUL_CAMPFIRE": return "Lửa Trại Linh Hồn";
            case "BOOKSHELF": return "Kệ Sách";
            case "CHISELED_BOOKSHELF": return "Kệ Sách Điêu Khắc";
            case "PAINTING": return "Tranh Vẽ";
            case "ITEM_FRAME": return "Khung Vật Phẩm";
            case "GLOW_ITEM_FRAME": return "Khung Phát Sáng";
            case "FLOWER_POT": return "Chậu Hoa";
            case "BELL": return "Chuông";
            case "IRON_PICKAXE": return "Cúp Sắt";
            case "IRON_AXE": return "Rìu Sắt";
            case "IRON_SHOVEL": return "Xẻng Sắt";
            case "IRON_HOE": return "Cuốc Sắt";
            case "DIAMOND_AXE": return "Rìu Kim Cương";
            case "DIAMOND_SHOVEL": return "Xẻng Kim Cương";
            case "DIAMOND_HOE": return "Cuốc Kim Cương";
            case "NETHERITE_PICKAXE": return "Cúp Netherite";
            case "BUCKET": return "Xô";
            case "WATER_BUCKET": return "Xô Nước";
            case "LAVA_BUCKET": return "Xô Dung Nham";
            case "FISHING_ROD": return "Cần Câu";
            case "FLINT_AND_STEEL": return "Bật Lửa";
            case "SHEARS": return "Kéo";
            case "NAME_TAG": return "Thẻ Tên";
            case "LEAD": return "Dây Dẫn";
            case "BEACON": return "Đèn Tín Hiệu";
            case "CONDUIT": return "Ống Dẫn Nước";
            case "SHULKER_BOX": return "Hộp Shulker";
            case "DRAGON_EGG": return "Trứng Rồng";
            case "DRAGON_HEAD": return "Đầu Rồng";
            case "WITHER_SKELETON_SKULL": return "Đầu Wither";
            case "ENCHANTED_GOLDEN_APPLE": return "Táo Vàng Phù Phép";
            case "HEART_OF_THE_SEA": return "Trái Tim Của Biển";
            case "ECHO_SHARD": return "Mảnh Vỡ Tiếng Vang";
            default:
                String[] words = mat.name().split("_");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        sb.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) {
                            sb.append(word.substring(1).toLowerCase());
                        }
                        sb.append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
}
