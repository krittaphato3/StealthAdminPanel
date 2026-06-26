package com.adminpanel;

import com.adminpanel.command.StealthCommand;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.hooks.VaultHook;
import com.adminpanel.listener.*;
import com.adminpanel.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for StealthAdminPanel.
 * Handles initialization, dynamic command registration, and lifecycle.
 */
public final class AdminPanel extends JavaPlugin {

    private static AdminPanel instance;
    private DataManager dataManager;
    private VaultHook vaultHook;
    private ChatManager chatManager;
    private PunishmentManager punishmentManager;
    private SessionManager sessionManager;
    private AuditManager auditManager;
    private WarpManager warpManager;
    private NoteManager noteManager;
    private EconomyManager economyManager;
    private PresetManager presetManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize database
        getLogger().info("Initializing SQLite database...");
        dataManager = new DataManager(this);
        dataManager.initialize();

        // Initialize managers
        chatManager = new ChatManager(this);
        punishmentManager = new PunishmentManager(this, dataManager);
        sessionManager = new SessionManager(this, dataManager);
        auditManager = new AuditManager(this, dataManager);
        warpManager = new WarpManager(this, dataManager);
        noteManager = new NoteManager(this, dataManager);
        presetManager = new PresetManager(this, dataManager);

        // Hook into Vault (optional)
        vaultHook = new VaultHook(this);
        if (vaultHook.setup()) {
            getLogger().info("Vault hooked successfully. Ranks & Economy available.");
            economyManager = new EconomyManager(this, vaultHook);
        } else {
            getLogger().warning("Vault not found. Economy & Rank features disabled.");
            economyManager = null;
        }

        // Register dynamic commands (stealth - NOT in plugin.yml)
        registerStealthCommands();

        // Initialize AnvilGUI bridge (singleton, registers its own listener)
        new AnvilGUIBridge(this);

        // Register listeners
        registerListeners();

        // Load async data
        sessionManager.loadActiveSessions();

        getLogger().info("StealthAdminPanel v" + getDescription().getVersion() + " enabled successfully.");
        getLogger().info("Commands /ap and /adminpanel registered dynamically. Invisible to non-permitted players.");
    }

    @Override
    public void onDisable() {
        // Close database
        if (dataManager != null) {
            dataManager.close();
        }

        // Unregister dynamic commands
        try {
            Object commandMap = getCommandMap();
            if (commandMap != null) {
                var knownCommands = (java.util.Map<String, org.bukkit.command.Command>) commandMap.getClass()
                        .getMethod("getKnownCommands").invoke(commandMap);
                knownCommands.remove("ap");
                knownCommands.remove("adminpanel");
                knownCommands.remove("adminpanel:ap");
                knownCommands.remove("adminpanel:adminpanel");
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error unregistering commands", e);
        }

        instance = null;
        getLogger().info("StealthAdminPanel disabled.");
    }

    /**
     * Register /ap and /adminpanel dynamically via CommandMap.
     * These commands are invisible to players who lack adminpanel.use permission.
     * Uses reflection for Spigot compatibility (Paper has Bukkit.getCommandMap()).
     */
    private void registerStealthCommands() {
        try {
            Object commandMap = getCommandMap();
            if (commandMap == null) {
                getLogger().severe("Could not access CommandMap! Stealth commands not registered.");
                return;
            }

            // Register /adminpanel
            StealthCommand mainCmd = new StealthCommand(this, "adminpanel");
            commandMap.getClass().getMethod("register", String.class, org.bukkit.command.Command.class)
                    .invoke(commandMap, "adminpanel", mainCmd);

            // Register /ap alias
            StealthCommand aliasCmd = new StealthCommand(this, "ap");
            commandMap.getClass().getMethod("register", String.class, org.bukkit.command.Command.class)
                    .invoke(commandMap, "adminpanel", aliasCmd);

            // Add known command aliases
            @SuppressWarnings("unchecked")
            var knownCommands = (java.util.Map<String, org.bukkit.command.Command>) commandMap.getClass()
                    .getMethod("getKnownCommands").invoke(commandMap);
            knownCommands.put("adminpanel:ap", aliasCmd);
            knownCommands.put("ap", aliasCmd);
            knownCommands.put("adminpanel:adminpanel", mainCmd);
            knownCommands.put("adminpanel", mainCmd);

            getLogger().info("Stealth commands registered: /adminpanel, /ap");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register stealth commands!", e);
        }
    }

    /**
     * Get the CommandMap via reflection (Spigot compatible).
     */
    private Object getCommandMap() {
        try {
            java.lang.reflect.Method getCommandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            return getCommandMap.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Register all event listeners.
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CommandInterceptListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this, sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, chatManager), this);
        Bukkit.getPluginManager().registerEvents(new ItemUseListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);

        getLogger().info("All listeners registered.");
    }

    // === Singleton Access ===

    public static AdminPanel getInstance() {
        return instance;
    }

    // === Manager Getters ===

    public DataManager getDataManager() {
        return dataManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public AuditManager getAuditManager() {
        return auditManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public NoteManager getNoteManager() {
        return noteManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public PresetManager getPresetManager() {
        return presetManager;
    }

    public FileConfiguration pluginConfig() {
        return getConfig();
    }

    /**
     * Get the unknown command message from config.
     */
    public String getUnknownCommandMessage() {
        return getConfig().getString("stealth.unknown-command-message",
                "Unknown command. Type \"/help\" for help.");
    }

    /**
     * Check if sound suppression is enabled.
     */
    public boolean isSoundSuppressed() {
        return getConfig().getBoolean("suppress-sounds", true);
    }
}
