package group.austale.deitylandprotection;

import group.austale.deitylandprotection.LangPreferenceManager;
import group.austale.deitylandprotection.DeityLandProtectionPlugin;
import group.austale.deitylandprotection.DeityLandProtectionText;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public final class LangCommand
extends AbstractAsyncCommand {
    private final DeityLandProtectionPlugin plugin;
    private final LangPreferenceManager langPreferenceManager;

    public LangCommand(@Nonnull DeityLandProtectionPlugin plugin, @Nonnull LangPreferenceManager langPreferenceManager) {
        super("deitylang", "Set DeityLandProtection language (default + per-player override)");
        this.plugin = plugin;
        this.langPreferenceManager = langPreferenceManager;
        this.setAllowsExtraArguments(true);
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        String[] parts = ctx.getInputString().trim().split("\\s+");
        String sub = parts.length >= 2 ? parts[1].trim().toLowerCase() : "status";
        CommandSender sender = ctx.sender();
        UUID senderId = sender.getUuid();
        LangPreferenceManager.Language defaultLang = this.langPreferenceManager.getDefaultLanguage();
        LangPreferenceManager.Language override = ctx.isPlayer() ? this.langPreferenceManager.getOverride(senderId) : null;
        LangPreferenceManager.Language effective = ctx.isPlayer()
                ? this.langPreferenceManager.getEffectiveLanguage(senderId)
                : defaultLang;
        if (sub.equals("help") || sub.equals("ayuda")) {
            ctx.sendMessage(Message.raw(DeityLandProtectionText.helpLang(effective)));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("status") || sub.equals("estado")) {
            ctx.sendMessage(Message.raw(DeityLandProtectionText.langStatus(effective, effective, defaultLang, override)));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("clear") || sub.equals("reset")) {
            if (!ctx.isPlayer()) {
                ctx.sendMessage(Message.raw(DeityLandProtectionText.helpLang(effective)));
                return CompletableFuture.completedFuture(null);
            }
            this.langPreferenceManager.clearOverride(senderId);
            ctx.sendMessage(Message.raw(DeityLandProtectionText.langOverrideCleared(effective)));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("default")) {
            if (parts.length < 3) {
                ctx.sendMessage(Message.raw(DeityLandProtectionText.helpLang(effective)));
                return CompletableFuture.completedFuture(null);
            }
            if (!this.canChangeDefault(ctx)) {
                ctx.sendMessage(Message.raw(DeityLandProtectionText.noPermission(effective)));
                return CompletableFuture.completedFuture(null);
            }
            LangPreferenceManager.Language newLang = LangPreferenceManager.Language.fromCode(parts[2]);
            this.langPreferenceManager.setDefaultLanguage(newLang);
            ctx.sendMessage(Message.raw(DeityLandProtectionText.langDefaultUpdated(effective, newLang)));
            return CompletableFuture.completedFuture(null);
        }
        if (sub.equals("en") || sub.equals("es")) {
            if (!ctx.isPlayer()) {
                ctx.sendMessage(Message.raw(DeityLandProtectionText.helpLang(effective)));
                return CompletableFuture.completedFuture(null);
            }
            LangPreferenceManager.Language newLang = LangPreferenceManager.Language.fromCode(sub);
            this.langPreferenceManager.setOverride(senderId, newLang);
            ctx.sendMessage(Message.raw(DeityLandProtectionText.langUpdated(newLang, newLang)));
            return CompletableFuture.completedFuture(null);
        }
        ctx.sendMessage(Message.raw(DeityLandProtectionText.helpLang(effective)));
        return CompletableFuture.completedFuture(null);
    }

    private boolean canChangeDefault(CommandContext ctx) {
        if (!ctx.isPlayer()) {
            return true;
        }
        try {
            UUID uuid = ctx.sender().getUuid();
            return this.plugin != null && uuid != null && this.plugin.isOpBypass(uuid);
        }
        catch (Exception ignored) {
            return false;
        }
    }
}



