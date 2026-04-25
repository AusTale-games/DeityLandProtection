package group.austale.deitylandprotection;

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

public final class MyClaimsCommand
extends AbstractPlayerCommand {
    private final DeityLandProtectionPlugin plugin;

    public MyClaimsCommand(DeityLandProtectionPlugin plugin) {
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
        playerComponent.getPageManager().openCustomPage(ref, store, (CustomUIPage)new PlayerOverviewPage(this.plugin, playerRef));
    }
}



