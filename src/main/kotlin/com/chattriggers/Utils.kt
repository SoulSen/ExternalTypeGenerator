package com.chattriggers

internal const val TAB = "    "

operator fun String.times(num: Int): String {
    val sb = StringBuilder()

    for (i in 1..num) {
        sb.append(this)
    }

    return sb.toString()
}

internal val BLOCKED_TYPES = setOf(
        "ScriptObjectMirror", "ClientChatReceivedEvent", "MouseEvent",
        "RenderGameOverlayEvent", "IChatComponent", "PlaySoundEvent",
        "ConfigChangedEvent.OnConfigChangedEvent", "File", "BufferedImage",
        "DynamicTexture", "FontRenderer", "ILoader", "NBTTagList",
        "GuiScreen", "TickEvent.ClientTickEvent", "KeyBinding",
        "ITextComponent", "NetHandlerPlayClient", "GuiNewChat",
        "MCChunk", "Class<*>", "MCEntity", "IInventory",
        "Container", "ItemStack", "EntityItem",
        "MCParticle", "EntityPlayer", "MCPotionEffect",
        "EntityPlayerSP", "Score", "WorldClient", "TriggerType",
        "net.minecraft.scoreboard.Score", "IBlockState",
        "MCBlock", "BlockPos", "GuiButton",
        "RenderGameOverlayEvent.ElementType", "Minecraft"
)