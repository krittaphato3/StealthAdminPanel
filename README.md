<div align="center">

# StealthAdminPanel

**A fully invisible, high-performance admin panel for Minecraft servers**

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spigot](https://img.shields.io/badge/Spigot/Paper-1.18--1.21-green?style=flat-square)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-purple?style=flat-square)](https://github.com/krittaphato3/StealthAdminPanel/releases)

*35+ feature menus -- Sub-command shortcuts -- Unlimited NBT editor -- Zero external dependencies*

</div>

---

## Table of Contents

- [Overview](#overview)
- [Design Philosophy](#design-philosophy)
- [Key Features](#key-features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Sub-Command Shortcuts](#sub-command-shortcuts)
- [Permission Nodes](#permission-nodes)
- [Configuration](#configuration)
- [Building from Source](#building-from-source)
- [Project Architecture](#project-architecture)
- [Changelog](#changelog)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

StealthAdminPanel is a Minecraft server plugin designed for trusted server administrators who need powerful, discreet management tools. Unlike traditional admin panels that register commands in `plugin.yml` (making them visible to OPs and tab-complete), this plugin uses dynamic command injection via `CommandMap` to remain completely invisible to unauthorized users.

**Key highlights:**
- 35+ GUI menus across 12 feature modules
- Sub-command shortcuts for quick actions without opening the GUI
- Full NBT/Item editor with unlimited enchantment levels
- SQLite audit trail and persistent storage
- Sound effects on all interactions
- Confirmation dialogs for destructive actions
- Auto-refreshing performance dashboard

---

## Design Philosophy

| Principle | Implementation |
|---|---|
| **Invisibility** | Commands not in `plugin.yml`; dynamic `CommandMap` registration with permission-gated tab-complete |
| **Silent Failures** | Unauthorized users see vanilla "Unknown command" -- no permission leak |
| **Zero Dependencies** | Only Spigot API (provided by server) and SQLite JDBC; no external libraries required |
| **Permission-First** | Every GUI button checks permissions before rendering; missing permissions means the button does not exist |
| **Stealth Logging** | All admin actions dispatched via console -- logs show `[Server]`, not the admin's name |
| **Cross-Version** | Compatible with Spigot/Paper/Purpur 1.18 through 1.21.x |

---

## Key Features

### Stealth Architecture
- Dynamic `CommandMap` registration -- `/ap` and `/adminpanel` invisible to `/help` and plugin scanners
- Tab-complete returns empty list for unauthorized players
- Vanilla "Unknown command" response for unauthorized attempts
- Configurable inventory sound suppression
- Console-dispatched actions hide the admin identity in server logs

### Sub-Command Shortcuts
- 15+ shortcuts for common actions without opening the GUI
- Full tab-completion for all commands and arguments
- Examples: `/ap unbreakable all`, `/ap ench sharp 100`, `/ap god`, `/ap tp Steve`

### Player Control
- View player inventory with take mode toggle
- Give items from hand to target
- Troll suite: smite, slap, freeze, fake death, disorient, bounce, lava bath
- Set player rank via Vault integration

### Punishment System
- Temporary and permanent bans (dispatched via console for stealth)
- Mutes with configurable duration tracked in database
- Warning strikes with auto-ban threshold
- Punishment history with pagination and search

### Economy
- View all player balances (paginated with search)
- Give/take money via Vault
- Server leaderboard display

### Chat Management
- Global mute toggle
- Slow mode with configurable cooldown
- Staff-only chat channel
- Regex-based chat filter with auto-mute/warn/kick actions

### World Configuration
- Toggle 12+ GameRules with instant visual feedback
- Weather control (clear/storm)
- Time control (day/night/dawn cycle)
- Green/red wool indicators for boolean states

### Server Management
- Whitelist toggle and player management
- Ban list with unban functionality (paginated, searchable)
- Active player list with teleport/kick

### Item Editor (NBT Power Tools)
- Enchantments at unlimited levels (Sharpness 1000000, Efficiency 999, etc.)
- Attributes: attack damage, speed, armor, luck
- Display: name with color codes, lore, custom model data, item flags
- PersistentDataContainer for custom data tags
- Command binding: execute commands on use, hit, or projectile impact
- Unbreakable toggle and damage override

### Staff Coordination
- Staff online list with teleport-to
- Staff chat toggle
- Player notes (add, view, delete)

### Monitoring
- Performance dashboard: TPS, memory, entity counts, chunk counts (auto-refreshes every 3 seconds)
- Session history: join/leave times, total playtime, last known IP
- Alt detection: find accounts sharing the same IP
- Admin audit log: searchable trail of every admin action

### Extras
- Warp system: create, delete, teleport to saved locations
- Presets and templates: announcement templates, ban reason presets
- Announcements: custom, template, quick broadcast, and alert formats
- Config editor: toggle plugin settings in-game with hot-reload
- Confirmation dialogs for destructive actions
- Sound effects on all GUI interactions

---

## Requirements

| Component | Requirement |
|---|---|
| Java | 17 or higher |
| Server Software | Spigot, Paper, or Purpur |
| Server Version | 1.18.x -- 1.21.x |
| Vault | Optional (required for ranks, economy, permissions) |
| LuckPerms | Recommended (for permission management) |
| SQLite | Bundled (no external database server needed) |

---

## Installation

1. Download the latest `StealthAdminPanel-1.0.0.jar` from [Releases](https://github.com/krittaphato3/StealthAdminPanel/releases)
2. Place the JAR in your server's `plugins/` directory
3. Restart your server (do not use `/reload` -- dynamic commands require a full restart)
4. Set permissions using LuckPerms or your preferred permission plugin:
```
/lp user <player> permission set adminpanel.use true
/lp user <player> permission set adminpanel.* true
```
5. Type `/ap` in-game to open the admin panel

**Post-installation checklist:**
- Verify the plugin loads without errors in `latest.log`
- Confirm `/ap` opens the GUI for authorized players
- Confirm `/ap` shows "Unknown command" for unauthorized players
- Set up permission groups for different staff tiers
- Configure `config.yml` to match your server's needs
- (Optional) Install Vault for economy/rank features

---

## Sub-Command Shortcuts

Common actions can be performed directly from chat without opening the GUI.

| Command | Description |
|---|---|
| `/ap` | Open the admin panel GUI |
| `/ap unbreakable` | Toggle unbreakable on held item |
| `/ap unbreakable all` | Make all inventory items unbreakable |
| `/ap repair` | Repair held item to full durability |
| `/ap ench <name> <level>` | Enchant held item (fuzzy name match) |
| `/ap name <name>` | Rename held item (supports `&`-color codes) |
| `/ap gm <mode>` | Change gamemode (survival, creative, adventure, spectator) |
| `/ap heal [player]` | Heal and feed a player (defaults to self) |
| `/ap feed [player]` | Feed a player (defaults to self) |
| `/ap fly` | Toggle flight |
| `/ap speed <level>` | Set walk speed (0.1 to 10) |
| `/ap clear` | Clear entire inventory and armor |
| `/ap god` | Toggle god mode (no damage, auto-heal) |
| `/ap tp <player>` | Teleport to a player |
| `/ap head <player>` | Get a player's head item |
| `/ap anvil` | Open an anvil |

**Examples:**
```
/ap ench sharp 100        -- Adds Sharpness 100 to held item
/ap ench efficiency 999   -- Adds Efficiency 999 to held tool
/ap name &6&lGod Sword   -- Renames with gold color and bold
/ap unbreakable all       -- Makes all 36 inventory items unbreakable
/ap god                   -- Toggles god mode on/off
/ap tp Steve              -- Teleports to Steve
```

All sub-commands include full tab-completion for command names, enchantment names, player names, and argument values.

---

## Permission Nodes

### Base Permission

| Permission | Description |
|---|---|
| `adminpanel.use` | Access to `/ap` and `/adminpanel` commands, including all sub-command shortcuts |

### Feature Permissions

| Permission | Description |
|---|---|
| `adminpanel.troll` | Troll options (smite, slap, freeze, fake death, etc.) |
| `adminpanel.invsee` | View player inventories and give items |
| `adminpanel.ranks` | Set player ranks via Vault |
| `adminpanel.world` | World configuration (GameRules, weather, time) |
| `adminpanel.server` | Server management (whitelist, ban list, active players) |
| `adminpanel.economy` | Economy features (view balances, give/take money, leaderboard) |
| `adminpanel.punish` | Punishment system (ban, mute, warn) |
| `adminpanel.chat` | Chat management (global mute, slow mode, staff chat, filter) |
| `adminpanel.item` | Item editor (enchantments, attributes, NBT, command binding) |
| `adminpanel.staff` | Staff features (staff list, staff chat) |
| `adminpanel.warp` | Warp management (create, delete, teleport) |
| `adminpanel.note` | Player notes (add, view, delete) |
| `adminpanel.config` | Config editor (toggle settings, hot-reload) |
| `adminpanel.log` | View admin audit logs |
| `adminpanel.announce` | Send announcements |
| `adminpanel.monitor` | Performance monitoring, session tracking, alt detection |

**Wildcard:** `adminpanel.*` grants all permissions.

### Recommended Permission Groups

**Helper:**
```
adminpanel.use, adminpanel.invsee, adminpanel.chat, adminpanel.monitor
```

**Moderator:**
```
adminpanel.use, adminpanel.invsee, adminpanel.troll, adminpanel.punish,
adminpanel.chat, adminpanel.server, adminpanel.monitor, adminpanel.note, adminpanel.warp
```

**Admin:**
```
adminpanel.*
```

---

## Configuration

The `config.yml` is generated on first server start in `plugins/StealthAdminPanel/config.yml`:

```yaml
# Suppress inventory open sounds for stealth
suppress-sounds: true

# Announcement settings
announcement:
  prefix: '&6&l[Admin] &r'

# Punishment settings
punishment:
  auto-ban-after-warns: 3      # Warnings before auto-temp-ban (0 = disabled)
  default-temp-ban: '7d'
  default-temp-mute: '30m'

# Chat management
chat:
  slow-mode-cooldown: 5        # Default slow mode in seconds (0 = disabled)
  staff-chat-format: '&8[&bStaff&8] &e%player%&7: &f%message%'

# Item editor
item-editor:
  max-enchant-level: 1000000

# Stealth
stealth:
  unknown-command-message: 'Unknown command. Type "/help" for help.'
```

All settings can be edited in-game via the Config Editor menu (`/ap` -> Config Editor). Changes are saved to disk immediately.

---

## Building from Source

**Prerequisites:** Java 17+, Git

```bash
# Clone the repository
git clone https://github.com/krittaphato3/StealthAdminPanel.git
cd StealthAdminPanel

# Build (Maven wrapper included -- no Maven install needed)
./mvnw clean package

# Output JAR
ls target/StealthAdminPanel-1.0.0.jar
```

On Windows:
```cmd
git clone https://github.com/krittaphato3/StealthAdminPanel.git
cd StealthAdminPanel
mvnw.cmd clean package
```

---

## Project Architecture

```
StealthAdminPanel/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
│
└── src/main/
    ├── resources/
    │   ├── plugin.yml                    # No commands registered (stealth)
    │   └── config.yml
    │
    └── java/com/adminpanel/
        ├── AdminPanel.java               # Main plugin class
        │
        ├── command/
        │   └── StealthCommand.java       # Dynamic registration + sub-commands
        │
        ├── gui/
        │   ├── base/
        │   │   ├── PaginationGUI.java    # Paginated inventory base
        │   │   ├── SubMenu.java          # Single-page menu base
        │   │   └── ConfirmDialog.java    # Reusable confirmation dialog
        │   ├── MainMenu.java             # Central hub
        │   ├── player/                   # Player control (5 menus)
        │   ├── punishment/               # Ban/mute/warn (5 menus)
        │   ├── economy/                  # Economy (3 menus)
        │   ├── chat/                     # Chat management (3 menus)
        │   ├── world/                    # World configuration
        │   ├── server/                   # Server management (4 menus)
        │   ├── item/                     # NBT item editor (6 menus)
        │   ├── staff/                    # Staff coordination (3 menus)
        │   ├── monitoring/               # Performance/audit (4 menus)
        │   ├── warp/                     # Warp management
        │   ├── preset/                   # Preset templates
        │   ├── announcement/             # Announcement builder
        │   └── config/                   # Config editor
        │
        ├── listener/                     # Event listeners (6 files)
        ├── manager/                      # Business logic (10 files)
        ├── util/                         # Utilities (6 files)
        └── hooks/                        # Vault + AnvilGUI (2 files)
```

### Data Flow

```
Player types /ap
       |
       v
CommandInterceptListener (LOWEST priority)
       |
       +-- Has permission? --> StealthCommand.execute() --> MainMenu.open()
       |
       +-- No permission? --> Cancel event --> Vanilla "Unknown command"
```

### Database Schema (SQLite)

| Table | Purpose |
|---|---|
| `punishments` | Ban/mute/warn records with expiry tracking |
| `audit_log` | All admin actions with timestamps |
| `player_notes` | Notes attached to player profiles |
| `sessions` | Join/leave records for session tracking |
| `warps` | Saved teleport locations |
| `presets` | Announcement and ban reason templates |
| `chat_filter` | Regex patterns for auto-moderation |

---

## Changelog

### v1.0.0 -- Initial Release

**Core:**
- Stealth command registration via dynamic `CommandMap`
- Silent failure with vanilla "Unknown command" message
- Permission-gated GUI rendering
- Full pagination system with chat-based search
- SQLite database for persistent storage
- Zero external compile dependencies
- Sound effects on all interactions
- Confirmation dialogs for destructive actions

**Sub-Command Shortcuts:**
- `/ap unbreakable`, `/ap repair`, `/ap ench`, `/ap name`
- `/ap gm`, `/ap heal`, `/ap feed`, `/ap fly`, `/ap speed`
- `/ap clear`, `/ap god`, `/ap tp`, `/ap head`, `/ap anvil`
- Full tab-completion for all shortcuts

**Modules:**
- Player Control: inventory view, give items, troll (7 options), set rank
- Punishment: ban (temp/perm), mute, warn with auto-ban threshold, history
- Economy: view balances, give/take, leaderboard (Vault integration)
- Chat: global mute, slow mode, staff chat, regex filter
- World: 12+ GameRule toggles, weather, time control
- Server: whitelist, ban list, active players (kick/tp)
- Item Editor: unlimited enchantments, attributes, display, PersistentDataContainer, command binding
- Staff: staff list, staff chat, player notes
- Monitoring: auto-refreshing TPS/memory dashboard, session history, alt detection, audit log
- Warps, presets, announcements, config editor

---

## License

This project is licensed under the MIT License.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit using conventional commits: `git commit -m "feat: add my feature"`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

**Commit convention:** `feat:` for features, `fix:` for bugs, `refactor:` for restructuring, `docs:` for documentation.

---

## Disclaimer

This plugin is designed for authorized server administration only. The stealth features are intended to prevent regular players and unauthorized operators from discovering admin tools. Server owners are responsible for ensuring these tools are used exclusively by trusted staff members.

---

<div align="center">

**Built for the Minecraft server administration community**

[Report Bug](https://github.com/krittaphato3/StealthAdminPanel/issues) | [Request Feature](https://github.com/krittaphato3/StealthAdminPanel/issues) | [Releases](https://github.com/krittaphato3/StealthAdminPanel/releases)

</div>
