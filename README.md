<div align="center">

# 🛡️ StealthAdminPanel

**A fully invisible, high-performance admin panel for Minecraft servers**

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spigot](https://img.shields.io/badge/Spigot/Paper-1.18--1.21-green?style=flat-square)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-purple?style=flat-square)](https://github.com/krittaphato3/StealthAdminPanel/releases)

*35+ feature menus • Unlimited NBT editor • Zero external dependencies • 100% stealth architecture*

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Permission Nodes](#permission-nodes)
- [Configuration](#configuration)
- [Feature Modules](#feature-modules)
- [Building from Source](#building-from-source)
- [Project Architecture](#project-architecture)
- [Contributing](#contributing)
- [Changelog](#changelog)
- [License](#license)

---

## Overview

StealthAdminPanel is a Minecraft server plugin designed for **trusted server administrators** who need powerful, discreet management tools. Unlike traditional admin panels that register commands in `plugin.yml` (making them visible to OPs and tab-complete), this plugin uses **dynamic command injection** via `CommandMap` to remain completely invisible to unauthorized users.

### Design Philosophy

| Principle | Implementation |
|---|---|
| **Invisibility** | Commands not in `plugin.yml`; dynamic `CommandMap` registration with permission-gated tab-complete |
| **Silent Failures** | Unauthorized users see vanilla "Unknown command" — no permission leak |
| **Zero Dependencies** | Only Spigot API (provided by server) and SQLite JDBC; no external libraries required |
| **Permission-First** | Every GUI button checks permissions before rendering; missing permissions = button doesn't exist |
| **Stealth Logging** | All admin actions dispatched via console — logs show `[Server]`, not the admin's name |
| **Cross-Version** | Compatible with Spigot/Paper 1.18 through 1.21.x using reflection for Paper-only APIs |

---

## Key Features

### 🥷 Stealth Architecture
- **Dynamic command registration** — `/ap` and `/adminpanel` are injected at runtime, invisible to `/help` and plugin scanners
- **Permission-gated tab-complete** — returns empty list for players without `adminpanel.use`
- **Vanilla failure message** — unauthorized attempts trigger the standard Minecraft "Unknown command" response
- **Sound suppression** — configurable cancellation of inventory open sounds
- **Console dispatch** — admin actions executed through `Bukkit.dispatchCommand(consoleSender, ...)` to hide the admin's identity in server logs

### 🎮 Player Control
- View any player's inventory and armor with a **take mode** toggle
- Give items directly from your hand to the target
- **Troll suite**: Smite (lightning), Slap (damage), Freeze (movement lock), Fake Death (animation + sound), Disorient (blindness/nausea), Bounce (launch), Lava Bath (temporary)
- Set player ranks via Vault integration

### ⚔️ Punishment System
- **Temporary & permanent bans** — dispatched via console for stealth
- **Mutes** — tracked in plugin database with configurable duration
- **Warning strikes** — auto-ban after configurable threshold (e.g., 3 warns → 7-day ban)
- **Punishment history** — paginated, searchable audit trail

### 💰 Economy
- View all player balances (paginated with AnvilGUI search)
- Give/take money with amount input
- Server leaderboard display
- Full Vault Economy integration

### 💬 Chat Management
- **Global mute** toggle
- **Slow mode** with configurable per-player cooldown
- **Staff chat** — private channel only visible to staff members
- **Chat filter** — regex-based patterns with auto-mute, warn, or kick actions

### 🌍 World Configuration
- Toggle **12+ GameRules** with instant visual feedback (green/red wool indicators)
- Weather control (clear/storm)
- Time control (day/night/dawn cycle)
- GameRules include: PVP, Mob Spawning, Keep Inventory, Mob Griefing, Fire Tick, and more

### 🖥️ Server Management
- Whitelist toggle and player management (add/remove/view)
- Ban list — paginated, searchable, with unban functionality
- Active player list with quick-action buttons (teleport, kick)

### 🔧 Item Editor (NBT Power Tools)
- **Enchantments** — add any enchantment at **unlimited levels** (Sharpness 1000000, Efficiency 999, etc.)
- **Attributes** — edit attack damage, speed, armor, luck via AnvilGUI input
- **Display** — edit item name (with color codes), lore (line by line), custom model data, item flags
- **PersistentDataContainer** — add, edit, and remove custom data tags on items
- **Command Binding** — bind commands to execute on right-click, arrow hit, or melee hit (e.g., bow that kicks on hit)
- **Unbreakable** toggle, **Damage Override** via custom tags

### 👥 Staff Coordination
- Staff online list with teleport-to functionality
- Staff chat toggle (private channel)
- Player notes — add/view/delete notes on any player profile

### 📊 Monitoring & Observability
- **Performance dashboard** — TPS (via Paper reflection), memory usage, entity counts, chunk counts
- **Session history** — join/leave times, total playtime, last known IP
- **Alt detection** — find accounts sharing the same IP address
- **Admin audit log** — searchable trail of every admin action

### 📌 Additional Features
- **Warp system** — create, delete, and teleport to saved locations
- **Presets & Templates** — announcement templates, ban reason presets
- **Announcements** — custom, template-based, quick broadcast, and alert formats with color support
- **Config editor** — toggle plugin settings in-game with hot-reload

---

## Requirements

| Component | Requirement |
|---|---|
| **Java** | 17 or higher |
| **Server Software** | Spigot, Paper, or Purpur |
| **Server Version** | 1.18.x – 1.21.x |
| **Vault** | Optional — required for ranks, economy, and permissions |
| **LuckPerms** | Recommended — for permission management |
| **SQLite** | Bundled — no external database server needed |

---

## Installation

### Quick Start

1. **Download** the latest `StealthAdminPanel-1.0.0.jar` from the [Releases](https://github.com/krittaphato3/StealthAdminPanel/releases) page
2. **Place** the JAR file in your server's `plugins/` directory
3. **Restart** your server (do not use `/reload` — dynamic commands require a full restart)
4. **Set permissions** using LuckPerms or your preferred permission plugin:

```
/lp user <player> permission set adminpanel.use true
/lp user <player> permission set adminpanel.* true
```

5. **Type** `/ap` in-game — the admin panel GUI will open

### Post-Installation Checklist

- [ ] Verify the plugin loads without errors in `latest.log`
- [ ] Confirm `/ap` opens the GUI for authorized players
- [ ] Confirm `/ap` shows "Unknown command" for unauthorized players
- [ ] Set up permission groups for different staff tiers
- [ ] Configure `config.yml` to match your server's needs
- [ ] (Optional) Install Vault for economy/rank features

---

## Permission Nodes

### Base Permission

| Permission | Description |
|---|---|
| `adminpanel.use` | Access to `/ap` and `/adminpanel` commands |

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

### Wildcard

```
adminpanel.*    # Grants all permissions
```

### Recommended Permission Groups

**Tier 1 — Helper**
```
adminpanel.use
adminpanel.invsee
adminpanel.chat
adminpanel.monitor
```

**Tier 2 — Moderator**
```
adminpanel.use
adminpanel.invsee
adminpanel.troll
adminpanel.punish
adminpanel.chat
adminpanel.server
adminpanel.monitor
adminpanel.note
adminpanel.warp
```

**Tier 3 — Admin**
```
adminpanel.*
```

---

## Configuration

The `config.yml` is generated on first server start. Edit it in `plugins/StealthAdminPanel/config.yml`:

```yaml
# ===========================
#  StealthAdminPanel Config
# ===========================

# Suppress inventory open sounds for stealth
suppress-sounds: true

# Announcement settings
announcement:
  prefix: '&6&l[Admin] &r'
  format: '%prefix% %message%'

# Punishment settings
punishment:
  # Number of warnings before auto-temp-ban (0 = disabled)
  auto-ban-after-warns: 3
  # Default temp ban duration
  default-temp-ban: '7d'
  # Default temp mute duration
  default-temp-mute: '30m'

# Chat management
chat:
  # Default slow mode cooldown in seconds (0 = disabled)
  slow-mode-cooldown: 5
  # Staff chat format
  staff-chat-format: '&8[&bStaff&8] &e%player%&7: &f%message%'

# Item editor defaults
item-editor:
  # Maximum enchantment level allowed
  max-enchant-level: 1000000

# Stealth settings
stealth:
  # Vanilla "unknown command" message
  unknown-command-message: 'Unknown command. Type "/help" for help.'
```

### Color Code Support

All text fields support:
- **Standard codes**: `&0` – `&f`, `&k` – `&o`
- **Hex colors**: `&#RRGGBB` (e.g., `&#FF5555` for red)

---

## Feature Modules

<details>
<summary><strong>🎮 Player Control</strong></summary>

- **Player List** — Paginated list of all online players with heads, ping, health, and world
- **Player Actions** — Sub-menu for any selected player
- **Inventory View** — Full view of player's inventory, armor, and offhand with a take mode toggle
- **Give Items** — Hold an item and click to give a custom amount via AnvilGUI input
- **Troll Options** — Smite, Slap, Freeze, Fake Death, Disorient, Bounce, Lava Bath
- **Set Rank** — Change player rank via Vault with AnvilGUI input

</details>

<details>
<summary><strong>⚔️ Punishment System</strong></summary>

- **Ban** — Temporary or permanent with reason and duration input
- **Mute** — Chat muting tracked in SQLite with duration
- **Warn** — Warning strikes with auto-ban threshold (configurable)
- **History** — Paginated view of all past punishments with type, reason, issuer, and date

</details>

<details>
<summary><strong>🔧 Item Editor</strong></summary>

- **Enchantments** — Add any enchantment at unlimited levels (Sharpness 1000000)
- **Attributes** — Edit attack damage, speed, armor, luck, and more
- **Display** — Edit name with color codes, lore line by line, custom model data
- **Data Tags** — Add, edit, remove PersistentDataContainer tags
- **Command Binding** — Bind commands to execute on use, hit, or projectile impact
- **Unbreakable** — Toggle unbreakable status

</details>

<details>
<summary><strong>📊 Monitoring</strong></summary>

- **Performance Dashboard** — TPS (Paper), memory usage, entity counts, chunk counts, JVM info
- **Session History** — Join/leave times, total playtime, last known IP per player
- **Alt Detection** — Find accounts sharing the same IP address
- **Audit Log** — Searchable trail of every admin action with timestamps

</details>

---

## Building from Source

### Prerequisites

- Java 17 or higher
- Git

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/krittaphato3/StealthAdminPanel.git
cd StealthAdminPanel

# 2. Build with Maven wrapper (no Maven install needed)
./mvnw clean package

# 3. The JAR is in target/
ls target/StealthAdminPanel-1.0.0.jar
```

### Windows

```cmd
git clone https://github.com/krittaphato3/StealthAdminPanel.git
cd StealthAdminPanel
mvnw.cmd clean package
```

The built JAR in `target/` can be dropped directly into any Spigot/Paper 1.18+ server's `plugins/` folder.

---

## Project Architecture

```
StealthAdminPanel/
├── pom.xml                              # Maven build configuration
├── mvnw / mvnw.cmd                      # Maven wrapper (no install needed)
├── .mvn/wrapper/                         # Maven wrapper JAR + properties
├── README.md
│
└── src/main/
    ├── resources/
    │   ├── plugin.yml                    # ⚠️ NO commands registered (stealth!)
    │   └── config.yml                    # Default plugin configuration
    │
    └── java/com/adminpanel/
        ├── AdminPanel.java               # Main plugin class, lifecycle management
        │
        ├── command/
        │   └── StealthCommand.java       # Dynamic CommandMap registration
        │
        ├── gui/
        │   ├── base/
        │   │   ├── PaginationGUI.java    # Abstract paginated inventory holder
        │   │   └── SubMenu.java          # Abstract single-page menu base
        │   ├── MainMenu.java             # Central hub — routes to all modules
        │   ├── player/                   # Player control (5 menus)
        │   ├── punishment/               # Ban/mute/warn (5 menus)
        │   ├── economy/                  # Economy features (3 menus)
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
        ├── listener/
        │   ├── CommandInterceptListener.java   # Silent failure interceptor
        │   ├── GUIClickListener.java           # Central click router
        │   ├── PlayerSessionListener.java      # Join/leave tracking
        │   ├── ChatListener.java               # Mute/slow-mode/filter
        │   ├── ItemUseListener.java            # Command binding execution
        │   └── DamageListener.java             # Custom damage override
        │
        ├── manager/
        │   ├── PermissionManager.java    # Centralized permission constants
        │   ├── DataManager.java          # SQLite database operations
        │   ├── PunishmentManager.java    # Ban/mute/warn business logic
        │   ├── SessionManager.java       # Player session + alt tracking
        │   ├── AuditManager.java         # Admin action logging
        │   ├── ChatManager.java          # Mute/slow-mode state
        │   ├── WarpManager.java          # Warp CRUD
        │   ├── NoteManager.java          # Player notes CRUD
        │   ├── EconomyManager.java       # Vault economy wrapper
        │   └── PresetManager.java        # Template/preset CRUD
        │
        ├── util/
        │   ├── ItemBuilder.java          # Fluent ItemStack builder
        │   ├── TextUtil.java             # Color codes, hex, placeholders
        │   ├── HeadUtil.java             # Player head texture loading
        │   ├── DurationParser.java       # "1h30m" → milliseconds
        │   └── ColorUtil.java            # RGB gradients, rainbow text
        │
        └── hooks/
            ├── VaultHook.java            # Vault API provider setup
            └── AnvilGUIBridge.java       # Lightweight AnvilGUI wrapper
```

### Data Flow

```
Player types /ap
       │
       ▼
CommandInterceptListener (LOWEST priority)
       │
       ├── Has permission? → StealthCommand.execute() → MainMenu.open()
       │
       └── No permission? → Cancel event → Send vanilla "Unknown command"
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

## Contributing

Contributions are welcome! Please follow these guidelines:

### Getting Started

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Test on a local Paper/Spigot server
5. Commit: `git commit -m 'feat: add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix | Usage |
|---|---|
| `feat:` | New feature |
| `fix:` | Bug fix |
| `refactor:` | Code restructuring without behavior change |
| `docs:` | Documentation changes |
| `style:` | Formatting, no code change |
| `test:` | Adding or updating tests |
| `chore:` | Build process, dependencies |

### Code Style

- Java 17+ features (pattern matching, records, text blocks)
- Consistent with existing code patterns
- Javadoc on public methods
- Permission checks via `PermissionManager` constants

---

## Changelog

### v1.0.0 — Initial Release (2026-06-26)

**Core**
- Stealth command registration via dynamic `CommandMap`
- Silent failure with vanilla "Unknown command" message
- Permission-gated GUI rendering (no blank/locked buttons)
- Full pagination system with AnvilGUI search
- SQLite database for persistent storage
- Zero external compile dependencies

**Modules**
- Player Control: Inventory view, give items, troll (7 options), set rank
- Punishment: Ban (temp/perm), mute, warn with auto-ban threshold, history
- Economy: View balances, give/take, leaderboard (Vault integration)
- Chat: Global mute, slow mode, staff chat, regex filter
- World: 12+ GameRule toggles, weather, time control
- Server: Whitelist, ban list, active players (kick/tp)
- Item Editor: Unlimited enchantments, attributes, display, PersistentDataContainer, command binding
- Staff: Staff list, staff chat, player notes
- Monitoring: TPS/memory dashboard, session history, alt detection, audit log
- Warps, presets, announcements, config editor

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## Disclaimer

This plugin is designed for **authorized server administration only**. The stealth features are intended to prevent regular players and unauthorized operators from discovering admin tools. Server owners are responsible for ensuring these tools are used exclusively by trusted staff members. The developers assume no responsibility for misuse.

---

<div align="center">

**Built with ❤️ for the Minecraft server administration community**

[Report Bug](https://github.com/krittaphato3/StealthAdminPanel/issues) • [Request Feature](https://github.com/krittaphato3/StealthAdminPanel/issues) • [Releases](https://github.com/krittaphato3/StealthAdminPanel/releases)

</div>
