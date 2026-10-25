package dev.j3fftw.litexpansion.machine;

import dev.j3fftw.extrautils.interfaces.InventoryBlock;
import dev.j3fftw.litexpansion.Items;
import dev.j3fftw.litexpansion.LiteXpansion;
import dev.j3fftw.litexpansion.machine.api.PoweredMachine;
import dev.j3fftw.litexpansion.utils.BlockMenuPresetTest;
import dev.j3fftw.litexpansion.uumatter.UUMatter;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun5.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun5.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import dev.j3fftw.litexpansion.compat.MaterialCompat;
import io.github.thebusybiscuit.slimefun5.libraries.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the {@link UUCrafter} class.
 *
 * @author j3fftw
 */
public class UUCrafter extends SlimefunItem implements InventoryBlock, EnergyNetComponent, PoweredMachine {

    public static final int ENERGY_CONSUMPTION = 50_000;
    public static final int CAPACITY = ENERGY_CONSUMPTION;
    public static final int INPUT_SLOT = 19;
    public static final int OUTPUT_SLOT = 25;
    public static final int[] CRAFTING_SLOTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    public static final int START_STOP = 40;
    public static final org.bukkit.inventory.ItemStack RUNNING = CustomItemStack.create(
        MaterialCompat.safe(XMaterial.GREEN_STAINED_GLASS_PANE), ChatColor.GRAY + "Click to stop"
    );

    public static final org.bukkit.inventory.ItemStack NOT_RUNNING = CustomItemStack.create(
        MaterialCompat.safe(XMaterial.RED_STAINED_GLASS_PANE), ChatColor.GRAY + "Click to start"
    );

    // [MEMORY + BUG] Dahulu HashMap biasa tanpa pembersihan. 3 masalah sekaligus:
    // 1) BlockTicker mesin ini isSynchronized() == false, jadi tick berjalan di thread async dan
    //    membaca map ini; HashMap yang diakses sambil ditulis thread lain bisa rusak (resize) atau
    //    berputar tanpa henti. ConcurrentHashMap menghilangkan risiko itu.
    // 2) Entri tidak pernah dibuang saat mesin dibongkar -> kunci Location menumpuk seumur session.
    //    Sekarang dibersihkan di onPlayerBreak di bawah.
    // 3) Pembacaan memakai auto-unboxing (whatIsRunning.get(loc)) -> NullPointerException kalau
    //    mesin di-tick sebelum onNewInstance mengisi map (terjadi setelah reload / chunk unload).
    protected static final Map<Location, Boolean> whatIsRunning = new java.util.concurrent.ConcurrentHashMap<>();

    public UUCrafter() {
        super(Items.LITEXPANSION, Items.UU_CRAFTER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
            Items.UU_MATTER.item(), new ItemStack(MaterialCompat.safe(XMaterial.CRAFTING_TABLE)), Items.UU_MATTER.item(),
            Items.CARGO_CONFIGURATOR.item(), Items.ADVANCED_MACHINE_BLOCK.item(), Items.GLASS_CUTTER.item(),
            Items.UU_MATTER.item(), Items.UU_MATTER.item(), Items.UU_MATTER.item()
        });
        setup();
        this.addItemHandler(
            new BlockBreakHandler(false, false) {
                @Override
                public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
                    BlockMenu blockMenu = BlockStorage.getInventory(event.getBlock());
                    // [MEMORY] Buang status running mesin ini, tidak ada gunanya disimpan terus
                    // setelah bloknya hilang (kunci Location menahan dunia/chunk di heap).
                    whatIsRunning.remove(event.getBlock().getLocation());
                    if (blockMenu != null) {
                        blockMenu.dropItems(blockMenu.getLocation(), INPUT_SLOT, OUTPUT_SLOT);
                        blockMenu.dropItems(blockMenu.getLocation(), CRAFTING_SLOTS);
                    }
                }
            }
        );
    }

    @Override
    public void preRegister() {
        this.addItemHandler(new BlockTicker() {
            public void tick(Block b, SlimefunItem sf, Config data) {
                UUCrafter.this.tick(b);
            }

            public boolean isSynchronized() {
                return false;
            }
        });
    }

    private void tick(Block block) {
        @Nullable final BlockMenu blockMenu = BlockStorage.getInventory(block);
        final Location location = block.getLocation();
        if (blockMenu == null) {
            return;
        }

        if (this.getCharge(location) < getDefaultEnergyConsumption()) {
            return;
        }

        Boolean running = whatIsRunning.get(location);
        if (running == null) {
            // [BUG] Sebelumnya baris ini auto-unboxing dari Map.get() -> NPE kalau mesin di-tick
            // sebelum onNewInstance mengisi map (setelah reload / chunk unload), dan tick async
            // membuat crash ini muncul tanpa pola. Ambil nilai yang tersimpan di BlockStorage,
            // persis seperti yang dilakukan onNewInstance, lalu catat.
            running = Boolean.parseBoolean(BlockStorage.getLocationInfo(location, "RUNNING"));
            whatIsRunning.put(location, running);
        }
        if (!running) {
            return;
        }

        ItemStack[] itemStacks = new ItemStack[9];
        int i = 0;

        for (int slot : CRAFTING_SLOTS) {
            itemStacks[i] = blockMenu.getItemInSlot(slot);
            i++;
        }

        for (Map.Entry<ItemStack, ItemStack[]> entry : UUMatter.INSTANCE.getRecipes().entrySet()) {
            if (Arrays.equals(entry.getValue(), itemStacks)) {
                int amount = 9;
                for (ItemStack recipeStack : entry.getValue()) {
                    if (recipeStack == null) amount--;
                }

                ItemStack output = entry.getKey().clone();
                final ItemStack input = blockMenu.getItemInSlot(INPUT_SLOT);
                SlimefunItem slimefunItem = SlimefunItem.getByItem(input);

                if (input != null
                    && slimefunItem != null
                    && slimefunItem.getId().equals("UU_MATTER")
                    && input.getAmount() >= amount
                    && blockMenu.fits(output, OUTPUT_SLOT)
                ) {
                    this.removeCharge(location, getDefaultEnergyConsumption());
                    blockMenu.pushItem(output, OUTPUT_SLOT);
                    blockMenu.consumeItem(INPUT_SLOT, amount);
                }
                break;
            }
        }
    }

    public void setup() {
        new BlockMenuPresetTest(this.getId(), "\u00a78UU Crafter", this);
    }

    public void onNewInstance(BlockMenu menu, Block block) {
        String isRunningString = BlockStorage.getLocationInfo(block.getLocation(), "RUNNING");
        boolean isRunning = false;
        if (isRunningString != null) {
            isRunning = Boolean.parseBoolean(isRunningString);
        }

        whatIsRunning.put(block.getLocation(), isRunning);

        menu.replaceExistingItem(START_STOP, isRunning ? RUNNING : NOT_RUNNING);
        menu.addMenuClickHandler(START_STOP, (p, slot, item, action) -> {
            toggleRunning(menu, block);
            return false;
        });
    }

    public void toggleRunning(BlockMenu blockMenu, Block block) {
        // [BUG] getOrDefault, bukan unboxing langsung: tombol START/STOP harus tetap berfungsi
        // walaupun mesin ini belum pernah tercatat di map (baru di-toggle sebelum tick pertama).
        boolean setTo = !whatIsRunning.getOrDefault(block.getLocation(), false);
        BlockStorage.addBlockInfo(block, "RUNNING", String.valueOf(setTo));
        whatIsRunning.put(block.getLocation(), setTo);
        ItemStack itemStack = setTo ? RUNNING : NOT_RUNNING;
        blockMenu.replaceExistingItem(START_STOP, itemStack);
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }

    @Override
    public int getDefaultEnergyConsumption() {
        return ENERGY_CONSUMPTION;
    }

    @Override
    public int[] getInputSlots() {
        return new int[] {
            INPUT_SLOT
        };
    }

    @Override
    public int[] getOutputSlots() {
        return new int[] {
            OUTPUT_SLOT
        };
    }

}

