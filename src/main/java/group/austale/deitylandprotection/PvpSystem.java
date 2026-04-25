package group.austale.deitylandprotection;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PvpSystem
extends DamageEventSystem {
    private final DeityLandProtectionPlugin plugin;
    private static final long LOG_COOLDOWN_MS = 1500L;
    private static final ConcurrentHashMap<UUID, Long> LAST_LOG_MS = new ConcurrentHashMap();

    public PvpSystem(DeityLandProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    public SystemGroup<EntityStore> getGroup() {
        DamageModule module = DamageModule.get();
        return module == null ? null : module.getFilterDamageGroup();
    }

    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, Damage event) {
        Player victimPlayer;
        UUIDComponent victimUuidComponent;
        TransformComponent victimTransform;
        Ref victimRef;
        if (chunk == null || store == null || commandBuffer == null || event == null) {
            return;
        }
        try {
            victimRef = chunk.getReferenceTo(entityIndex);
        }
        catch (Throwable ignored) {
            return;
        }
        if (!victimRef.isValid()) {
            return;
        }
        try {
            victimTransform = (TransformComponent)commandBuffer.getComponent(victimRef, TransformComponent.getComponentType());
            victimUuidComponent = (UUIDComponent)commandBuffer.getComponent(victimRef, UUIDComponent.getComponentType());
        }
        catch (Throwable ignored) {
            return;
        }
        if (victimTransform == null || victimUuidComponent == null) {
            return;
        }
        UUID victimUuid = victimUuidComponent.getUuid();
        try {
            victimPlayer = (Player)commandBuffer.getComponent(victimRef, Player.getComponentType());
        }
        catch (Throwable ignored) {
            return;
        }
        if (victimPlayer == null) {
            return;
        }
        Damage.Source source = event.getSource();
        if (!(source instanceof Damage.EntitySource)) {
            return;
        }
        Ref attackerRef = ((Damage.EntitySource)source).getRef();
        if (!attackerRef.isValid()) {
            return;
        }
        Player attackerPlayer = null;
        try {
            attackerPlayer = (Player)commandBuffer.getComponent(attackerRef, Player.getComponentType());
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        if (attackerPlayer == null) {
            return;
        }
        UUIDComponent attackerUuidComponent = null;
        try {
            attackerUuidComponent = (UUIDComponent)commandBuffer.getComponent(attackerRef, UUIDComponent.getComponentType());
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        if (attackerUuidComponent == null) {
            return;
        }
        UUID attackerUuid = attackerUuidComponent.getUuid();
        if (attackerUuid.equals(victimUuid)) {
            return;
        }
        if (this.plugin.isOpBypass(attackerUuid) || this.plugin.isOpBypass(victimUuid)) {
            return;
        }
        int x = (int)Math.floor(victimTransform.getPosition().x);
        int z = (int)Math.floor(victimTransform.getPosition().z);
        Claim claim = this.plugin.getClaimStore().findClaimAt(x, z);
        if (claim == null) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            Long lastObj = LAST_LOG_MS.get(attackerUuid);
            long last = lastObj == null ? 0L : lastObj;
            if (lastObj == null || now - last >= 1500L) {
                LAST_LOG_MS.put(attackerUuid, now);
                if (this.plugin != null) {
                    this.plugin.getLogger().at(Level.INFO).log("DeityLandProtection DamageSeen attacker=" + String.valueOf(attackerUuid) + " victim=" + String.valueOf(victimUuid) + " pos=" + x + "," + z + " claimCenter=" + claim.getCenterX() + "," + claim.getCenterZ() + " pvpEnabled=" + claim.isPvpEnabled() + " source=" + source.getClass().getSimpleName());
                }
            }
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        if (claim.isPvpEnabled()) {
            return;
        }
        try {
            if (this.plugin != null) {
                this.plugin.getLogger().at(Level.INFO).log("DeityLandProtection PvP blocked attacker=" + String.valueOf(attackerUuid) + " victim=" + String.valueOf(victimUuid) + " pos=" + x + "," + z + " claimCenter=" + claim.getCenterX() + "," + claim.getCenterZ() + " source=" + source.getClass().getSimpleName());
            }
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        event.setCancelled(true);
        event.setAmount(0.0f);
        event.putMetaObject(Damage.BLOCKED, Boolean.TRUE);
        PlayerRef attackerRefComponent = null;
        try {
            attackerRefComponent = (PlayerRef)commandBuffer.getComponent(attackerRef, PlayerRef.getComponentType());
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        if (attackerRefComponent != null) {
            LangPreferenceManager.Language lang = this.plugin.getEffectiveLanguage(attackerRefComponent);
            this.plugin.sendPlayerMessage(attackerRefComponent, Text.pvpDisabledInArea(lang));
        }
    }
}



