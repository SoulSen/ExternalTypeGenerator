package com.chattriggers

internal const val TAB = "    "

operator fun String.times(num: Int): String {
    val sb = StringBuilder()

    for (i in 1..num) {
        sb.append(this)
    }

    return sb.toString()
}

internal fun isBlocked(type: String): Boolean {
    val fixedType = type.replace("?", "")

    return BLOCKED_TYPES.contains(fixedType) || BLOCKED_TYPES.any {
        type.contains("<$it>")
    }
}

internal fun fixType(type: String): String {
    if (!isBlocked(type)) return type

    if (type.contains("<")) {
        return type.replace(
                BLOCKED_TYPES.first { type.contains(it) },
                "dynamic"
        )
    }

    return "dynamic"
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
        "RenderGameOverlayEvent.ElementType", "Minecraft",
        "UUID", "Vector2f"
)