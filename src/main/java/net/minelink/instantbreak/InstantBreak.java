package net.minelink.instantbreak;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import net.minelink.instantbreak.hooks.AACHook;

public final class InstantBreak extends JavaPlugin implements Listener {

    public static final Set<Material> materials = new HashSet<>();

    public static InstantBreak instance;
    public double versionAsDouble;
    public final List<String> blockedWorlds = getConfig().getStringList("blocked-worlds");
    public final boolean restrictionsEnabled = getConfig().getBoolean("only-allow-instantbreak-with.enabled", false);
    public final List<String> restrictionsItems = getConfig().getStringList("only-allow-instantbreak-with.items");
    public final boolean restrictionsDamageItem = getConfig().getBoolean("only-allow-instantbreak-with.damage-item");

    public void breakBlock(final Block block, final Player player) {
        final BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType(), 16);
            block.breakNaturally();
        }
    }

    public String getMCVersion() {
        String version = new String(Bukkit.getVersion());
        final int pos = version.indexOf("(MC: ");
        version = version.substring(pos + 5).replace(")", "");
        return version;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        final String version = getMCVersion();
        final String[] splitVersion = version.split("\\.");
        versionAsDouble = Double.parseDouble(splitVersion[0] + "." + splitVersion[1]);
        getLogger().info("Running Bukkit version " + version);

        for (String mat : getConfig().getStringList("materials")) {
            mat = mat.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            try {
                materials.add(Material.valueOf(mat));
            } catch (final IllegalArgumentException e) {
                getLogger().severe("Unknown material: " + mat);
            }
        }

        if (restrictionsEnabled) {
            for (final String item : restrictionsItems) {
                try {
                    Material.valueOf(item);
                } catch (final IllegalArgumentException e) {
                    getLogger().severe("Unknown item: " + item);
                }
            }
        }

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

        for (final String s : blockedWorlds) {
            if (player.getWorld().getName().equals(s))
                return;
        }

        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        final Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();
        if (materials.contains(block.getType())) {
            if (restrictionsEnabled) {
                for (final String item : restrictionsItems) {
                    if (versionAsDouble <= 1.8
                            && player.getInventory().getItemInHand().getType() == Material.valueOf(item)) {
                        breakBlock(block, player);
                        return;
                    } else if (!(versionAsDouble <= 1.8)
                            && player.getInventory().getItemInMainHand().getType() == Material.valueOf(item)) {
                        breakBlock(block, player);
                        return;
                    }
                }
            } else {
                breakBlock(block, player);
            }
        }
    }

}
