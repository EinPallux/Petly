package com.pallux.petly.data;

import com.pallux.petly.PetlyPlugin;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerDataManager {
    private final PetlyPlugin plugin;
    private final StorageAdapter storage;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(PetlyPlugin plugin) {
        this.plugin = plugin;
        this.storage = new StorageAdapter(plugin);
    }

    public void loadAsync(UUID uuid) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            PlayerData data = storage.load(uuid);
            cache.put(uuid, data);
        });
    }

    public void saveAsync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                storage.save(data);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save data for " + uuid, e);
            }
        });
    }

    public void saveSync(PlayerData data) {
        try {
            storage.save(data);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data for " + data.getUuid(), e);
        }
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
                try {
                    storage.save(data);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save data on unload for " + uuid, e);
                }
            });
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            saveSync(data);
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, storage::load);
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public Collection<PlayerData> getAll() {
        return cache.values();
    }

    public List<PlayerData> loadAllForLeaderboard() {
        java.util.Map<UUID, PlayerData> combined = new java.util.LinkedHashMap<>();
        for (PlayerData d : storage.loadAll()) combined.put(d.getUuid(), d);
        combined.putAll(cache);
        return new java.util.ArrayList<>(combined.values());
    }
}
