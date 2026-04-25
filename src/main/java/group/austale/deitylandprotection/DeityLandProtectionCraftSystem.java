package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class DeityLandProtectionCraftSystem
extends EntityEventSystem<EntityStore, CraftRecipeEvent.Pre> {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionCraftSystem(DeityLandProtectionPlugin plugin) {
        super(CraftRecipeEvent.Pre.class);
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.and((Query[])new Query[]{Player.getComponentType()});
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, CraftRecipeEvent.Pre event) {
        String outId;
        MaterialQuantity out;
        CraftingRecipe recipe;
        if (event == null) {
            return;
        }
        if (this.plugin.isAllowCrafting()) {
            return;
        }
        try {
            recipe = event.getCraftedRecipe();
        }
        catch (Exception ignored) {
            return;
        }
        if (recipe == null) {
            return;
        }
        try {
            out = recipe.getPrimaryOutput();
        }
        catch (Exception ignored) {
            return;
        }
        if (out == null) {
            return;
        }
        try {
            outId = out.getItemId();
        }
        catch (Exception ignored) {
            return;
        }
        if (!this.plugin.isClaimItemId(outId)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player)chunk.getComponent(entityIndex, Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw("Crafting DeityLandProtection is disabled on this server"));
        }
    }
}



