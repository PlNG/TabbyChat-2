package mnm.mods.tabbychat.client

import mnm.mods.tabbychat.CHATBOX
import mnm.mods.tabbychat.STARTUP
import mnm.mods.tabbychat.TabbyChat
import mnm.mods.tabbychat.api.events.MessageAddedToChannelEvent
import mnm.mods.tabbychat.client.extra.ChatAddonAntiSpam
import mnm.mods.tabbychat.client.extra.ChatLogging
import mnm.mods.tabbychat.client.extra.filters.FilterAddon
import mnm.mods.tabbychat.client.extra.spell.JazzySpellcheck
import mnm.mods.tabbychat.client.extra.spell.Spellcheck
import mnm.mods.tabbychat.client.extra.spell.WordListDownloader
import mnm.mods.tabbychat.client.gui.GuiNewChatTC
import mnm.mods.tabbychat.client.gui.NotificationToast
import mnm.mods.tabbychat.client.settings.ServerSettings
import mnm.mods.tabbychat.client.settings.TabbySettings
import mnm.mods.tabbychat.util.ChatTextUtils
import mnm.mods.tabbychat.util.listen
import mnm.mods.tabbychat.util.mc
import net.minecraft.client.gui.IngameGui
import net.minecraft.client.gui.NewChatGui
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.resources.IResourceManager
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.resource.IResourceType
import net.minecraftforge.resource.ISelectiveResourceReloadListener
import net.minecraftforge.resource.VanillaResourceType
import java.util.function.Predicate

object TabbyChatClient {
    val spellcheck = Spellcheck()

    val settings: TabbySettings = TabbySettings(TabbyChat.dataFolder).apply {
        TabbyChat.logger.info("Loading TabbyChat settings")
        load()
    }

    lateinit var serverSettings: ServerSettings

    init {
        MinecraftForge.EVENT_BUS.listen(EventPriority.LOW, listener = this::removeChannelTags)
        MinecraftForge.EVENT_BUS.register(StartListener)
        MinecraftForge.EVENT_BUS.register(ChatAddonAntiSpam)
        MinecraftForge.EVENT_BUS.register(FilterAddon)
        MinecraftForge.EVENT_BUS.register(ChatLogging)
    }

    private fun removeChannelTags(event: MessageAddedToChannelEvent.Pre) {
        if (settings.advanced.hideTag && event.channel !== DefaultChannel) {
            val pattern = serverSettings.general.channelPattern

            val text = event.text
            if (text != null) {
                val matcher = pattern.pattern.matcher(text.string)
                if (matcher.find()) {
                    event.text = ChatTextUtils.subChat(text, matcher.end())
                }
            }
        }
    }

    private object StartListener {
        var IngameGui.chat: NewChatGui
            get() = this.chatGUI
            set(chat) {
                try {
                    ObfuscationReflectionHelper.setPrivateValue(IngameGui::class.java, this, chat, "field_73840_e")
                    TabbyChat.logger.info(STARTUP, "Successfully hooked into chat.")
                } catch (e: Throwable) {
                    TabbyChat.logger.fatal(STARTUP, "Unable to hook into chat. This is bad.", e)
                }
            }

        @SubscribeEvent
        fun onGuiOpen(event: TickEvent.ClientTickEvent) {
            // Do the first tick, then unregister self.
            // essentially an on-thread startup complete listener
            mc.ingameGUI.chat = GuiNewChatTC

            // init spellchecking to load languages
            spellcheck.loadCurrentLanguage()
            (mc.resourceManager as IReloadableResourceManager).addReloadListener(object : ISelectiveResourceReloadListener {
                override fun onResourceManagerReload(resourceManager: IResourceManager, resourcePredicate: Predicate<IResourceType>) {
                    if (resourcePredicate.test(VanillaResourceType.LANGUAGES)) {
                        spellcheck.loadCurrentLanguage()
                    }
                }
            })

            // unregister self so it doesn't get called again.
            MinecraftForge.EVENT_BUS.unregister(StartListener)
        }
    }

    @SubscribeEvent
    fun onClientLogin(event: ClientPlayerNetworkEvent.LoggedInEvent) {
        // load up the current server's settings
        val connection = mc.connection

        val address = connection?.networkManager?.remoteAddress
        if (address != null) {
            TabbyChat.logger.info("Loading settings for server $address")
            serverSettings = ServerSettings(TabbyChat.dataFolder, address).apply {
                load()
            }
            // load chat
            try {
                ChatManager.loadFrom(serverSettings.config.nioPath.parent)
            } catch (e: Exception) {
                TabbyChat.logger.warn(CHATBOX, "Unable to load chat data.", e)
            }

            if (spellcheck is JazzySpellcheck && spellcheck.wordLists.getMissingLocales().isNotEmpty()) {
                val title = TranslationTextComponent("tabbychat.spelling.missing")
                mc.toastGui.add(NotificationToast("Spellcheck", title))
            }
        }
    }
}
