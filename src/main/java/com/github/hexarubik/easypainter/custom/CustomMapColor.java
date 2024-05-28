package com.github.hexarubik.easypainter.custom;

public class CustomMapColor {
    public static final CustomMapColor CLEAR = new CustomMapColor(0, 0);
    public static final CustomMapColor PALE_GREEN = new CustomMapColor(1, 8368696);
    public static final CustomMapColor PALE_YELLOW = new CustomMapColor(2, 16247203);
    public static final CustomMapColor WHITE_GRAY = new CustomMapColor(3, 0xC7C7C7);
    public static final CustomMapColor BRIGHT_RED = new CustomMapColor(4, 0xFF0000);
    public static final CustomMapColor PALE_PURPLE = new CustomMapColor(5, 0xA0A0FF);
    public static final CustomMapColor IRON_GRAY = new CustomMapColor(6, 0xA7A7A7);
    public static final CustomMapColor DARK_GREEN = new CustomMapColor(7, 31744);
    public static final CustomMapColor WHITE = new CustomMapColor(8, 0xFFFFFF);
    public static final CustomMapColor LIGHT_BLUE_GRAY = new CustomMapColor(9, 10791096);
    public static final CustomMapColor DIRT_BROWN = new CustomMapColor(10, 9923917);
    public static final CustomMapColor STONE_GRAY = new CustomMapColor(11, 0x707070);
    public static final CustomMapColor WATER_BLUE = new CustomMapColor(12, 0x4040FF);
    public static final CustomMapColor OAK_TAN = new CustomMapColor(13, 9402184);
    public static final CustomMapColor OFF_WHITE = new CustomMapColor(14, 0xFFFCF5);
    public static final CustomMapColor ORANGE = new CustomMapColor(15, 14188339);
    public static final CustomMapColor MAGENTA = new CustomMapColor(16, 11685080);
    public static final CustomMapColor LIGHT_BLUE = new CustomMapColor(17, 6724056);
    public static final CustomMapColor YELLOW = new CustomMapColor(18, 0xE5E533);
    public static final CustomMapColor LIME = new CustomMapColor(19, 8375321);
    public static final CustomMapColor PINK = new CustomMapColor(20, 15892389);
    public static final CustomMapColor GRAY = new CustomMapColor(21, 0x4C4C4C);
    public static final CustomMapColor LIGHT_GRAY = new CustomMapColor(22, 0x999999);
    public static final CustomMapColor CYAN = new CustomMapColor(23, 5013401);
    public static final CustomMapColor PURPLE = new CustomMapColor(24, 8339378);
    public static final CustomMapColor BLUE = new CustomMapColor(25, 3361970);
    public static final CustomMapColor BROWN = new CustomMapColor(26, 6704179);
    public static final CustomMapColor GREEN = new CustomMapColor(27, 6717235);
    public static final CustomMapColor RED = new CustomMapColor(28, 0x993333);
    public static final CustomMapColor BLACK = new CustomMapColor(29, 0x191919);
    public static final CustomMapColor GOLD = new CustomMapColor(30, 16445005);
    public static final CustomMapColor DIAMOND_BLUE = new CustomMapColor(31, 6085589);
    public static final CustomMapColor LAPIS_BLUE = new CustomMapColor(32, 4882687);
    public static final CustomMapColor EMERALD_GREEN = new CustomMapColor(33, 55610);
    public static final CustomMapColor SPRUCE_BROWN = new CustomMapColor(34, 8476209);
    public static final CustomMapColor DARK_RED = new CustomMapColor(35, 0x700200);
    public static final CustomMapColor TERRACOTTA_WHITE = new CustomMapColor(36, 13742497);
    public static final CustomMapColor TERRACOTTA_ORANGE = new CustomMapColor(37, 10441252);
    public static final CustomMapColor TERRACOTTA_MAGENTA = new CustomMapColor(38, 9787244);
    public static final CustomMapColor TERRACOTTA_LIGHT_BLUE = new CustomMapColor(39, 7367818);
    public static final CustomMapColor TERRACOTTA_YELLOW = new CustomMapColor(40, 12223780);
    public static final CustomMapColor TERRACOTTA_LIME = new CustomMapColor(41, 6780213);
    public static final CustomMapColor TERRACOTTA_PINK = new CustomMapColor(42, 10505550);
    public static final CustomMapColor TERRACOTTA_GRAY = new CustomMapColor(43, 0x392923);
    public static final CustomMapColor TERRACOTTA_LIGHT_GRAY = new CustomMapColor(44, 8874850);
    public static final CustomMapColor TERRACOTTA_CYAN = new CustomMapColor(45, 0x575C5C);
    public static final CustomMapColor TERRACOTTA_PURPLE = new CustomMapColor(46, 8014168);
    public static final CustomMapColor TERRACOTTA_BLUE = new CustomMapColor(47, 4996700);
    public static final CustomMapColor TERRACOTTA_BROWN = new CustomMapColor(48, 4993571);
    public static final CustomMapColor TERRACOTTA_GREEN = new CustomMapColor(49, 5001770);
    public static final CustomMapColor TERRACOTTA_RED = new CustomMapColor(50, 9321518);
    public static final CustomMapColor TERRACOTTA_BLACK = new CustomMapColor(51, 2430480);
    public static final CustomMapColor DULL_RED = new CustomMapColor(52, 12398641);
    public static final CustomMapColor DULL_PINK = new CustomMapColor(53, 9715553);
    public static final CustomMapColor DARK_CRIMSON = new CustomMapColor(54, 6035741);
    public static final CustomMapColor TEAL = new CustomMapColor(55, 1474182);
    public static final CustomMapColor DARK_AQUA = new CustomMapColor(56, 3837580);
    public static final CustomMapColor DARK_DULL_PINK = new CustomMapColor(57, 5647422);
    public static final CustomMapColor BRIGHT_TEAL = new CustomMapColor(58, 1356933);
    public static final CustomMapColor DEEPSLATE_GRAY = new CustomMapColor(59, 0x646464);
    public static final CustomMapColor RAW_IRON_PINK = new CustomMapColor(60, 14200723);
    public static final CustomMapColor LICHEN_GREEN = new CustomMapColor(61, 8365974);
    private static final CustomMapColor[] COLORS = new CustomMapColor[64];
    public final int color;
    public final int id;

    private CustomMapColor(int id, int color) {
        if (id < 0 || id > 63) {
            throw new IndexOutOfBoundsException("Map colour ID must be between 0 and 63 (inclusive)");
        }
        this.id = id;
        this.color = color;
        CustomMapColor.COLORS[id] = this;
    }

    public static CustomMapColor[] getColors() {
        return COLORS;
    }
}