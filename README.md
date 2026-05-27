# Petly — Server Admin Guide

A feature-rich pet collection and progression system for **Paper 26.1.2**.  
Players collect pets through a gacha summon system, level them up, send them on field missions, and display them orbiting around their character.

---

## Requirements

| Dependency | Required | Notes |
|---|---|---|
| Paper | ✅ | 26.1.2 or newer |
| Java | ✅ | 25+ |
| PlaceholderAPI | ⬜ Optional | Enables `%petly_*%` placeholders |
| Vault | ⬜ Optional | Economy hook (future use) |

---

## Installation

1. Drop `Petly-1.0.0.jar` into your `plugins/` folder.
2. Start the server once — all config files are generated automatically under `plugins/Petly/`.
3. Edit the generated YAML files to your liking (see [Configuration](#configuration)).
4. Run `/petly reload` to apply changes without restarting.

---

## Configuration

All configuration lives in `plugins/Petly/`. **Never edit files while the server is running** unless you reload afterwards.

| File | Purpose |
|---|---|
| `config.yml` | Core settings: summon costs, leveling, pity, dust chamber timers, pet display |
| `messages.yml` | Every player-facing message (MiniMessage format) |
| `pets.yml` | All 50 pet definitions — name, rarity, power stats, skin texture |
| `missions.yml` | All 100 field missions — power requirements, duration, rewards |
| `guis.yml` | GUI layout: slot positions, titles, filler materials |
| `summon-rates.yml` | Gacha rates, pity counters, star/ascension costs, milestone rewards |
| `towers.yml` | The Tower settings: floor scaling formula, max floors, Pet XP per floor, battle duration |
| `playerdata/` | One YAML file per player UUID — do not edit manually |

### Key `towers.yml` values

```yaml
formula:
  base-recommended-power: 200   # Recommended power for Floor 1
  power-per-floor: 200          # Power increase per floor
  base-dust-reward: 50          # Dust reward for Floor 1
  dust-per-floor: 20            # Dust increase per floor

max-floors: 500                 # Total number of floors in the tower
pet-xp-per-floor: 50           # Pet XP awarded to each team member per cleared floor
battle-duration-ticks: 100     # How long the battle animation runs (100 = 5 seconds)
```

Floor N formula: `recommended-power = base-recommended-power + (N − 1) × power-per-floor`

### Key `config.yml` values

```yaml
summon:
  cost-1: 500        # Dust cost for a single summon
  cost-3: 1400       # Dust cost for a 3-pull
  cost-6: 2600       # Dust cost for a 6-pull

leveling:
  xp-base: 100           # XP needed at level 1
  xp-scaling-factor: 1.15 # Multiplier per level
  dust-per-1000xp: 10    # Dust cost to buy 1,000 XP
  max-level: 100

dust-chamber:
  interval-ticks: 6000   # How often the chamber generates dust (6000 = 5 min)
  base-rate: 2           # Base dust per cycle per pet slot
  star-multiplier: 0.5   # Extra dust per star (rate * (1 + stars * multiplier))

pity:
  guaranteed-sr-after: 50    # Guaranteed SR+ at this many summons without one
  guaranteed-smr-after: 200
  guaranteed-ur-after: 500

pet-display:
  orbit-radius: 1.5       # How far pets orbit from the player
  height-offset: 0.2      # Vertical offset of the armor-stand
  update-interval-ticks: 2
```

### Adding or editing pets (`pets.yml`)

```yaml
my_dragon:
  display-name: "ᴅʀᴀɢᴏɴ"   # Small-caps unicode recommended
  rarity: SR                 # N, R, SR, SMR, or UR
  base-power: 1200
  power-per-level: 18
  power-per-star: 300
  ascension-base-bonus: 120
  ascension-scaling-bonus: 5
  skin-texture: "<base64 texture string>"
  lore: "A mighty dragon from the ancient world."
```

Replace `skin-texture` with a real base64-encoded Minecraft skull texture value. The format is the same string used by other skull plugins. Any value starting with `PLACEHOLDER` is shown as a plain player head in-game.

### Adding missions (`missions.yml`)

```yaml
101:
  name: "ꜱᴛᴏʀᴍ ʀɪᴅɢᴇ"
  name-gradient: "<gradient:#60a5fa:#38bdf8>"
  lore: "Brave the storm atop the ridge."
  recommended-power: 20000
  duration-ticks: 72000       # 72000 ticks = 1 hour
  dust-reward: 5000
  pet-xp-reward: 300
  pet-drop-chance: 0.04       # 4% chance to receive a bonus pet
```

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `petly.admin` | OP | Full access to all admin commands. Inherits `petly.player`, `petly.summon.3`, and `petly.summon.6`. |
| `petly.player` | Everyone | Access to all player-facing commands and GUIs. |
| `petly.summon.3` | false | Unlocks the ×3 summon button in the summon GUI. |
| `petly.summon.6` | false | Unlocks the ×6 summon button in the summon GUI. |

`petly.admin` (i.e. OPs) automatically has all summon tiers — no extra grants needed.

Grant summon permissions to non-admin ranks via your permissions plugin:

```
/lp group vip permission set petly.summon.3 true
/lp group premium permission set petly.summon.6 true
```

---

## Commands

### Player Commands

| Command | Aliases | Description |
|---|---|---|
| `/menu` | `/manager` | Opens the main Petly hub menu. |
| `/pets` | — | Opens your personal pet storage. |
| `/summon` | `/petsummon`, `/gacha` | Opens the gacha summon interface. |
| `/missions` | `/fieldmissions` | Opens the field missions screen. |
| `/chamber` | `/dustchamber` | Opens the dust chamber. |
| `/collection` | — | Opens the full pet collection (shows all pets, owned or not). |
| `/tower` | — | Opens The Tower — 500-floor battle progression system. |
| `/leaderboard` | `/lb` | Opens the Leaderboard GUI (5 categories). |

### Admin Command — `/petly`

Permission: `petly.admin`

| Sub-command | Usage | Description |
|---|---|---|
| `reload` | `/petly reload` | Reloads all config files without a server restart. |
| `pet give` | `/petly pet give <player> <petId>` | Gives a player a pet by its config ID. |
| `pet take` | `/petly pet take <player> <petId>` | Removes one instance of a pet from a player. |
| `pet nickname` | `/petly pet nickname <petUUID> <name\|clear>` | Sets or clears a pet's nickname (in-game only). |
| `dust give` | `/petly dust give <player> <amount>` | Gives a player a dust amount. |
| `dust take` | `/petly dust take <player> <amount>` | Removes dust from a player. |
| `petlevel set` | `/petly petlevel set <player> <petUUID> <level>` | Sets a specific pet's level. |
| `petlevel add` | `/petly petlevel add <player> <petUUID> <amount>` | Adds levels to a specific pet. |
| `setpower` | `/petly setpower <player> <petUUID> <stars> <asc>` | Force-sets a pet's star and ascension values. |
| `reset` | `/petly reset <player> <type>` | Resets part of or all data for an online player. |

**Reset types:**

| Type | What it clears |
|---|---|
| `all` | Everything — dust, pets, pity counters, missions, tower progress |
| `dust` | Dust balance only |
| `pets` | All owned pets, team, and chamber slots |
| `petxp` | Resets XP to 0 on every pet (levels unchanged) |
| `petlevel` | Resets every pet to level 1 with 0 XP |
| `thetower` | Resets highest cleared floor to 0 |
| `fieldmissions` | Resets missions-completed counter and mission log |

`<petId>` is the key used in `pets.yml` (e.g. `my_dragon`).  
`<petUUID>` is the unique instance ID of an owned pet (visible in the pet detail GUI or via the API).  
`reset` requires the target player to be online.

---

## Gamemode Overview

### Dust — The Currency

**Dust** (✦) is the sole in-game currency. Players earn it by:

- Completing **field missions**
- Leaving pets in the **dust chamber**
- Receiving it from admins via `/petly dust give`

Dust is spent on summons, buying XP for pets, starring up, and ascending.

---

### Summoning (Gacha)

Open with `/summon`. Players spend dust to roll for pets:

| Pull | Default Cost |
|---|---|
| ×1 | 500 ✦ |
| ×3 | 1,400 ✦ (requires `petly.summon.3`) |
| ×6 | 2,600 ✦ (requires `petly.summon.6`) |

The summon buttons are **locked during the 5-second animation** — clicking again before the animation ends has no effect.

**Rarity rates** (configurable in `summon-rates.yml`):

| Rarity | Symbol | Default Rate |
|---|---|---|
| Normal | ɴ | 50% |
| Rare | ʀ | 30% |
| Super Rare | ꜱʀ | 14% |
| Super Mega Rare | ꜱᴍʀ | 5% |
| Ultra Rare | ᴜʀ | 1% |

**Pity system:** Guaranteed rate-ups at 50 (SR+), 200 (SMR+), and 500 (UR) summons without obtaining the respective rarity.

**Pet Luck:** A personal multiplier that boosts SR+/SMR/UR rates the longer a player goes without pulling an SR. It resets to 1.0× on any SR+ pull.

---

### Pet Progression

Each owned pet has independent stats:

| Stat | Max | How to increase |
|---|---|---|
| Level | 100 | Buy 1,000 XP chunks with dust in the Pet Detail GUI, or complete missions |
| Stars | 5★ | Star Up: requires Level 100 + one duplicate of the same pet |
| Ascension | 10 | Ascend: requires Level 100 + 5★, costs escalating amounts of dust |

Higher stars and ascension increase a pet's **power** according to:

```
power = (basePower + ascBaseBonus × asc)
      + (powerPerLevel + ascScalingBonus × asc) × (level − 1)
      + (powerPerStar + 200 × asc) × stars
```

---

### Team

Players build a team of up to **5 pets** from their storage. The team's combined power determines mission success rates and is shown on leaderboards via PlaceholderAPI.

**Team Rarity Bonus:** If all 5 team slots are filled with pets of the *same* rarity, a percentage bonus is applied to total team power (configurable per rarity in `summon-rates.yml`).

---

### Field Missions

Open with `/missions`. Players send their team on timed missions:

- **Requires at least one pet in the active team** — players with an empty team cannot start a mission.
- Only **one mission** can be active at a time.
- Missions complete in real time — even while the player is offline.
- Success chance is determined by a sigmoid curve based on team power vs. recommended power.  
  At 100% recommended power the chance is ~90%; at 120%+ it is capped at 100%.
- **Rewards on success:** Dust, Pet XP distributed to all team members, and a configurable chance to drop a bonus pet.
- **Daily Bonus:** The first mission completed each day awards **2× dust**.
- **Offline resolution:** If a mission finishes while the player is offline, results are shown on their next login.

**Milestone rewards:** At missions 10, 25, 50, 75, and 100, players receive a free pet of increasing rarity as a reward.

**Milestone broadcasts:** Every 10th mission completed (10, 20, 30 …) is announced server-wide.

---

### Dust Chamber

Open with `/chamber`. Up to **3 pets** can be placed in the chamber to passively generate dust over time.

Generation per cycle per slot:

```
rate = baseRate × (1 + stars × starMultiplier)
```

The cycle timer is configurable (`dust-chamber.interval-ticks` in `config.yml`). Players must manually click **Collect** in the chamber GUI to claim pending dust.

---

### The Tower

Open with `/tower`. A 500-floor solo battle system where players fight their way up floor by floor:

- **Requires at least one pet in the active team** to start a battle.
- Floors are unlocked sequentially — a player must clear Floor N before attempting Floor N+1.
- Each floor has a recommended power and a 5-second instant battle. Success chance follows the same sigmoid curve as Field Missions.
- **Rewards per cleared floor:** Dust (scales with floor number) + Pet XP awarded to every pet currently in the active team.
- Floor lore in the GUI shows the exact dust and Pet XP reward before entering.

**Scaling formula** (configurable in `towers.yml`):

```
Recommended Power = base-recommended-power + (floor − 1) × power-per-floor
Dust Reward       = base-dust-reward       + (floor − 1) × dust-per-floor
```

**Milestone broadcasts:** Every 10 floors cleared (10, 20, 30 …) is announced server-wide.

---

### Leaderboard

Open with `/leaderboard` or `/lb`, or via the leaderboard button in `/menu`. Shows the top 9 players across 5 categories:

| Category | Ranked by |
|---|---|
| ⚡ Team Power | Total combined power of the active 5-pet team |
| ⚗ Dust Gen/5min | Passive dust generation from the chamber at current star levels |
| ✦ Most Dust | Current dust balance |
| 🗼 Tower Floors | Highest tower floor cleared |
| 🎯 Field Missions | Total field missions completed |

Clicking a tab switches to that category. The leaderboard reads both online and offline player data so rankings are always complete. Player heads are shown for each entry.

---

### My Pets — Sort Function

The **My Pets** storage GUI (opened via `/pets` or the menu) includes a **sort button** at the bottom bar. Click it to cycle through six sort modes:

| Mode | Sort order |
|---|---|
| ⚡ Power | Highest power first |
| 📊 Level | Highest level first |
| ✨ Stars | Most stars first |
| 🔺 Ascension | Highest ascension first |
| 💎 Rarity | UR → SMR → SR → R → N |
| 🔤 A–Z | Pet display name, alphabetical |

The selected sort mode is preserved when navigating between pages.

---

### Starter Ticket

**Brand-new players** (first-ever join) automatically receive a **Starter Ticket** — a special Paper item placed in the center slot of their hotbar (slot 5).

**Properties:**
- Cannot be dropped (pressing Q is blocked).
- Removed from death drops — it cannot be lost.
- Single-use; consumed when right-clicked.

**Right-clicking the ticket** opens an animated **Starter Team Roll** GUI:
- Five slots cycle through a slot-machine spin animation (~4 seconds).
- Slots lock in one by one from left to right, each revealing one of the five rolled pets.
- **Rarity odds:** 90% ɴ · 10% ʀ.
- Click **Collect Pets** when all slots are revealed, or simply close the GUI — pets are added to storage and team either way.

All five starter pets are added directly to the player's team so they can participate in missions and the tower immediately.

---

### Collection

Open with `/collection`. Shows every pet defined in `pets.yml`. Pets the player has not yet obtained are shown as `???`. Clicking an owned pet opens its detail view.

---

### Server-wide Broadcasts

Petly automatically announces notable player achievements to all online players. All broadcast messages are configurable in `messages.yml`.

| Event | Trigger |
|---|---|
| SMR summon | Player pulls a Super Mega Rare from any summon |
| UR summon | Player pulls an Ultra Rare from any summon |
| Mission pet drop | Player receives a bonus pet from a Field Mission |
| Star-Up | Player raises any pet to a new star level (★1 – ★5) |
| Ascension | Player ascends any pet to a new ascension level (ASC 1–10) |
| Tower milestone | Player clears a floor divisible by 10 (Floor 10, 20, 30 …) |
| Mission milestone | Player reaches a total mission count divisible by 10 (10, 20, 30 …) |

To disable a specific broadcast, set its `announce-*` message in `messages.yml` to an empty string.

---

### Floating Pet Display

Team pets orbit around the player as invisible armor-stands wearing skull heads. The orbit radius, height, and update frequency are configurable in `config.yml` under `pet-display`. Display spawns on login and updates every 2 ticks (configurable).

---

## PlaceholderAPI

If PlaceholderAPI is installed, the following placeholders are available:

| Placeholder | Returns |
|---|---|
| `%petly_dust_raw%` | Player's dust balance (number) |
| `%petly_dust_formatted%` | Formatted dust (e.g. `12.5K ✦`) |
| `%petly_teampower%` | Formatted total team power |
| `%petly_petluck%` | Pet luck multiplier (e.g. `1.23x`) |
| `%petly_dustchamber_gen%` | Dust generated per chamber cycle |
| `%petly_active_mission%` | Name of active mission, or `ɴᴏɴᴇ` |
| `%petly_active_mission_time%` | Time remaining on active mission |
| `%petly_pet_count%` | Number of pets the player owns |
| `%petly_missions_completed%` | Total missions completed |

---

## Developer API

Other plugins can integrate with Petly via the public API:

```java
PetlyAPI api = PetlyAPI.getInstance();

long dust = api.getDust(playerUUID);
api.giveDust(playerUUID, 1000L);
api.takeDust(playerUUID, 500L);

OwnedPet pet = api.givePet(playerUUID, "my_dragon");
api.takePet(playerUUID, pet.getInstanceId());

long teamPower = api.getTeamPower(playerUUID);
List<OwnedPet> pets = api.getPets(playerUUID);
```

---

## Tips for Server Admins

- **Reload without restart:** `/petly reload` reloads all config files at once (including `towers.yml`). Player data in memory is not affected.
- **Starter Ticket:** Automatically given to brand-new players on first join. No admin action needed — detection is file-based and reliable across server restarts.
- **Give starter dust:** Run `/petly dust give <player> 2000` on first join via a join script.
- **Custom pet skins:** Replace `PLACEHOLDER_XXX` values in `pets.yml` with real base64 skull texture strings. Tools like [Minecraft-Heads.com](https://minecraft-heads.com) let you copy the texture value directly.
- **Balancing:** The recommended-power values in `missions.yml`, `towers.yml`, and stat values in `pets.yml` are the main balancing levers. Increase them to make progression slower; decrease them for faster-paced servers.
- **Tower tuning:** Edit `towers.yml` to change the dust/power scaling curve, total floor count, Pet XP per floor, or battle animation duration — no code changes required.
- **Reset a player's progress:** Use `/petly reset <player> <type>` to selectively wipe dust, pets, tower progress, or missions. The player must be online.
- **Data backup:** Player data is stored in `plugins/Petly/playerdata/` as individual YAML files. Include this folder in your backup routine.
