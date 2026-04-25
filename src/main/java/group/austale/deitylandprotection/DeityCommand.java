package group.austale.deitylandprotection;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public final class DeityCommand
extends AbstractAsyncCommand {
    private final DeityLandProtectionPlugin plugin;

    public DeityCommand(DeityLandProtectionPlugin plugin) {
        super("deity", "DeityLandProtection admin commands");
        this.plugin = plugin;
        this.addSubCommand((AbstractCommand)new AdminCommand(plugin));
        this.addSubCommand((AbstractCommand)new MyClaimsCommand(plugin));
        this.setAllowsExtraArguments(true);
    }

    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        String[] parts;
        String input = ctx.getInputString();
        String[] stringArray = parts = input == null ? new String[]{} : input.trim().split("\\s+");
        if (!this.isAllowed(ctx)) {
            ctx.sendMessage(Message.raw("No permission"));
            return CompletableFuture.completedFuture(null);
        }
        if (parts.length <= 1) {
            this.sendUsage(ctx);
            ctx.sendMessage(Message.raw(("Current radius: " + this.plugin.getClaimRadius())));
            ctx.sendMessage(Message.raw(("Current max claims per player: " + this.plugin.getMaxClaimsPerPlayer())));
            ctx.sendMessage(Message.raw(("Crafting allowed: " + this.plugin.isAllowCrafting())));
            return CompletableFuture.completedFuture(null);
        }
        String sub = parts[1].toLowerCase();
        if (sub.equals("trust")) {
            return this.handleTrust(ctx, parts);
        }
        if (sub.equals("upkeep")) {
            return this.handleUpkeep(ctx, parts);
        }
        if (sub.equals("reload")) {
            return this.handleReload(ctx);
        }
        if (sub.equals("radius")) {
            int radius;
            if (parts.length == 2) {
                ctx.sendMessage(Message.raw(("Current radius: " + this.plugin.getClaimRadius())));
                return CompletableFuture.completedFuture(null);
            }
            try {
                radius = Integer.parseInt(parts[2]);
            }
            catch (Exception e) {
                ctx.sendMessage(Message.raw("Invalid radius. Allowed: 16, 32, 64, 128"));
                return CompletableFuture.completedFuture(null);
            }
            boolean ok = this.plugin.setClaimRadius(radius);
            if (!ok) {
                ctx.sendMessage(Message.raw("Invalid radius. Allowed: 16, 32, 64, 128"));
                return CompletableFuture.completedFuture(null);
            }
            ctx.sendMessage(Message.raw(("DeityLandProtection radius set to " + this.plugin.getClaimRadius())));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("maxclaims")) {
            int max;
            if (parts.length == 2) {
                ctx.sendMessage(Message.raw(("Current max claims per player: " + this.plugin.getMaxClaimsPerPlayer())));
                return CompletableFuture.completedFuture(null);
            }
            try {
                max = Integer.parseInt(parts[2]);
            }
            catch (Exception e) {
                ctx.sendMessage(Message.raw("Invalid max claims. Allowed: 1-5"));
                return CompletableFuture.completedFuture(null);
            }
            this.plugin.setMaxClaimsPerPlayer(max);
            ctx.sendMessage(Message.raw(("Max claims per player set to " + this.plugin.getMaxClaimsPerPlayer())));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("crafting")) {
            if (parts.length == 2) {
                ctx.sendMessage(Message.raw(("Crafting allowed: " + this.plugin.isAllowCrafting())));
                return CompletableFuture.completedFuture(null);
            }
            String arg = parts[2].toLowerCase();
            if (arg.equals("on") || arg.equals("true") || arg.equals("yes")) {
                this.plugin.setAllowCrafting(true);
            } else if (arg.equals("off") || arg.equals("false") || arg.equals("no")) {
                this.plugin.setAllowCrafting(false);
            } else {
                ctx.sendMessage(Message.raw("Usage: /deity crafting <on|off>"));
                return CompletableFuture.completedFuture(null);
            }
            ctx.sendMessage(Message.raw(("Crafting allowed: " + this.plugin.isAllowCrafting())));
            return CompletableFuture.completedFuture(null);
        }
        this.sendUsage(ctx);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handleUpkeep(CommandContext ctx, String[] parts) {
        if (this.plugin == null) {
            ctx.sendMessage(Message.raw("DeityLandProtection plugin not available"));
            return CompletableFuture.completedFuture(null);
        }
        if (parts.length <= 2 || parts[2].equalsIgnoreCase("status")) {
            ctx.sendMessage(Message.raw(("Upkeep enabled: " + this.plugin.isUpkeepEnabled())));
            ctx.sendMessage(Message.raw(("Upkeep grace minutes: " + this.plugin.getUpkeepGraceMinutes())));
            ctx.sendMessage(Message.raw(("Upkeep essence cost/hour: " + this.plugin.getUpkeepEssenceCostPerHour())));
            return CompletableFuture.completedFuture(null);
        }
        String action = parts[2].toLowerCase();
        if (action.equals("enable") || action.equals("enabled")) {
            if (parts.length < 4) {
                ctx.sendMessage(Message.raw("Usage: /deity upkeep enable <on|off|toggle|status>"));
                return CompletableFuture.completedFuture(null);
            }
            String arg = parts[3].toLowerCase();
            if (arg.equals("status")) {
                ctx.sendMessage(Message.raw(("Upkeep enabled: " + this.plugin.isUpkeepEnabled())));
                return CompletableFuture.completedFuture(null);
            }
            if (arg.equals("toggle")) {
                this.plugin.setUpkeepEnabled(!this.plugin.isUpkeepEnabled());
                ctx.sendMessage(Message.raw(("Upkeep enabled: " + this.plugin.isUpkeepEnabled())));
                return CompletableFuture.completedFuture(null);
            }
            if (arg.equals("on") || arg.equals("true") || arg.equals("yes")) {
                this.plugin.setUpkeepEnabled(true);
                ctx.sendMessage(Message.raw(("Upkeep enabled: " + this.plugin.isUpkeepEnabled())));
                return CompletableFuture.completedFuture(null);
            }
            if (arg.equals("off") || arg.equals("false") || arg.equals("no")) {
                this.plugin.setUpkeepEnabled(false);
                ctx.sendMessage(Message.raw(("Upkeep enabled: " + this.plugin.isUpkeepEnabled())));
                return CompletableFuture.completedFuture(null);
            }
            ctx.sendMessage(Message.raw("Usage: /deity upkeep enable <on|off|toggle|status>"));
            return CompletableFuture.completedFuture(null);
        }
        if (action.equals("grace")) {
            int minutes;
            if (parts.length < 4) {
                ctx.sendMessage(Message.raw("Usage: /deity upkeep grace <minutes>"));
                return CompletableFuture.completedFuture(null);
            }
            try {
                minutes = Integer.parseInt(parts[3]);
            }
            catch (Exception e) {
                ctx.sendMessage(Message.raw("Invalid minutes"));
                return CompletableFuture.completedFuture(null);
            }
            this.plugin.setUpkeepGraceMinutes(minutes);
            ctx.sendMessage(Message.raw(("Upkeep grace minutes set to " + this.plugin.getUpkeepGraceMinutes())));
            return CompletableFuture.completedFuture(null);
        }
        if (action.equals("cost") || action.equals("essence") || action.equals("essencecost")) {
            int cost;
            if (parts.length < 4) {
                ctx.sendMessage(Message.raw("Usage: /deity upkeep cost <essencePerHour>"));
                return CompletableFuture.completedFuture(null);
            }
            try {
                cost = Integer.parseInt(parts[3]);
            }
            catch (Exception e) {
                ctx.sendMessage(Message.raw("Invalid essence cost"));
                return CompletableFuture.completedFuture(null);
            }
            this.plugin.setUpkeepEssenceCostPerHour(cost);
            ctx.sendMessage(Message.raw(("Upkeep essence cost/hour set to " + this.plugin.getUpkeepEssenceCostPerHour())));
            return CompletableFuture.completedFuture(null);
        }
        ctx.sendMessage(Message.raw("Usage: /deity upkeep status"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep enable <on|off|toggle|status>"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep grace <minutes>"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep cost <essencePerHour>"));
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handleReload(CommandContext ctx) {
        if (!this.isAllowed(ctx)) {
            ctx.sendMessage(Message.raw("No permission"));
            return CompletableFuture.completedFuture(null);
        }
        if (this.plugin != null) {
            this.plugin.reloadData();
            ctx.sendMessage(Message.raw("DeityLandProtection reloaded"));
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handleTrust(CommandContext ctx, String[] parts) {
        Claim claim;
        int actionIndex;
        if (parts.length < 4) {
            this.sendTrustUsage(ctx);
            return CompletableFuture.completedFuture(null);
        }
        ClaimStore store = this.plugin.getClaimStore();
        if (parts[2].equalsIgnoreCase("here")) {
            actionIndex = 3;
            Claim at = this.resolveClaimAtSender(ctx);
            if (at == null) {
                ctx.sendMessage(Message.raw("No claim found at your position"));
                return CompletableFuture.completedFuture(null);
            }
            claim = at;
        } else {
            int centerZ;
            int centerX;
            if (parts.length < 5) {
                this.sendTrustUsage(ctx);
                return CompletableFuture.completedFuture(null);
            }
            actionIndex = 4;
            try {
                centerX = Integer.parseInt(parts[2]);
                centerZ = Integer.parseInt(parts[3]);
            }
            catch (Exception e) {
                ctx.sendMessage(Message.raw("Invalid center coordinates"));
                return CompletableFuture.completedFuture(null);
            }
            claim = store.findClaimByCenter(centerX, centerZ);
            if (claim == null) {
                ctx.sendMessage(Message.raw(("No claim found at center " + centerX + "," + centerZ)));
                return CompletableFuture.completedFuture(null);
            }
        }
        String action = parts[actionIndex].toLowerCase();
        if (action.equals("list")) {
            Map<UUID, Integer> trusted = claim.getTrusted();
            if (trusted.isEmpty()) {
                ctx.sendMessage(Message.raw("No trusted players on this claim"));
                return CompletableFuture.completedFuture(null);
            }
            ctx.sendMessage(Message.raw("Trusted players:"));
            for (Map.Entry<UUID, Integer> e : trusted.entrySet()) {
                ctx.sendMessage(Message.raw(("- " + String.valueOf(e.getKey()) + " = " + String.valueOf(e.getValue()))));
            }
            return CompletableFuture.completedFuture(null);
        }
        if (action.equals("remove")) {
            if (parts.length <= actionIndex + 1) {
                ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> remove <playerName|playerUuid>"));
                ctx.sendMessage(Message.raw("Usage: /deity trust here remove <playerName|playerUuid>"));
                return CompletableFuture.completedFuture(null);
            }
            UUID target = this.resolvePlayerUuid(parts[actionIndex + 1]);
            if (target == null) {
                ctx.sendMessage(Message.raw(("Unknown player: " + parts[actionIndex + 1] + " (they must have joined at least once)")));
                return CompletableFuture.completedFuture(null);
            }
            claim.removeTrusted(target);
            store.markDirty();
            ctx.sendMessage(Message.raw(("Removed trust for " + String.valueOf(target))));
            return CompletableFuture.completedFuture(null);
        }
        if (action.equals("add") || action.equals("set")) {
            if (parts.length <= actionIndex + 1) {
                ctx.sendMessage(Message.raw(("Usage: /deity trust <centerX> <centerZ> " + action + " <playerName|playerUuid> [place|break|use|all|none]")));
                ctx.sendMessage(Message.raw(("Usage: /deity trust here " + action + " <playerName|playerUuid> [place|break|use|all|none]")));
                return CompletableFuture.completedFuture(null);
            }
            UUID target = this.resolvePlayerUuid(parts[actionIndex + 1]);
            if (target == null) {
                ctx.sendMessage(Message.raw(("Unknown player: " + parts[actionIndex + 1] + " (they must have joined at least once)")));
                return CompletableFuture.completedFuture(null);
            }
            int perms = parts.length > actionIndex + 2 ? DeityCommand.parsePerms(parts[actionIndex + 2]) : 7;
            claim.setTrusted(target, perms);
            store.markDirty();
            ctx.sendMessage(Message.raw(("Trust updated for " + String.valueOf(target) + " perms=" + claim.getPermissionsFor(target))));
            return CompletableFuture.completedFuture(null);
        }
        this.sendTrustUsage(ctx);
        return CompletableFuture.completedFuture(null);
    }

    private void sendUsage(CommandContext ctx) {
        ctx.sendMessage(Message.raw("Usage: /deity admin"));
        ctx.sendMessage(Message.raw("Usage: /deity my"));
        ctx.sendMessage(Message.raw("Usage: /deity radius <16|32|64|128>"));
        ctx.sendMessage(Message.raw("Usage: /deity maxclaims <1-5>"));
        ctx.sendMessage(Message.raw("Usage: /deity crafting <on|off>"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep status"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep grace <minutes>"));
        ctx.sendMessage(Message.raw("Usage: /deity upkeep cost <essencePerHour>"));
        ctx.sendMessage(Message.raw("Usage: /deity reload"));
        ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> <list|add|remove|set> ..."));
        ctx.sendMessage(Message.raw("Usage: /deity trust here <list|add|remove|set> ..."));
    }

    private void sendTrustUsage(CommandContext ctx) {
        ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> list"));
        ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> add <playerName|playerUuid> [place|break|use|all|none]"));
        ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> set <playerName|playerUuid> [place|break|use|all|none]"));
        ctx.sendMessage(Message.raw("Usage: /deity trust <centerX> <centerZ> remove <playerName|playerUuid>"));
        ctx.sendMessage(Message.raw("Usage: /deity trust here <list|add|remove|set> ..."));
    }

    private UUID resolvePlayerUuid(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(s);
        }
        catch (Exception exception) {
            if (this.plugin != null) {
                return this.plugin.getKnownUuidForUsername(s);
            }
            return null;
        }
    }

    private Claim resolveClaimAtSender(CommandContext ctx) {
        CommandSender sender;
        if (this.plugin == null || ctx == null || !ctx.isPlayer()) {
            return null;
        }
        try {
            sender = ctx.sender();
        }
        catch (Exception ignored) {
            sender = null;
        }
        if (sender == null) {
            return null;
        }
        Vector3d pos = null;
        try {
            Method pm;
            Object p;
            Method m = sender.getClass().getMethod("getTransform", new Class[0]);
            Object t = m.invoke(sender, new Object[0]);
            if (t != null && (p = (pm = t.getClass().getMethod("getPosition", new Class[0])).invoke(t, new Object[0])) instanceof Vector3d) {
                pos = (Vector3d)p;
            }
        }
        catch (Throwable ignored) {
            // best-effort: swallowing a non-fatal failure
        }
        if (pos == null) {
            return null;
        }
        int x = (int)Math.floor(pos.x);
        int z = (int)Math.floor(pos.z);
        return this.plugin.getClaimStore().findClaimAt(x, z);
    }

    private static int parsePerms(String raw) {
        if (raw == null) {
            return 0;
        }
        String r = raw.trim().toLowerCase();
        if (r.isEmpty()) {
            return 0;
        }
        if (r.equals("none")) {
            return 0;
        }
        if (r.equals("all")) {
            return 7;
        }
        if (r.equals("place")) {
            return 1;
        }
        if (r.equals("break")) {
            return 2;
        }
        if (r.equals("use")) {
            return 4;
        }
        return 0;
    }

    private boolean isAllowed(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            return true;
        }
        try {
            return this.plugin.isOpBypass(ctx.sender().getUuid());
        }
        catch (Exception ignored) {
            return false;
        }
    }
}



