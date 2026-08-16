## Trophy Hall — Kế hoạch chi tiết

Tính năng: Mini Boss của Island Event rơi **Trophy** (đầu custom). Người chơi đặt trophy lên đảo để trưng bày; bộ sưu tập càng nhiều buff càng tăng. Làm theo pattern module builtin sẵn có (`orestorage`, `worldevents`) — module mới tên `trophies`.

### 1. Module骨架 (copy pattern OreStorageModule)
- `module/trophies/TrophiesModule.java` — extends BuiltinModule, config từ `resources/modules/trophies/config.yml`
- Đăng ký: thêm `TROPHIES` vào `BuiltinModules.java` + `registerModule` trong `ModulesManagerImpl.java` (~dòng 54)
- `module/trophies/TrophyManager.java` — registry 7 trophy (mỗi event 1 loại: Tornado, Volcano, AncientTree, Celestial, Invasion, MeteorShower, SpaceRift), tạo ItemStack skull custom texture (qua `ItemSkulls.getPlayerHead`), tên dạng `§6§l🏆 Trophy <tên>` + lore mô tả. Nhận diện trophy khi đặt bằng display name (đúng pattern `StackedBlocksListener` đang dùng, không cần PDC)

### 2. Lưu trữ — dùng Island PersistentDataContainer có sẵn
- Key `trophies:placed` trong island PDC (bảng `islands_custom_data` đã có sẵn — không đụng DB)
- Dữ liệu: danh sách `trophyId;world;x;y;z` các trophy đang đặt trên đảo

### 3. Listener (`module/trophies/listeners/TrophyListener.java`)
- **BlockPlaceEvent**: item là trophy + trong đảo → ghi vào PDC, phát thông báo, áp lại buff
- **BlockBreakEvent**: block tại vị trí có trong danh sách → xóa khỏi PDC, rơi lại item trophy, áp lại buff (khách không破 được do protection đảo sẵn có chặn)
- **BlockExplodeEvent / EntityExplodeEvent (HIGHEST)**: gỡ block trophy khỏi danh sách nổ → trophy không thể bị nổ phá
- Sau mỗi thay đổi → re-apply buff

### 4. Buff — 2 giai đoạn (theo lựa chọn "Cả hai")
**Giai đoạn A — Potion (làm ngay, không đụng core):** task lặp 8 giây trong module, quét người chơi đang trên đảo có đủ mốc bộ sưu tập:
- 3 loại: Haste I
- 5 loại: + Speed I
- 7 loại (đủ bộ): Night Vision + Haste II

**Giai đoạn B — Multiplier:** sửa nhỏ 3 getter trong `SIsland.java` (`getCropGrowthMultiplier`, `getSpawnerRatesMultiplier`, `getMobDropsMultiplier`) nhân thêm bonus từ TrophyManager:
- 5 loại: +5% crop growth
- 7 loại: +10% crop growth & +5% mob drops
(3 chỗ sửa chirurgic, mỗi chỗ ~2 dòng — không đổi logic hiện có)

### 5. Cắm drop vào 7 event có sẵn
- Thêm 1 helper `dropTrophy(Location)` vào `IslandWorldEvent.java` (kiểm tra module bật + tỉ lệ rơi trong config, mặc định 35%, boss event xong gọi)
- Thêm 1 dòng gọi helper vào block drop khi boss chết của từng event (vd `AncientTreeEvent.java:91-95`) — 7 file, mỗi file 1 dòng

### 6. Menu + lệnh
- `/is trophies` (CmdTrophies theo pattern CmdKho): GUI 7 ô hiển thị từng trophy — đã đặt ✓ / chưa có ✗, mốc buff hiện tại, lore tiếng Việt
- `menus/trophies.yml` theo pattern menu có sẵn
- `/is admin trophy give <player> <id>` để test/admin tặng

### 7. Config `resources/modules/trophies/config.yml`
- `enabled`, `drop-chance`, danh sách 7 trophy (tên hiển thị + texture head), các mốc potion + multiplier (đề xuất như trên, chỉnh được)

### 8. Kiểm tra
- `gradlew build` compile qua
- Kiểm tra YAML mới parse hợp lệ

**Không P2W:** mọi trophy chỉ nhận bằng cách đánh boss event, buff nhỏ chỉ cộng dồn khi trưng bày — bóc lên mất buff đúng tinh thần "hành trình không reset".