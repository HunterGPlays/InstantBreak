package net.minelink.instantbreak;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minelink.instantbreak.hooks.AACHook;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class InstantBreak extends JavaPlugin implements Listener {
    public InstantBreak plugin;
    public static final Set<Material> materials = new HashSet<>();
    public final List<String> blockedWorlds = getConfig().getStringList("blocked-worlds");
    public final boolean restrictionsEnabled = getConfig().getBoolean("only-allow-instantbreak-with.enabled", false);
    public final List<String> restrictionsItems = getConfig().getStringList("only-allow-instantbreak-with.items");
    public final boolean restrictionsDamageItem = getConfig().getBoolean("only-allow-instantbreak-with.damage-item");

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        for (String mat : getConfig().getStringList("materials")) {
            mat = mat.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            try {
                materials.add(Material.valueOf(mat));
            } catch (IllegalArgumentException e) {
                getLogger().severe("Unknown material: " + mat);
            }
        }
        
        if (restrictionsEnabled) {
            for (String item : restrictionsItems) {
                try {
                    Material.valueOf(item);
                } catch (IllegalArgumentException e) {
                    getLogger().severe("Unknown item: " + item);
                }
            }
        }
        

        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginManager().isPluginEnabled("AAC")) {
            getLogger().info("Attempting to hook into AAC...");
            new AACHook((Plugin)this);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        for (String s : blockedWorlds) {
            if (player.getWorld().getName().equals(s)) {
                return;
            }
        }
        
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (materials.contains(block.getType())) {
            if (restrictionsEnabled) {
                for (String item : restrictionsItems) {
                    if (player.getItemInHand().getType() == Material.valueOf(item)) {
                        breakBlock(block, player);
                        return;
                    }
                }
            } else { 
                breakBlock(block, player);
            }
        }
    }
    
    public void breakBlock(Block block, Player player) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType(), 16);
            block.breakNaturally();
        }
    }
}
