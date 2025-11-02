package dev.j3fftw.litexpansion.weapons;

import dev.j3fftw.litexpansion.Items;
import dev.j3fftw.litexpansion.items.PassiveElectricRemoval;
import dev.j3fftw.litexpansion.machine.multiblock.MetalForge;
import dev.j3fftw.litexpansion.utils.Constants;
import dev.j3fftw.litexpansion.utils.Utils;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Optional;

public class NanoBlade extends SimpleSlimefunItem<ItemUseHandler> implements Rechargeable, PassiveElectricRemoval {

    public static final float CAPACITY = 4_000;
    public static final float PER_TICK_REMOVAL = 64;

    public NanoBlade() {
        super(Items.LITEXPANSION, Items.NANO_BLADE, MetalForge.RECIPE_TYPE, new ItemStack[] {
                        new ItemStack(Material.GLOWSTONE_DUST), Items.ADVANCED_ALLOY.item(), null,
                        new ItemStack(Material.GLOWSTONE_DUST), Items.ADVANCED_ALLOY.item(), null,
                        Items.CARBON_PLATE.item(), SlimefunItems.POWER_CRYSTAL.item(), Items.CARBON_PLATE.item()
                }
        );
    }

    @Override
    public float getMaxItemCharge(ItemStack item) {
        return CAPACITY;
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return event -> {
            final ItemMeta nanoBladeMeta = event.getItem().getItemMeta();
            final Enchantment enchantment = Enchantment.UNBREAKING;  // pake unbreaking
            boolean enabled = !nanoBladeMeta.removeEnchant(enchantment);

            int damage;

            if (enabled && getItemCharge(event.getItem()) > getRemovedChargePerTick()) {
                nanoBladeMeta.addEnchant(enchantment, 1, false);
                nanoBladeMeta.setDisplayName(ChatColor.DARK_GREEN + "Nano Blade" + ChatColor.GREEN + " (On)");

                damage = 13; // Base is 7 so 7 + 13 = 20
            } else {
                nanoBladeMeta.setDisplayName(ChatColor.DARK_GREEN + "Nano Blade" + ChatColor.RED + " (Off)");

                damage = -3; // Base is 7 so 7 - 3 = 4
            }

            PersistentDataAPI.setBoolean(nanoBladeMeta, Constants.NANO_BLADE_ENABLED, enabled);

            nanoBladeMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);

            // Gunakan constructor baru dengan NamespacedKey dan EquipmentSlotGroup
            NamespacedKey key = new NamespacedKey("litexpansion", "nano_blade_damage");
            nanoBladeMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(key, damage,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND
                    )
            );

            event.getItem().setItemMeta(nanoBladeMeta);
        };
    }

    @Override
    public float getRemovedChargePerTick() {
        return PER_TICK_REMOVAL;
    }

    @Override
    public float getCapacity() {
        return CAPACITY;
    }

    @Override
    public boolean isEnabled(@Nonnull ItemMeta meta) {
        final Optional<Boolean> opt = Utils.getOptionalBoolean(meta, Constants.NANO_BLADE_ENABLED);

        if (opt.isPresent() && opt.get()) {
            return true;
        }

        // Ganti glow enchantment dengan Unbreaking
        Enchantment unbreaking = Enchantment.UNBREAKING;
        return unbreaking != null && meta.hasEnchant(unbreaking);
    }

}