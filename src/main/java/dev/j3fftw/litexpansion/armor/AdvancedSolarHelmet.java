package dev.j3fftw.litexpansion.armor;

import dev.j3fftw.litexpansion.Items;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.gadgets.SolarHelmet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class AdvancedSolarHelmet extends SolarHelmet {

    @Nonnull
    private final Type type;

    public AdvancedSolarHelmet(Type type) {
        super(Items.LITEXPANSION, type.getItem(), RecipeType.ENHANCED_CRAFTING_TABLE, type.getRecipe(),
            type.getGenerationRate()
        );
        this.type = type;
        Items.ADVANCED_SOLAR_HELMET.addEnchantment(Enchantment.UNBREAKING, 1);
        Items.CARBONADO_SOLAR_HELMET.addEnchantment(Enchantment.UNBREAKING, 2);
        Items.ENERGIZED_SOLAR_HELMET.addEnchantment(Enchantment.UNBREAKING, 3);
        Items.ADVANCEDLX_SOLAR_HELMET.addUnsafeEnchantment(Enchantment.UNBREAKING, 4);
        Items.HYBRID_SOLAR_HELMET.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        Items.ULTIMATE_SOLAR_HELMET.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {

        ADVANCED(Items.ADVANCED_SOLAR_HELMET, 5, new ItemStack[] {
            null, SlimefunItems.SOLAR_GENERATOR, null,
            Items.IRON_PLATE.clone(), SlimefunItems.SOLAR_HELMET, Items.IRON_PLATE.clone(),
            Items.IRON_PLATE.clone(), null, Items.IRON_PLATE.clone()
        }),

        CARBONADO(Items.CARBONADO_SOLAR_HELMET, 10, new ItemStack[] {
            null, SlimefunItems.SOLAR_GENERATOR_2, null,
            Items.COPPER_PLATE.clone(), Items.ADVANCED_SOLAR_HELMET, Items.COPPER_PLATE.clone(),
            Items.COPPER_PLATE.clone(), null, Items.COPPER_PLATE.clone()
        }),
        ENERGIZED(Items.ENERGIZED_SOLAR_HELMET, 20, new ItemStack[] {
            null, SlimefunItems.SOLAR_GENERATOR_3, null,
            Items.GOLD_PLATE.clone(), Items.CARBONADO_SOLAR_HELMET, Items.GOLD_PLATE.clone(),
            Items.GOLD_PLATE.clone(), null, Items.GOLD_PLATE.clone()
        }),
        ADVANCEDLX(Items.ADVANCEDLX_SOLAR_HELMET, 50, new ItemStack[] {
            null, SlimefunItems.SOLAR_GENERATOR_4, null,
            Items.DIAMOND_PLATE.clone(), Items.ENERGIZED_SOLAR_HELMET, Items.DIAMOND_PLATE.clone(),
            Items.DIAMOND_PLATE.clone(), null, Items.DIAMOND_PLATE.clone()
        }),
        HYBRID(Items.HYBRID_SOLAR_HELMET, 100, new ItemStack[] {
            null, Items.ADVANCED_SOLAR_PANEL, null,
            Items.THORIUM_PLATE.clone(), Items.ADVANCEDLX_SOLAR_HELMET, Items.THORIUM_PLATE.clone(),
            Items.THORIUM_PLATE.clone(), null, Items.THORIUM_PLATE.clone()
        }),
        ULTIMATE(Items.ULTIMATE_SOLAR_HELMET, 250, new ItemStack[] {
            null, Items.ULTIMATE_SOLAR_PANEL, null,
            Items.IRIDIUM_PLATE.clone(), Items.HYBRID_SOLAR_HELMET, Items.IRIDIUM_PLATE.clone(),
            Items.IRIDIUM_PLATE.clone(), null, Items.IRIDIUM_PLATE.clone()
        });


        @Nonnull
        private final SlimefunItemStack item;
        private final int generationRate;

        @Nonnull
        private final ItemStack[] recipe;
    }
}

