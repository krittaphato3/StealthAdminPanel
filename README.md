# 🛡️ StealthAdminPanel

A **fully stealth** Minecraft admin panel plugin for Spigot/Paper servers (1.18–1.21.x) featuring invisible commands, dynamic permission-based GUI, full NBT item editor, and 35+ feature menus.

---

## ⚡ Features

### 🥷 Stealth Architecture
- **Zero commands in `plugin.yml`** — invisible to `/help`, tab-complete, and plugin scanners
- **Dynamic `CommandMap` registration** — `/ap` and `/adminpanel` injected at runtime, visible only to players with `adminpanel.use`
- **Silent failure** — unauthorized players see vanilla "Unknown command" (no permission leak)
- **Console-dispatched actions** — admin actions appear as `[Server]` in logs, hiding the admin's name
- **Sound suppression** — configurable inventory sound cancellation

### 🎮 Player Control
- View player inventory & armor with take mode
- Give items from hand to target
- **Troll options**: Smite (lightning), Slap (damage), Freeze, Fake Death, Disorient, Bounce, Lava Bath
- Set rank via Vault integration

### ⚔️ Punishment System
- **Ban**: Temporary or permanent, dispatched via console
- **Mute**: With configurable duration, tracked in database
- **Warning strikes**: Auto-ban after configurable threshold (e.g., 3 warns → temp ban)
- **Punishment history**: Paginated, searchable audit trail

### 💰 Economy
- View player balances (paginated)
- Give/take money via Vault
- Leaderboard display

### 💬 Chat Management
- **Global mute** toggle
- **Slow mode** with configurable cooldown
- **Staff chat** private channel
- **Chat filter** with regex patterns and auto-mute/warn/kick actions

### 🌍 World Configuration
- Toggle **12+ GameRules** (PVP, Mob Spawning, Keep Inventory, etc.)
- Weather control (clear/storm)
- Time control (day/night cycle)
- Green/red wool state indicators

### 🖥️ Server Management
- Whitelist toggle and player management
- Ban list (paginated, searchable, unban)
- Active players (teleport, kick)

### 🔧 Item Editor (NBT Power Tools)
- **Enchantments**: Add any enchantment at **unlimited levels** (Sharpness 1000000!)
- **Attributes**: Edit attack damage, speed, armor, luck via AnvilGUI
- **Display**: Edit name (with color codes), lore, custom model data
- **NBT Editor**: Full raw NBT tag access — add, edit, remove any tag
- **Command Binding**: Bind commands to execute on use/hit (e.g., bow that kicks on hit)
- **Unbreakable** toggle
- **Damage Override**: Custom attack damage via NBT

### 👥 Staff Coordination
- Staff online list with teleport
- Staff chat toggle
- Player notes (add/view/delete)

### 📊 Monitoring
- **Performance dashboard**: TPS, memory usage, entity counts, chunk counts
- **Session history**: Join/leave times, total playtime, last known IP
- **Alt detection**: Find accounts sharing the same IP
- **Admin audit log**: Searchable trail of all admin actions

### 📌 Extras
- **Warp system**: Create/delete/teleport to saved locations
- **Presets & Templates**: Announcement templates, ban reason presets
- **Announcements**: Custom, template, quick broadcast, and alert formats
- **Config editor**: Toggle plugin settings in-game with hot-reload

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| **Java** | 17+ |
| **Server** | Spigot / Paper 1.18 – 1.21.x |
| **Vault** | Optional (for ranks, economy, permissions) |
| **LuckPerms** | Recommended (for permission management) |

---

## 🔧 Installation

1. **Download** the latest `StealthAdminPanel-X.X.X.jar` from Releases
2. **Place** the JAR in your server's `plugins/` folder
3. **Restart** or reload your server
4. **Set permissions** via LuckPerms or your preferred permission plugin
5. **Type** `/ap` in-game (requires `adminpanel.use` permission)

---

## 🔑 Permission Nodes

| Permission | Description |
|---|---|
| `adminpanel.use` | Base command access (`/ap`, `/adminpanel`) |
| `adminpanel.troll` | Troll options (smite, slap, freeze, etc.) |
| `adminpanel.invsee` | View inventories, give items |
| `adminpanel.ranks` | Set player ranks via Vault |
| `adminpanel.world` | World configuration (GameRules, weather, time) |
| `adminpanel.server` | Server management (whitelist, ban list) |
| `adminpanel.economy` | Economy features (balances, give/take) |
| `adminpanel.punish` | Punishment system (ban, mute, warn) |
| `adminpanel.chat` | Chat management (mute, slow mode, staff chat) |
| `adminpanel.item` | Item editor (NBT, enchantments, attributes) |
| `adminpanel.staff` | Staff features (staff list, staff chat) |
| `adminpanel.warp` | Warp management |
| `adminpanel.note` | Player notes |
| `adminpanel.config` | Config editor |
| `adminpanel.log` | View audit logs |
| `adminpanel.announce` | Send announcements |
| `adminpanel.monitor` | Performance monitoring, session tracking |

**Wildcard**: `adminpanel.*` grants all permissions.

---

## 🏗️ Building from Source

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/StealthAdminPanel.git
cd StealthAdminPanel

# Build with Maven
mvn clean package

# The JAR will be in target/
ls target/StealthAdminPanel-1.0.0.jar
```

---

## 📁 Project Structure

```
StealthAdminPanel/
├── pom.xml                          # Maven build config
├── README.md
└── src/main/
    ├── resources/
    │   ├── plugin.yml               # No commands registered (stealth!)
    │   └── config.yml               # Default settings
    └── java/com/adminpanel/
        ├── AdminPanel.java          # Main plugin class
        ├── command/
        │   └── StealthCommand.java  # Dynamic command registration
        ├── gui/
        │   ├── base/                # PaginationGUI, SubMenu base classes
        │   ├── MainMenu.java        # Central hub
        │   ├── player/              # Player control (5 menus)
        │   ├── punishment/          # Ban/mute/warn (5 menus)
        │   ├── economy/             # Economy features (3 menus)
        │   ├── chat/                # Chat management (3 menus)
        │   ├── world/               # World config
        │   ├── server/              # Server management (4 menus)
        │   ├── item/                # NBT item editor (6 menus)
        │   ├── staff/               # Staff coordination (3 menus)
        │   ├── monitoring/          # Performance/audit (4 menus)
        │   ├── warp/                # Warp management
        │   ├── preset/              # Preset templates
        │   ├── announcement/        # Announcement builder
        │   └── config/              # Config editor
        ├── listener/                # Event listeners (6 files)
        ├── manager/                 # Business logic (9 files)
        ├── util/                    # Utilities (5 files)
        └── hooks/                   # Vault + AnvilGUI (2 files)
```

---

## ⚙️ Configuration

The `config.yml` is generated on first run:

```yaml
suppress-sounds: true              # Cancel inventory open sounds
announcement:
  prefix: '&6&l[Admin] &r'        # Announcement prefix
punishment:
  auto-ban-after-warns: 3          # Auto-ban after N warnings
  default-temp-ban: '7d'           # Default temp ban duration
  default-temp-mute: '30m'         # Default temp mute duration
chat:
  slow-mode-cooldown: 5            # Default slow mode (seconds)
  staff-chat-format: '&8[&bStaff&8] &e%player%&7: &f%message%'
item-editor:
  max-enchant-level: 1000000       # Max enchantment level allowed
stealth:
  unknown-command-message: 'Unknown command. Type "/help" for help.'
```

---

## 📝 Changelog

### v1.0.0 (Initial Release)
- Stealth command registration via dynamic `CommandMap`
- Silent failure with vanilla "Unknown command" message
- Permission-gated GUI rendering (no blank/locked buttons)
- Full pagination system with AnvilGUI search
- Player Control: Inventory view, give items, troll, set rank
- Punishment: Ban (temp/perm), mute, warn with auto-ban threshold
- Economy: View balances, give/take, leaderboard
- Chat: Global mute, slow mode, staff chat, regex filter
- World: 12+ GameRule toggles, weather, time
- Server: Whitelist, ban list, active players
- Item Editor: Unlimited enchantments, attributes, display, NBT, command binding
- Staff: Staff list, staff chat, player notes
- Monitoring: TPS/memory dashboard, session history, alt detection, audit log
- Warps, presets, announcements, config editor
- SQLite database for persistence
- Vault integration for ranks/economy

---

## 📜 License

MIT License — Free to use, modify, and distribute.

---

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## ⚠️ Disclaimer

This plugin is designed for **authorized server administration only**. The stealth features are intended to prevent regular players and unauthorized operators from discovering admin tools. Server owners are responsible for ensuring these tools are only used by trusted staff members.
