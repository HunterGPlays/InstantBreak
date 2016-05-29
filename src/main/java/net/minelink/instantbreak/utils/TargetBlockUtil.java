package net.minelink.instantbreak.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class TargetBlockUtil {

    public static Block getTargetBlock(final Player player, final int range) {
        final BlockIterator iterator = new BlockIterator(player, range);
        Block lastBlock = iterator.next();
        while (iterator.hasNext()) {
            lastBlock = iterator.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

}
