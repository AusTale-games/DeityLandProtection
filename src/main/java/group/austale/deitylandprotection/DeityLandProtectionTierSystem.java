package group.austale.deitylandprotection;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.UUID;
import java.util.function.Predicate;

public final class DeityLandProtectionTierSystem {
    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 4;

    private DeityLandProtectionTierSystem() {
    }

    public static int getUpkeepTierForClaim(UpkeepStore upkeepStore, Claim claim) {
        if (upkeepStore == null || claim == null) {
            return MIN_TIER;
        }
        UpkeepState st = upkeepStore.get(claim.getCenterX(), claim.getCenterZ());
        return st == null ? MIN_TIER : clampTier(st.getUpgradeTier());
    }

    public static int getTierMultiplier(int tier) {
        int safeTier = clampTier(tier);
        return 1 << safeTier - 1;
    }

    public static int getClaimRadiusForTier(int baseRadius, int tier) {
        long base = Math.max(1L, baseRadius);
        long radius = base * (long)getTierMultiplier(tier);
        if (radius > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)radius;
    }

    public static int getUpkeepEssenceCostPerHourForTier(int baseEssenceCostPerHour, int tier) {
        long base = Math.max(1L, baseEssenceCostPerHour);
        long cost = base * (long)getTierMultiplier(tier);
        if (cost > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)cost;
    }

    public static boolean tryUpgradeClaimTier(DeityLandProtectionPlugin plugin, ClaimStore claims, World world, Claim claim, ItemContainer container, UpkeepState state) {
        if (plugin == null || claims == null || world == null || claim == null || container == null || state == null) {
            return false;
        }
        int currentTier = state.getUpgradeTier();
        if (currentTier >= MAX_TIER) {
            return false;
        }
        int nextTier = currentTier + 1;
        if (!canApplyUpgradeMaterials(plugin, container, nextTier)) {
            return false;
        }
        int nextRadius = getClaimRadiusForTier(plugin.getClaimRadius(), nextTier);
        if (nextRadius > claim.getRadius() && !claims.hasUnclaimedTerritoryInRadius(claim.getCenterX(), claim.getCenterZ(), nextRadius)) {
            DeityLandProtectionTierSystem.sendNoUnclaimedTerritoryMessage(plugin, world, claim);
            return false;
        }
        consumeUpgradeMaterials(plugin, container, nextTier);
        state.setUpgradeTier(nextTier);
        PlayerRef ownerRef = findPlayer(world, claim.getOwner());
        if (ownerRef != null) {
            LangPreferenceManager.Language lang = plugin.getEffectiveLanguage(ownerRef);
            int upkeepCost = getUpkeepEssenceCostPerHourForTier(plugin.getUpkeepEssenceCostPerHour(), nextTier);
            String msg = Text.tierUpgraded(lang, nextTier, nextRadius, upkeepCost);
            plugin.sendPlayerMessageImmediate(ownerRef, msg);
        }
        return true;
    }

    private static boolean canApplyUpgradeMaterials(DeityLandProtectionPlugin plugin, ItemContainer container, int targetTier) {
        if (plugin == null || container == null) {
            return false;
        }
        return switch (targetTier) {
            default -> false;
            case 2 -> countMatchingItems(container, plugin::isTier2UpgradeItemId) >= plugin.getUpgradeTier2ItemQuantity();
            case 3 -> countMatchingItems(container, plugin::isTier3UpgradeItemId) >= plugin.getUpgradeTier3ItemQuantity();
            case 4 -> {
                int primaryRequired = plugin.getUpgradeTier4PrimaryItemQuantity();
                int secondaryRequired = plugin.getUpgradeTier4SecondaryItemQuantity();
                if (plugin.areTier4UpgradeItemsSameItem()) {
                    yield countMatchingItems(container, plugin::isTier4PrimaryUpgradeItemId) >= primaryRequired + secondaryRequired;
                }
                yield countMatchingItems(container, plugin::isTier4PrimaryUpgradeItemId) >= primaryRequired && countMatchingItems(container, plugin::isTier4SecondaryUpgradeItemId) >= secondaryRequired;
            }
        };
    }

    private static void consumeUpgradeMaterials(DeityLandProtectionPlugin plugin, ItemContainer container, int targetTier) {
        if (plugin == null || container == null) {
            return;
        }
        switch (targetTier) {
            case 2: {
                consumeMatchingItems(container, plugin.getUpgradeTier2ItemQuantity(), plugin::isTier2UpgradeItemId);
                break;
            }
            case 3: {
                consumeMatchingItems(container, plugin.getUpgradeTier3ItemQuantity(), plugin::isTier3UpgradeItemId);
                break;
            }
            case 4: {
                int primaryRequired = plugin.getUpgradeTier4PrimaryItemQuantity();
                int secondaryRequired = plugin.getUpgradeTier4SecondaryItemQuantity();
                if (plugin.areTier4UpgradeItemsSameItem()) {
                    consumeMatchingItems(container, primaryRequired + secondaryRequired, plugin::isTier4PrimaryUpgradeItemId);
                } else {
                    consumeMatchingItems(container, primaryRequired, plugin::isTier4PrimaryUpgradeItemId);
                    consumeMatchingItems(container, secondaryRequired, plugin::isTier4SecondaryUpgradeItemId);
                }
                break;
            }
        }
    }

    private static int countMatchingItems(ItemContainer container, Predicate<String> matcher) {
        if (container == null || matcher == null) {
            return 0;
        }
        int total = 0;
        short cap = container.getCapacity();
        short slot = 0;
        while (slot < cap) {
            ItemStack stack = container.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && stack.isValid() && matcher.test(stack.getItemId())) {
                total += Math.max(0, stack.getQuantity());
            }
            slot = (short)(slot + 1);
        }
        return total;
    }

    private static int consumeMatchingItems(ItemContainer container, int amount, Predicate<String> matcher) {
        if (container == null || amount <= 0 || matcher == null) {
            return 0;
        }
        int remaining = amount;
        short cap = container.getCapacity();
        short slot = 0;
        while (slot < cap && remaining > 0) {
            ItemStack stack = container.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && stack.isValid() && matcher.test(stack.getItemId())) {
                int qty = Math.max(0, stack.getQuantity());
                int take = Math.min(qty, remaining);
                if (take > 0) {
                    container.setItemStackForSlot(slot, stack.withQuantity(qty - take));
                    remaining -= take;
                }
            }
            slot = (short)(slot + 1);
        }
        return amount - remaining;
    }

    private static PlayerRef findPlayer(World world, UUID playerUuid) {
        if (world == null || playerUuid == null) {
            return null;
        }
        try {
            for (PlayerRef playerRef : world.getPlayerRefs()) {
                if (playerRef == null || !playerUuid.equals(playerRef.getUuid())) {
                    continue;
                }
                return playerRef;
            }
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        return null;
    }

    private static void sendNoUnclaimedTerritoryMessage(DeityLandProtectionPlugin plugin, World world, Claim claim) {
        if (plugin == null || world == null || claim == null) {
            return;
        }
        PlayerRef ownerRef = findPlayer(world, claim.getOwner());
        if (ownerRef == null) {
            return;
        }
        LangPreferenceManager.Language lang = plugin.getEffectiveLanguage(ownerRef);
        String msg = Text.tierNoUnclaimedTerritory(lang);
        plugin.sendPlayerMessageImmediate(ownerRef, msg);
    }

    private static int clampTier(int tier) {
        if (tier < MIN_TIER) {
            return MIN_TIER;
        }
        return Math.min(tier, MAX_TIER);
    }
}
