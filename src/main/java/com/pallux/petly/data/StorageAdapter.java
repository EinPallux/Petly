package com.pallux.petly.data;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.model.ActiveMission;
import com.pallux.petly.model.MissionResult;
import com.pallux.petly.model.OwnedPet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StorageAdapter {
    private final File dataFolder;

    public StorageAdapter(PetlyPlugin plugin) {
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public PlayerData load(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) return new PlayerData(uuid);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        long dust = cfg.getLong("dust", 0);
        double petLuck = cfg.getDouble("pet-luck", 1.0);
        int sincesr = cfg.getInt("summons-since-sr", 0);
        int sincesmr = cfg.getInt("summons-since-smr", 0);
        int sinceur = cfg.getInt("summons-since-ur", 0);
        long lastDay = cfg.getLong("last-daily-day", -1);
        boolean dailyUsed = cfg.getBoolean("daily-bonus-used", false);
        int missionsDone = cfg.getInt("missions-completed", 0);
        long pendingDust = cfg.getLong("pending-dust", 0);
        long lastChamberTick = cfg.getLong("last-chamber-tick", System.currentTimeMillis());

        List<OwnedPet> pets = new ArrayList<>();
        ConfigurationSection petsSection = cfg.getConfigurationSection("pets");
        if (petsSection != null) {
            for (String key : petsSection.getKeys(false)) {
                ConfigurationSection p = petsSection.getConfigurationSection(key);
                if (p == null) continue;
                UUID instanceId = UUID.fromString(key);
                OwnedPet op = new OwnedPet(
                        instanceId,
                        p.getString("pet-id", ""),
                        p.getInt("level", 1),
                        p.getLong("xp", 0),
                        p.getInt("stars", 0),
                        p.getInt("ascension", 0),
                        p.getString("nickname", null),
                        p.getBoolean("in-chamber", false),
                        p.getBoolean("in-team", false)
                );
                pets.add(op);
            }
        }

        List<UUID> teamPetIds = new ArrayList<>();
        for (String s : cfg.getStringList("team")) {
            try { teamPetIds.add(UUID.fromString(s)); } catch (Exception ignored) {}
        }

        ActiveMission activeMission = null;
        ConfigurationSection mSection = cfg.getConfigurationSection("active-mission");
        if (mSection != null) {
            int mId = mSection.getInt("id");
            long start = mSection.getLong("start");
            long end = mSection.getLong("end");
            List<UUID> mTeam = new ArrayList<>();
            for (String s : mSection.getStringList("team")) {
                try { mTeam.add(UUID.fromString(s)); } catch (Exception ignored) {}
            }
            activeMission = new ActiveMission(mId, start, end, mTeam);
        }

        List<MissionResult> log = new ArrayList<>();
        for (java.util.Map<?, ?> m : cfg.getMapList("mission-log")) {
            try {
                Object id      = m.get("mission-id");
                Object mname   = m.get("mission-name");
                Object succ    = m.get("success");
                Object mDust   = m.get("dust");
                Object mXp     = m.get("xp");
                Object drop    = m.get("pet-drop");
                Object at      = m.get("at");
                log.add(new MissionResult(
                        id    instanceof Number  n ? n.intValue()  : 0,
                        mname instanceof String  s ? s             : "",
                        succ  instanceof Boolean b ? b             : false,
                        mDust instanceof Number  n ? n.longValue() : 0L,
                        mXp   instanceof Number  n ? n.intValue()  : 0,
                        drop  instanceof String  s ? s             : null,
                        at    instanceof Number  n ? n.longValue() : 0L
                ));
            } catch (Exception ignored) {}
        }

        List<UUID> chamberIds = new ArrayList<>();
        for (String s : cfg.getStringList("chamber")) {
            try { chamberIds.add(UUID.fromString(s)); } catch (Exception ignored) {}
        }

        PlayerData pdata = new PlayerData(uuid, dust, petLuck, sincesr, sincesmr, sinceur,
                pets, teamPetIds, activeMission, lastDay, dailyUsed,
                missionsDone, log, chamberIds, pendingDust, lastChamberTick);
        pdata.setHighestTowerFloor(cfg.getInt("tower-floor", 0));
        return pdata;
    }

    public List<PlayerData> loadAll() {
        List<PlayerData> result = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return result;
        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                result.add(load(uuid));
            } catch (Exception ignored) {}
        }
        return result;
    }

    public void save(PlayerData data) throws IOException {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        cfg.set("dust", data.getDust());
        cfg.set("pet-luck", data.getPetLuck());
        cfg.set("summons-since-sr", data.getSummonsSinceLastSr());
        cfg.set("summons-since-smr", data.getSummonsSinceLastSmr());
        cfg.set("summons-since-ur", data.getSummonsSinceLastUr());
        cfg.set("last-daily-day", data.getLastDailyMissionDay());
        cfg.set("daily-bonus-used", data.isDailyBonusUsedToday());
        cfg.set("missions-completed", data.getMissionsCompleted());
        cfg.set("pending-dust", data.getPendingDust());
        cfg.set("last-chamber-tick", data.getLastChamberTickTimestamp());

        for (OwnedPet op : data.getPets()) {
            String prefix = "pets." + op.getInstanceId();
            cfg.set(prefix + ".pet-id", op.getPetId());
            cfg.set(prefix + ".level", op.getLevel());
            cfg.set(prefix + ".xp", op.getXp());
            cfg.set(prefix + ".stars", op.getStars());
            cfg.set(prefix + ".ascension", op.getAscension());
            cfg.set(prefix + ".nickname", op.getNickname());
            cfg.set(prefix + ".in-chamber", op.isInChamber());
            cfg.set(prefix + ".in-team", op.isInTeam());
        }

        List<String> team = data.getTeamPetIds().stream().map(UUID::toString).toList();
        cfg.set("team", team);

        if (data.hasActiveMission()) {
            ActiveMission am = data.getActiveMission();
            cfg.set("active-mission.id", am.getMissionId());
            cfg.set("active-mission.start", am.getStartTimestamp());
            cfg.set("active-mission.end", am.getEndTimestamp());
            cfg.set("active-mission.team", am.getTeamPetIds().stream().map(UUID::toString).toList());
        }

        List<java.util.Map<String, Object>> logList = new ArrayList<>();
        for (MissionResult r : data.getMissionLog()) {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("mission-id", r.getMissionId());
            m.put("mission-name", r.getMissionName());
            m.put("success", r.isSuccess());
            m.put("dust", r.getDustEarned());
            m.put("xp", r.getXpEarned());
            m.put("pet-drop", r.getPetDropped());
            m.put("at", r.getCompletedAt());
            logList.add(m);
        }
        cfg.set("mission-log", logList);

        List<String> chamber = data.getChamberPetIds().stream().map(UUID::toString).toList();
        cfg.set("chamber", chamber);

        cfg.set("tower-floor", data.getHighestTowerFloor());

        cfg.save(file);
    }
}
