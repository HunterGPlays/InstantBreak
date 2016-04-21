package net.minelink.instantbreak.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationEvent;
import net.minelink.instantbreak.InstantBreak;
import net.minelink.instantbreak.utils.TargetBlockUtil;

public class AACHook implements Listener {

    public AACHook(final Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void cancelAACFastbreak(final PlayerViolationEvent event) {
        final Player player = event.getPlayer();
        final HackType hacktype = event.getHackType();
        if (hacktype != HackType.FASTBREAK)
            return;

        for (final Material mat : InstantBreak.materials) {
            if (TargetBlockUtil.getTargetBlock(player, 5).getType() == mat) {
                event.setCancelled(true);
            }
        }
    }

}
