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
| `playerdata/` | One YAML file per player UUID — do not edit manually |

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
| `petly.admin` | OP | Full access to all admin commands. Inherits `petly.player`. |
| `petly.player` | Everyone | Access to all player-facing commands and GUIs. |
| `petly.summon.3` | false | Unlocks the ×3 summon button in the summon GUI. |
| `petly.summon.6` | false | Unlocks the ×6 summon button in the summon GUI. |

Grant summon permissions to ranks using your permissions plugin:

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

`<petId>` is the key used in `pets.yml` (e.g. `my_dragon`).  
`<petUUID>` is the unique instance ID of an owned pet (visible in the pet detail GUI or via the API).

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

- Only **one mission** can be active at a time.
- Missions complete in real time — even while the player is offline.
- Success chance is determined by a sigmoid curve based on team power vs. recommended power.  
  At 100% recommended power the chance is ~90%; at 120%+ it is capped at 100%.
- **Rewards on success:** Dust, Pet XP distributed to all team members, and a configurable chance to drop a bonus pet.
- **Daily Bonus:** The first mission completed each day awards **2× dust**.
- **Offline resolution:** If a mission finishes while the player is offline, results are shown on their next login.

**Milestone rewards:** At missions 10, 25, 50, 75, and 100, players receive a free pet of increasing rarity as a reward.

---

### Dust Chamber

Open with `/chamber`. Up to **3 pets** can be placed in the chamber to passively generate dust over time.

Generation per cycle per slot:

```
rate = baseRate × (1 + stars × starMultiplier)
```

The cycle timer is configurable (`dust-chamber.interval-ticks` in `config.yml`). Players must manually click **Collect** in the chamber GUI to claim pending dust.

---

### Collection

Open with `/collection`. Shows every pet defined in `pets.yml`. Pets the player has not yet obtained are shown as `???`. Clicking an owned pet opens its detail view.

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

- **Reload without restart:** `/petly reload` reloads all 5 YAML config files at once. Player data in memory is not affected.
- **Give starter dust:** Run `/petly dust give <player> 2000` on first join via a join script.
- **Custom pet skins:** Replace `PLACEHOLDER_XXX` values in `pets.yml` with real base64 skull texture strings. Tools like [Minecraft-Heads.com](https://minecraft-heads.com) let you copy the texture value directly.
- **Balancing:** The recommended-power values in `missions.yml` and stat values in `pets.yml` are the main balancing levers. Increase them to make progression slower; decrease them for faster-paced servers.
- **Data backup:** Player data is stored in `plugins/Petly/playerdata/` as individual YAML files. Include this folder in your backup routine.
