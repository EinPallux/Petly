package com.pallux.petly.api;

import com.pallux.petly.PetlyPlugin;
import com.pallux.petly.data.PlayerData;
import com.pallux.petly.model.OwnedPet;
import com.pallux.petly.model.Pet;
import com.pallux.petly.model.Rarity;
import com.pallux.petly.system.PowerCalculator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API for Petly. Other plugins can use this to interact with the pet system.
 * Access via: PetlyAPI api = PetlyAPI.getInstance();
 */
public class PetlyAPI {
    private static PetlyAPI instance;
    private final PetlyPlugin plugin;

    public PetlyAPI(PetlyPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static PetlyAPI getInstance() {
        return instance;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return plugin.getPlayerDataManager().get(uuid);
    }

    public long getDust(UUID uuid) {
        return getPlayerData(uuid).getDust();
    }

    public void giveDust(UUID uuid, long amount) {
        getPlayerData(uuid).addDust(amount);
        plugin.getPlayerDataManager().saveAsync(uuid);
    }

    public boolean takeDust(UUID uuid, long amount) {
        PlayerData data = getPlayerData(uuid);
        if (!data.hasDust(amount)) return false;
        data.takeDust(amount);
        plugin.getPlayerDataManager().saveAsync(uuid);
        return true;
    }

    public OwnedPet givePet(UUID uuid, String petId) {
        Optional<Pet> petOpt = plugin.getConfigManager().getPetConfig().getPet(petId);
        if (petOpt.isEmpty()) return null;
        PlayerData data = getPlayerData(uuid);
        OwnedPet op = new OwnedPet(petId);
        data.addPet(op);
        plugin.getPlayerDataManager().saveAsync(uuid);
        return op;
    }

    public boolean takePet(UUID uuid, UUID instanceId) {
        PlayerData data = getPlayerData(uuid);
        Optional<OwnedPet> op = data.getPetByInstanceId(instanceId);
        if (op.isEmpty()) return false;
        data.removePet(instanceId);
        plugin.getPlayerDataManager().saveAsync(uuid);
        return true;
    }

    public long getTeamPower(UUID uuid) {
        return plugin.getPowerCalc().calcTeamPower(getPlayerData(uuid));
    }

    public double getPetLuck(UUID uuid) {
        return getPlayerData(uuid).getPetLuck();
    }

    public int getMissionsCompleted(UUID uuid) {
        return getPlayerData(uuid).getMissionsCompleted();
    }

    public List<OwnedPet> getPets(UUID uuid) {
        return getPlayerData(uuid).getPets();
    }

    public List<Pet> getAllPetDefinitions() {
        return List.copyOf(plugin.getConfigManager().getPetConfig().getAllPets().values());
    }

    public List<Pet> getPetsByRarity(Rarity rarity) {
        return plugin.getConfigManager().getPetConfig().getPetsByRarity(rarity);
    }

    public long calcPetPower(Pet pet, OwnedPet ownedPet) {
        return plugin.getPowerCalc().calcPetPower(pet, ownedPet);
    }
}
