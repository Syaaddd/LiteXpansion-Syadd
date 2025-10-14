# Migration Guide for Minecraft 1.21 Compatibility

## Known Issues

When upgrading to Minecraft 1.21, you may encounter the following errors:
```
java.lang.VerifyError: Bad type on operand stack
Exception Details:
  Location:
    dev/j3fftw/litexpansion/ItemSetup.registerRubber()V @60: putfield
  Reason:
    Type 'io/github/thebusybiscuit/slimefun4/api/items/SlimefunItemStack' (current frame, stack[1]) is not assignable to 'org/bukkit/inventory/ItemStack'
```

Or:
```
java.lang.VerifyError: Bad type on operand stack
Exception Details:
  Location:
    dev/j3fftw/litexpansion/ItemSetup.registerEndgameItems()V @51: invokevirtual
  Reason:
    Type 'io/github/thebusybiscuit/slimefun4/api/items/SlimefunItemStack' (current frame, stack[6]) is not assignable to 'org/bukkit/inventory/ItemStack'
```

## Root Cause

This error occurs due to API changes in newer versions of Slimefun4 where the relationship between `SlimefunItemStack` and `ItemStack` has been modified. The API no longer allows direct assignment between these types in certain contexts, likely due to stricter type checking or changes in the class hierarchy.

## Required Changes

To fix the compatibility issues, the following code changes need to be made in `ItemSetup.java`:

### In `registerRubber()` method:

```java
// Change from:
this.rubberItem = SlimefunItem.getById("RUBBER").getItem();

// To:
SlimefunItem item = SlimefunItem.getById("RUBBER");
this.rubberItem = item != null ? item.getItem() : null;

// And add null check before using rubberItem
if (this.rubberItem != null) {
    // continue with logic
}
```

### In `registerEndgameItems()` method:

Look for locations where SlimefunItemStack is being used where ItemStack is expected and ensure proper type handling.

### Additional Changes Required

The `ItemStack` and `SlimefunItemStack` handling throughout the codebase might need adjustment due to API changes in newer versions of Slimefun4, especially in:
- Recipe registrations
- Item comparisons
- Item assignments
- Method parameters expecting ItemStack but receiving SlimefunItemStack

## Recommended Solution

1. Update to the latest version of Slimefun4 that supports Minecraft 1.21 (RC-40 or newer)
2. Make necessary code modifications to handle API changes
3. Replace direct assignments between SlimefunItemStack and ItemStack with proper conversion methods
4. Use `SlimefunItemStack.clone()` or similar methods where appropriate
5. Test thoroughly on a Minecraft 1.21 server

## Dependencies

- Minecraft 1.21
- Spigot/Paper 1.21
- Slimefun4 RC-40 or newer