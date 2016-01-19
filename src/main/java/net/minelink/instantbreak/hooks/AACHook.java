package net.minelink.instantbreak.hooks;

import me.konsolas.aac.api.AACAPIProvider;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import net.minelink.instantbreak.InstantBreak;
import net.minelink.instantbreak.utils.TargetBlockUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class AACHook {
    public static void hook(InstantBreak plugin) {
        AACAPIProvider.getAPI();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancelAACFastbreak(PlayerViolationEvent event) {
        Player player = event.getPlayer();
        HackType hacktype = event.getHackType();
        if (hacktype != HackType.FASTBREAK) {
            return;
        }
        for (Material mat : InstantBreak.getInstance().materials) {
            if (TargetBlockUtil.getTargetBlock(player, 6).getType() == mat) {
                event.setCancelled(true);
            }
        }
    }
}
