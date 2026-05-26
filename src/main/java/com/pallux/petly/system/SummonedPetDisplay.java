package com.pallux.petly.system;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.config.ConfigManager;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.data.PlayerDataManager;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class SummonedPetDisplay {
    private final PetlyPlugin plugin;
    private final ConfigManager config;
    private final PlayerDataManager pdm;
    private final Map<UUID, List<ArmorStand>> displayEntities = new HashMap<>();

    private static final double[] ANGLE_OFFSETS = { 135, 90, 180, 270, 225 };

    public SummonedPetDisplay(PetlyPlugin plugin, ConfigManager config, PlayerDataManager pdm) {
        this.plugin = plugin;
        this.config = config;
        this.pdm = pdm;
    }

    public void spawnForPlayer(UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;

        removeForPlayer(playerUUID);

        PlayerData data = pdm.get(playerUUID);
        List<OwnedPet> team = data.getTeamPets();
        if (team.isEmpty()) return;

        List<ArmorStand> stands = new ArrayList<>();
        double radius = config.getPetDisplayOrbitRadius();
        double height = config.getPetDisplayHeightOffset();

        for (int i = 0; i < Math.min(team.size(), 5); i++) {
            OwnedPet op = team.get(i);
            Optional<Pet> petOpt = config.getPetConfig().getPet(op.getPetId());
            if (petOpt.isEmpty()) continue;
            Pet pet = petOpt.get();

            double angle = Math.toRadians(ANGLE_OFFSETS[i]);
            Location loc = getOrbitLocation(player.getLocation(), angle, radius, height);

            ArmorStand head = spawnStand(loc);
            head.getEquipment().setHelmet(buildSkullItem(pet, op));
            head.customName(buildLabel(pet, op));
            head.setCustomNameVisible(true);
            stands.add(head);
        }

        displayEntities.put(playerUUID, stands);
    }

    public void removeForPlayer(UUID playerUUID) {
        List<ArmorStand> old = displayEntities.remove(playerUUID);
        if (old != null) old.forEach(ArmorStand::remove);
    }

    public void updatePositions(UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) { removeForPlayer(playerUUID); return; }

        List<ArmorStand> stands = displayEntities.get(playerUUID);
        if (stands == null) return;

        double radius = config.getPetDisplayOrbitRadius();
        double height = config.getPetDisplayHeightOffset();

        for (int i = 0; i < stands.size(); i++) {
            double angle = Math.toRadians(player.getLocation().getYaw() + 180 + ANGLE_OFFSETS[i]);
            Location newLoc = getOrbitLocation(player.getLocation(), angle, radius, height);
            stands.get(i).teleport(newLoc);
        }
    }

    public void refreshForPlayer(UUID playerUUID) {
        removeForPlayer(playerUUID);
        spawnForPlayer(playerUUID);
    }

    public void removeAll() {
        for (List<ArmorStand> stands : displayEntities.values()) {
            stands.forEach(ArmorStand::remove);
        }
        displayEntities.clear();
    }

    public void spawnForOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            spawnForPlayer(player.getUniqueId());
        }
    }

    // --- Private helpers ---

    private ArmorStand spawnStand(Location loc) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setGravity(false);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        stand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        return stand;
    }

    private Location getOrbitLocation(Location playerLoc, double angle, double radius, double heightOffset) {
        double yaw = Math.toRadians(playerLoc.getYaw() + 180);
        double x = playerLoc.getX() + radius * Math.sin(yaw + angle);
        double z = playerLoc.getZ() + radius * Math.cos(yaw + angle);
        double y = playerLoc.getY() + heightOffset;
        return new Location(playerLoc.getWorld(), x, y, z);
    }

    private ItemStack buildSkullItem(Pet pet, OwnedPet owned) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        String tex = pet.getSkinTexture();
        if (tex != null && !tex.startsWith("PLACEHOLDER") && !tex.isEmpty()) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", tex));
            meta.setPlayerProfile(profile);
        }
        skull.setItemMeta(meta);
        return skull;
    }

    private Component buildLabel(Pet pet, OwnedPet owned) {
        String stars = "★".repeat(owned.getStars());
        String asc = owned.getAscension() > 0 ? " ASC " + owned.getAscension() : "";
        String name = owned.getNickname() != null ? owned.getNickname() : pet.getDisplayName();
        return TextUtil.parse("[" + pet.getRarity().name() + "] " + stars + asc + " | " + name);
    }
}
