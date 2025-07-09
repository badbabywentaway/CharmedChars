package org.stephanosbad.charmedChars.Utility;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ColorPrint {
    static public void sendPlayer(@NotNull Player player, String inString)
    {
        player.sendMessage(colorize(inString));
    }

    private static @NotNull String colorize(@NotNull String inString) {

        StringBuilder outString = new StringBuilder();
        for(char c: inString.toCharArray())
        {
            outString.append(randomColor()).append(c);
        }
        return outString.toString();
    }
    /*§0	black	0	0	0
     #000000	0	0	0
     #000000
    §1	dark_blue	0	0	170
     #0000AA	0	0	42
     #00002A
    §2	dark_green	0	170	0
     #00AA00	0	42	0
     #002A00
    §3	dark_aqua	0	170	170
     #00AAAA	0	42	42
     #002A2A
    §4	dark_red	170	0	0
     #AA0000	42	0	0
     #2A0000
    §5	dark_purple	170	0	170
     #AA00AA	42	0	42
     #2A002A
    §6	gold	255	170	0
     #FFAA00	42	42	0
     #2A2A00	‌[JE only]
    64	42	0
     #402A00	‌[BE only]
    §7	gray	170	170	170
     #AAAAAA	42	42	42
     #2A2A2A
    §8	dark_gray	85	85	85
     #555555	21	21	21
     #151515
    §9	blue	85	85	255
     #5555FF	21	21	63
     #15153F
    §a	green	85	255	85
     #55FF55	21	63	21
     #153F15
    §b	aqua	85	255	255
     #55FFFF	21	63	63
     #153F3F
    §c	red	255	85	85
     #FF5555	63	21	21
     #3F1515
    §d	light_purple	255	85	255
     #FF55FF	63	21	63
     #3F153F
    §e	yellow	255	255	85
     #FFFF55	63	63	21
     #3F3F15
    §f	white	255	255	255
     #FFFFFF	63	63	63
     #3F3F3F
    §g	minecoin_gold	221	214	5
     #DDD605	55	53	1
     #373501	*/
    enum codedColor
    {
        BLUE("§9"),
        GREEN("§a"),
        AQUA("§b"),
        RED("§c"),
        PURPLE("§d"),
        YELLOW("§e");

        public final String code;
        codedColor(String s) {
            code = s;
        }
    }
    private static String randomColor() {

        return codedColor.values()[(int)(Math.random() * codedColor.values().length)].code;

    }
}