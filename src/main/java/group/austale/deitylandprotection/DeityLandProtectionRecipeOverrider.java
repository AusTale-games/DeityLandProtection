package group.austale.deitylandprotection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites bundled recipe JSON in-memory so that the runtime materials and
 * tier-upgrade requirements reflect the live config. The original resource
 * files on disk are never modified; we only transform the content right
 * before {@link DeityLandProtectionAssetInstaller} writes the per-server copy.
 */
final class DeityLandProtectionRecipeOverrider {
    private final DeityLandProtectionPlugin plugin;

    DeityLandProtectionRecipeOverrider(DeityLandProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    String applyOverrides(String resourcePath, String content) {
        if (content == null || content.isEmpty() || resourcePath == null) {
            return content;
        }
        boolean isSlumberingDeity = resourcePath.endsWith("SlumberingDeity_Block.json");
        boolean isOutlanderDeity = resourcePath.endsWith("OutlanderDeity_Block.json");
        if (!isSlumberingDeity && !isOutlanderDeity) {
            return content;
        }
        String adjusted = content;
        if (isSlumberingDeity) {
            adjusted = replaceRecipeQuantity(adjusted, "Rock_Stone_Cobble", this.plugin.getSlumberingRecipeCobbleCost());
            adjusted = replaceRecipeQuantity(adjusted, "Ingredient_Life_Essence", this.plugin.getSlumberingRecipeEssenceCost());
        }
        if (isOutlanderDeity) {
            adjusted = replaceRecipeQuantity(adjusted, "Rock_Slate_Cobble", this.plugin.getOutlanderRecipeCobbleCost());
            adjusted = replaceRecipeQuantity(adjusted, "Ingredient_Void_Essence", this.plugin.getOutlanderRecipeEssenceCost());
        }
        adjusted = applyUpgradeRequirementOverrides(adjusted);
        return adjusted;
    }

    private String applyUpgradeRequirementOverrides(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String tier2Materials = buildUpgradeMaterialsJson(this.plugin.getUpgradeTier2ItemId(), this.plugin.getUpgradeTier2ItemQuantity(), null, 0);
        String tier3Materials = buildUpgradeMaterialsJson(this.plugin.getUpgradeTier3ItemId(), this.plugin.getUpgradeTier3ItemQuantity(), null, 0);
        String tier4Materials = buildUpgradeMaterialsJson(this.plugin.getUpgradeTier4PrimaryItemId(), this.plugin.getUpgradeTier4PrimaryItemQuantity(), this.plugin.getUpgradeTier4SecondaryItemId(), this.plugin.getUpgradeTier4SecondaryItemQuantity());
        String adjusted = content;
        adjusted = replaceUpgradeRequirementMaterials(adjusted, 1, tier2Materials);
        adjusted = replaceUpgradeRequirementMaterials(adjusted, 2, tier3Materials);
        adjusted = replaceUpgradeRequirementMaterials(adjusted, 3, tier4Materials);
        return adjusted;
    }

    private static String replaceRecipeQuantity(String content, String itemId, int quantity) {
        if (content == null || content.isEmpty() || itemId == null || itemId.isEmpty()) {
            return content;
        }
        int safeQuantity = Math.max(0, quantity);
        Pattern pattern = Pattern.compile("(\\\"ItemId\\\"\\s*:\\s*\\\"" + Pattern.quote(itemId) + "\\\"\\s*,\\s*\\\"Quantity\\\"\\s*:\\s*)\\d+");
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            replaced = true;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + safeQuantity));
        }
        if (!replaced) {
            return content;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String buildUpgradeMaterialsJson(String firstItemId, int firstQuantity, String secondItemId, int secondQuantity) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n              {\n                \"ItemId\": \"").append(escapeJson(firstItemId)).append("\",\n                \"Quantity\": ").append(Math.max(0, firstQuantity)).append("\n              }");
        if (secondItemId != null && !secondItemId.trim().isEmpty()) {
            sb.append(",\n              {\n                \"ItemId\": \"").append(escapeJson(secondItemId)).append("\",\n                \"Quantity\": ").append(Math.max(0, secondQuantity)).append("\n              }");
        }
        sb.append("\n            ");
        return sb.toString();
    }

    private static String replaceUpgradeRequirementMaterials(String content, int requirementIndex, String replacementMaterialsJson) {
        if (content == null || content.isEmpty() || requirementIndex < 1 || replacementMaterialsJson == null) {
            return content;
        }
        Pattern pattern = Pattern.compile("(\\\"UpgradeRequirement\\\"\\s*:\\s*\\{\\s*\\\"Material\\\"\\s*:\\s*\\[)(.*?)(\\]\\s*,\\s*\\\"TimeSeconds\\\"\\s*:\\s*\\d+\\s*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        int index = 0;
        boolean replaced = false;
        while (matcher.find()) {
            ++index;
            if (index == requirementIndex) {
                replaced = true;
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + replacementMaterialsJson + matcher.group(3)));
                continue;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
        if (!replaced) {
            return content;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
