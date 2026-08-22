import os

# 1. VolcanoEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/VolcanoEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.MAGMA_CREAM, "§c§lTinh Thể Dung Nham")', 'createEventItem(Material.MAGMA_CREAM, "§c§lTinh Thể Dung Nham", "§d§lSử Thi", "Sự kiện Núi Lửa", "Tinh thể nóng chảy rớt ra từ lõi của Golem Lửa. Cầm trên tay vẫn còn cảm thấy sức nóng kinh người.")')
text = text.replace('named(Material.BLAZE_ROD, "§6§lLõi Nham Thạch")', 'createEventItem(Material.BLAZE_ROD, "§6§lLõi Nham Thạch", "§e§lHiếm", "Sự kiện Núi Lửa", "Thanh nhiệt lượng cung cấp sức mạnh cho Golem Lửa.")')
text = text.replace('named(Material.NETHER_STAR, "§c§lBảo Ngọc Địa Ngục")', 'createEventItem(Material.NETHER_STAR, "§4§lBảo Ngọc Địa Ngục", "§6§lHuyền Thoại", "Sự kiện Núi Lửa", "Viên ngọc chứa đựng toàn bộ cơn thịnh nộ của núi lửa. Vô cùng hiếm có.")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 2. InvasionEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/InvasionEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.IRON_INGOT, "§c§lCúp Bảo Vệ Đảo")', 'createEventItem(Material.IRON_INGOT, "§c§lCúp Bảo Vệ Đảo", "§e§lHiếm", "Sự kiện Xâm Lược", "Kỷ niệm chương vinh danh người anh hùng đã dũng cảm bảo vệ hòn đảo khỏi bầy yêu quái.")')
text = text.replace('named(Material.GOLD_NUGGET, "§6§lXu Chiến Lợi Phẩm", 5)', 'createEventItem(Material.GOLD_NUGGET, "§6§lXu Chiến Lợi Phẩm", "§a§lThường", "Sự kiện Xâm Lược", "Đồng xu cổ được quân xâm lược mang theo.", 5)')
text = text.replace('named(Material.DIAMOND, "§c§lKim Cương Chỉ Huy", 3)', 'createEventItem(Material.DIAMOND, "§b§lKim Cương Chỉ Huy", "§6§lHuyền Thoại", "Sự kiện Xâm Lược", "Viên kim cương cướp được từ tên thủ lĩnh quân xâm lược.", 3)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 3. SpaceRiftEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/SpaceRiftEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.ENDER_PEARL, "§5§lThánh Vật Hư Vô")', 'createEventItem(Material.ENDER_PEARL, "§5§lThánh Vật Hư Vô", "§d§lSử Thi", "Vết Nứt Không Gian", "Vật thể kỳ lạ tỏa ra năng lượng tối từ chiều không gian khác.")')
text = text.replace('named(Material.EMERALD, "§d§lMảnh Cổng Không Gian")', 'createEventItem(Material.EMERALD, "§d§lMảnh Cổng Không Gian", "§e§lHiếm", "Vết Nứt Không Gian", "Một mảnh vỡ rớt lại sau khi vết nứt không gian khép lại.")')
text = text.replace('named(Material.OBSIDIAN, "§5§lTinh Chất Hư Vô", 5)', 'createEventItem(Material.OBSIDIAN, "§5§lTinh Chất Hư Vô", "§6§lHuyền Thoại", "Vết Nứt Không Gian", "Cô đặc của bóng tối hư vô. Cực kỳ giá trị.", 5)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 4. MeteorShowerEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/MeteorShowerEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(mat, "§6§lQuặng Thiên Thạch (" + mat.name() + ")", qty)', 'createEventItem(mat, "§6§lQuặng Thiên Thạch", "§e§lHiếm", "Mưa Thiên Thạch", "Khối " + mat.name() + " mang năng lượng vũ trụ vừa đập xuống đảo của bạn.", qty)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 5. TornadoEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/TornadoEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.NETHER_STAR, "§b§lLõi Bão")', 'createEventItem(Material.NETHER_STAR, "§b§lLõi Bão", "§d§lSử Thi", "Vòi Rồng", "Lõi năng lượng ngưng tụ của cơn bão dữ dội.")')
text = text.replace('named(Material.GOLD_INGOT, "§b§lMảnh Sét")', 'createEventItem(Material.GOLD_INGOT, "§e§lMảnh Sét", "§e§lHiếm", "Vòi Rồng", "Mảnh năng lượng sấm sét rớt ra từ Hồn Bão.")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 6. CelestialEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/CelestialEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.GHAST_TEAR, "§d§lMảnh Tinh Tú")', 'createEventItem(Material.GHAST_TEAR, "§d§lMảnh Tinh Tú", "§d§lSử Thi", "Thú Thiên Thể", "Một khối lấp lánh mang năng lượng từ những vì sao.")')
text = text.replace('named(Material.GLOWSTONE_DUST, "§e§lBụi Thiên Thể")', 'createEventItem(Material.GLOWSTONE_DUST, "§e§lBụi Thiên Thể", "§a§lThường", "Thú Thiên Thể", "Tàn dư bụi sáng lấp lánh sót lại của linh thú.")')
text = text.replace('named(Material.NETHER_STAR, "§b§lLõi Thiên Hà")', 'createEventItem(Material.NETHER_STAR, "§b§lLõi Thiên Hà", "§6§lHuyền Thoại", "Thú Thiên Thể", "Vật phẩm tối cao chứa đựng năng lượng thiên hà.")')
text = text.replace('named(Material.GLOWSTONE_DUST, "§e✦ Bụi Sao")', 'createEventItem(Material.GLOWSTONE_DUST, "§e✦ Bụi Sao", "§a§lThường", "Thú Thiên Thể", "Những hạt bụi phát sáng rơi xuống từ trận chiến tinh tú.")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# 7. AncientTreeEvent
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/AncientTreeEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('named(Material.VINE, "§a§lTinh Chất Thiên Nhiên")', 'createEventItem(Material.VINE, "§a§lTinh Chất Thiên Nhiên", "§e§lHiếm", "Thụ Thần Cổ Đại", "Nhựa cây tinh khiết hấp thụ sức sống ngàn năm của hòn đảo.")')
text = text.replace('named(Material.SAPLING, "§2§lHạt Giống Cổ Rừng")', 'createEventItem(Material.SAPLING, "§2§lHạt Giống Cổ Rừng", "§d§lSử Thi", "Thụ Thần Cổ Đại", "Mầm sống rực rỡ mang trong mình linh hồn của Dryad.")')
text = text.replace('named(Material.EMERALD, "§a§lBụi Rừng Xanh")', 'createEventItem(Material.EMERALD, "§a§lBụi Rừng Xanh", "§a§lThường", "Thụ Thần Cổ Đại", "Mảnh vụn sinh thái lấp lánh rớt ra từ quái cây.")')
text = text.replace('named(Material.NETHER_STAR, "§2§lPhước Lành Dryad")', 'createEventItem(Material.NETHER_STAR, "§2§lPhước Lành Dryad", "§6§lHuyền Thoại", "Thụ Thần Cổ Đại", "Linh khí thần thánh ban phước cho bất kỳ sinh vật nào sở hữu nó.")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
