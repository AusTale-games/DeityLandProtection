package group.austale.deitylandprotection;

import group.austale.deitylandprotection.Claim;
import group.austale.deitylandprotection.ClaimStore;
import group.austale.deitylandprotection.DeityLandProtectionAdminPage;
import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.Text;
import group.austale.deitylandprotection.UpkeepStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;

public final class DeityLandProtectionAdminActionInteraction
extends ChoiceInteraction {
    private final DeityLandProtectionPlugin plugin;
    private final Action action;
    private final int delta;

    public DeityLandProtectionAdminActionInteraction(DeityLandProtectionPlugin plugin, Action action) {
        this(plugin, action, 0);
    }

    public DeityLandProtectionAdminActionInteraction(DeityLandProtectionPlugin plugin, Action action, int delta) {
        this.plugin = plugin;
        this.action = action;
        this.delta = delta;
    }

    public void run(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        if (this.plugin == null || store == null || ref == null || playerRef == null || this.action == null) {
            return;
        }
        LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(playerRef);
        UUID actor = playerRef.getUuid();
        if (actor == null || !this.plugin.isOpBypass(actor)) {
            this.plugin.sendPlayerMessageImmediate(playerRef, Text.noPermission(lang));
            return;
        }
        String worldName = null;
        try {
            EntityStore entityStore = (EntityStore)store.getExternalData();
            if (entityStore != null && entityStore.getWorld() != null) {
                worldName = entityStore.getWorld().getName();
            }
        }
        catch (Exception ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        switch (this.action) {
            case TOGGLE_CRAFTING: {
                this.plugin.setAllowCrafting(!this.plugin.isAllowCrafting());
                break;
            }
            case RELOAD: {
                this.plugin.reloadData();
                break;
            }
            case CYCLE_RADIUS: {
                int current = this.plugin.getClaimRadius();
                int next = switch (current) {
                    case 16 -> 32;
                    case 32 -> 64;
                    case 64 -> 128;
                    default -> 16;
                };
                this.plugin.setClaimRadius(next);
                break;
            }
            case CYCLE_MAXCLAIMS: {
                int current = this.plugin.getMaxClaimsPerPlayer();
                int next = current + 1;
                if (next > 5) {
                    next = 1;
                }
                this.plugin.setMaxClaimsPerPlayer(next);
                break;
            }
            case TOGGLE_MAP_CLAIM_VISUAL: {
                this.plugin.setMapClaimVisualEnabled(!this.plugin.isMapClaimVisualEnabled());
                if (worldName != null && !worldName.isEmpty()) {
                    this.plugin.queueMapUpdateForAllClaims(worldName);
                }
                break;
            }
            case TOGGLE_UPKEEP: {
                this.plugin.setUpkeepEnabled(!this.plugin.isUpkeepEnabled());
                break;
            }
            case ADJUST_UPKEEP_GRACE: {
                this.plugin.setUpkeepGraceMinutes(this.plugin.getUpkeepGraceMinutes() + this.delta);
                break;
            }
            case CYCLE_UPKEEP_ESSENCE_COST: {
                this.plugin.cycleUpkeepEssenceCostPerHour();
                break;
            }
            case REMOVE_CLAIM_HERE: {
                Transform transform;
                try {
                    transform = playerRef.getTransform();
                }
                catch (Exception ignored) {
                    transform = null;
                }
                if (transform == null || transform.getPosition() == null) {
                    this.plugin.sendPlayerMessageImmediate(playerRef, Text.uiAdminRemoveClaimNone(lang));
                    break;
                }
                Vector3d pos = transform.getPosition();
                int x = (int)Math.floor(pos.x);
                int z = (int)Math.floor(pos.z);
                ClaimStore claims = this.plugin.getClaimStore();
                Claim claim = claims == null ? null : claims.findClaimAt(x, z);
                if (claim == null) {
                    this.plugin.sendPlayerMessageImmediate(playerRef, Text.uiAdminRemoveClaimNone(lang));
                    break;
                }
                if (worldName != null && !worldName.isEmpty()) {
                    this.plugin.queueMapUpdateForClaim(worldName, claim);
                }
                if (claims != null) {
                    claims.removeClaimAt(claim.getCenterX(), claim.getCenterZ());
                }
                UpkeepStore upkeep = this.plugin.getUpkeepStore();
                if (upkeep != null) {
                    upkeep.remove(claim.getCenterX(), claim.getCenterZ());
                    upkeep.markDirty();
                }
                this.plugin.clearBorderForClaim(claim.getCenterX(), claim.getCenterZ());
                this.plugin.sendPlayerMessageImmediate(playerRef, Text.protectionRemoved(lang));
                break;
            }
        }
        Player playerEntity = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerEntity == null) {
            return;
        }
        PageManager pages = playerEntity.getPageManager();
        if (pages == null) {
            return;
        }
        pages.openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionAdminPage(this.plugin, playerRef));
    }

    public static enum Action {
        TOGGLE_CRAFTING,
        RELOAD,
        CYCLE_RADIUS,
        CYCLE_MAXCLAIMS,
        TOGGLE_MAP_CLAIM_VISUAL,
        TOGGLE_UPKEEP,
        ADJUST_UPKEEP_GRACE,
        CYCLE_UPKEEP_ESSENCE_COST,
        REMOVE_CLAIM_HERE;

    }
}



