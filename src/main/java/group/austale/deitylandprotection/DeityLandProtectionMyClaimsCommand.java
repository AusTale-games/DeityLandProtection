/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.command.system.CommandContext
 *  com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  javax.annotation.Nonnull
 */
package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionPlayerOverviewPage;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class DeityLandProtectionMyClaimsCommand
extends AbstractPlayerCommand {
    private final DeityLandProtectionPlugin plugin;

    public DeityLandProtectionMyClaimsCommand(DeityLandProtectionPlugin plugin) {
        super("my", "Open your DeityLandProtection claims overview");
        this.plugin = plugin;
        this.addAliases(new String[]{"claims", "me"});
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (this.plugin == null) {
            return;
        }
        Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return;
        }
        playerComponent.getPageManager().openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionPlayerOverviewPage(this.plugin, playerRef));
    }
}



