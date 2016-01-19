package net.minelink.instantbreak.hooks;

import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import net.minelink.instantbreak.InstantBreak;
import net.minelink.instantbreak.utils.TargetBlockUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class AACHook implements Listener {
    public AACHook(final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents((Listener)this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancelAACFastbreak(PlayerViolationEvent event) {
        Player player = event.getPlayer();
        HackType hacktype = event.getHackType();
        if (hacktype != HackType.FASTBREAK) {
            return;
        }

        for (Material mat : InstantBreak.materials) {
            if (TargetBlockUtil.getTargetBlock(player, 5).getType() == mat) {
                event.setCancelled(true);
            }
        }
    }
}
