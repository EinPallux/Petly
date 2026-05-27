package com.pallux.petly.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String miniMessage) {
        meta.displayName(item(miniMessage));
        return this;
    }

    public ItemBuilder name(Component component) {
        meta.displayName(noItalic(component));
        return this;
    }

    public ItemBuilder lore(String... lines) {
        List<Component> loreList = new ArrayList<>();
        for (String line : lines) loreList.add(item(line));
        meta.lore(loreList);
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        List<Component> loreList = new ArrayList<>();
        for (String line : lines) loreList.add(item(line));
        meta.lore(loreList);
        return this;
    }

    public ItemBuilder loreComponents(List<Component> lines) {
        List<Component> wrapped = new ArrayList<>();
        for (Component c : lines) wrapped.add(noItalic(c));
        meta.lore(wrapped);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder glow(boolean condition) {
        if (condition) return glow();
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAll() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    // --- Static helpers ---

    public static ItemStack skull(String base64Texture, String displayName, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (base64Texture != null && !base64Texture.startsWith("PLACEHOLDER") && !base64Texture.isEmpty()) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", base64Texture));
            skullMeta.setPlayerProfile(profile);
        }

        skullMeta.displayName(item(displayName));

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) loreComponents.add(item(line));
        skullMeta.lore(loreComponents);
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static ItemStack filler(Material material) {
        return new ItemBuilder(material).name(" ").hideAll().build();
    }

    public static ItemStack named(Material material, String name) {
        return new ItemBuilder(material).name(name).build();
    }

    // Parses MiniMessage and strips the default italic Minecraft applies to custom item text
    private static Component item(String miniMessage) {
        return noItalic(TextUtil.parse(miniMessage));
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
