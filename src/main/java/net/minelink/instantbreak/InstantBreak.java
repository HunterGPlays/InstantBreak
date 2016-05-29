package net.minelink.instantbreak;

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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstantBreak extends JavaPlugin implements Listener {

    private static InstantBreak instance;
    public static InstantBreak getInstance() {
        return instance;
    }

    private double serverVersion;
    public final Set<Material> materials = new HashSet<>();

    private final List<String> blockedWorlds = getConfig().getStringList("blocked-worlds");
    private final boolean restrictionsEnabled = getConfig().getBoolean("only-allow-instantbreak-with.enabled");
    private final List<String> restrictionsItems = getConfig().getStringList("only-allow-instantbreak-with.items");

    @Override
    public void onEnable() {
        instance = this;

        // Make sure config exists if not already existing.
        saveDefaultConfig();
        reloadConfig();

        // Get the Minecraft server version.
        serverVersion = getMCVersion();
        getLogger().info("Running server version " + serverVersion);

        // Get instant break materials from config.
        for (String mat : getConfig().getStringList("materials")) {
            mat = mat.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            try {
                materials.add(Material.valueOf(mat));
            } catch (final IllegalArgumentException e) {
                getLogger().severe("Unknown material: " + mat);
            }
        }

        // Get allowed items for breaking if enabled.
        if (restrictionsEnabled) {
            for (final String item : restrictionsItems) {
                try {
                    Material.valueOf(item);
                } catch (final IllegalArgumentException e) {
                    getLogger().severe("Unknown item: " + item);
                }
            }
        }

        // Load listeners.
        Bukkit.getPluginManager().registerEvents(this, this);
        if (Bukkit.getPluginManager().isPluginEnabled("AAC")) {
            getLogger().info("Attempting to hook into AAC...");
            new AACHook(this);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        // Check if world is on blocked list.
        for (final String s : blockedWorlds) {
            if (player.getWorld().getName().equals(s))
                return;
        }

        // Exempt for creative players.
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        // Check if player left clicked.
        final Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK)
            return;

        // Check if block is on instant break list.
        final Block block = event.getClickedBlock();
        if (materials.contains(block.getType())) {
            // If restrictions are enabled, do its own checks.
            if (restrictionsEnabled) {
                for (final String item : restrictionsItems) {
                    // Check if player is holding allowed item.
                    if ((serverVersion <= 1.8 && !(player.getInventory().getItemInHand().getType() == Material.valueOf(item)))
                            || (!(serverVersion <= 1.8) && !(player.getInventory().getItemInMainHand().getType() == Material.valueOf(item)))) {
                        continue;
                    }
                    breakBlock(block, player);
                    return;
                }
            } else {
                breakBlock(block, player);
            }
        }
    }

    private void breakBlock(final Block block, final Player player) {
        // Create new BlockBreakEvent.
        final BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        // Call and check if event is not cancelled.
        Bukkit.getPluginManager().callEvent(blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType(), 16);
            block.breakNaturally();
        }
    }

    private double getMCVersion() {
        // Get version from Bukkit.
        String version = new String(Bukkit.getVersion());
        final int pos = version.indexOf("(MC: ");
        // Clean it up to get the numbers.
        version = version.substring(pos + 5).replace(")", "");
        // Parse as a double.
        final String[] splitVersion = version.split("\\.");
        return Double.parseDouble(splitVersion[0] + "." + splitVersion[1]);
    }

}
