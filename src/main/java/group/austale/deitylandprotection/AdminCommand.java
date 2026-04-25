package group.austale.deitylandprotection;

import group.austale.deitylandprotection.DeityLandProtectionAdminPage;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class AdminCommand
extends AbstractPlayerCommand {
    private final DeityLandProtectionPlugin plugin;

    public AdminCommand(DeityLandProtectionPlugin plugin) {
        super("admin", "Open DeityLandProtection admin panel");
        this.plugin = plugin;
    }

    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (this.plugin == null) {
            return;
        }
        UUID actor = playerRef.getUuid();
        if (actor == null || !this.plugin.isOpBypass(actor)) {
            context.sendMessage(Message.raw("No permission"));
            return;
        }
        Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return;
        }
        playerComponent.getPageManager().openCustomPage(ref, store, (CustomUIPage)new DeityLandProtectionAdminPage(this.plugin, playerRef));
    }
}



