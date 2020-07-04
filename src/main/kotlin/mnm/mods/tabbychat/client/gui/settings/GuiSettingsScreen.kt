package mnm.mods.tabbychat.client.gui.settings

import mnm.mods.tabbychat.CONFIG
import mnm.mods.tabbychat.TabbyChat
import mnm.mods.tabbychat.api.Channel
import mnm.mods.tabbychat.client.DefaultChannel
import mnm.mods.tabbychat.client.TabbyChatClient
import mnm.mods.tabbychat.client.gui.component.ComponentScreen
import mnm.mods.tabbychat.client.gui.component.GuiButton
import mnm.mods.tabbychat.client.gui.component.GuiPanel
import mnm.mods.tabbychat.client.gui.component.layout.BorderLayout
import mnm.mods.tabbychat.client.gui.component.layout.VerticalLayout
import mnm.mods.tabbychat.util.Color
import mnm.mods.tabbychat.util.Location
import mnm.mods.tabbychat.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.util.text.StringTextComponent
import kotlin.reflect.KClass

class GuiSettingsScreen(channel: Channel?) : ComponentScreen(StringTextComponent("Settings")) {

    private lateinit var settingsPanel: GuiPanel
    private lateinit var settingsList: GuiPanel

    private var selectedSetting: SettingPanel<*>? = null

    init {
        if (channel !== DefaultChannel) {
            selectedSetting = GuiSettingsChannel(channel?.name)
        }
    }

    override fun init() {
        val panels = mutableListOf<SettingPanel<*>>()
        for ((key, value) in settings) {
            try {
                panels.add(selectedSetting?.takeIf { key.isInstance(it) }?.also {
                    it.parent = null
                } ?: value())
            } catch (e: Exception) {
                TabbyChat.logger.error(CONFIG, "Unable to add {} as a setting.", key, e)
            }

        }

        settingsPanel = panel.add(GuiPanel()) {
            layout = BorderLayout()
            val x = width / 2 - 300 / 2
            val y = height / 2 - 200 / 2
            location = Location(x, y, 300, 200)
        }

        val panel = this.settingsPanel.add(GuiPanel(), BorderLayout.Position.WEST) {
            layout = BorderLayout()
        }
        settingsList = panel.add(GuiPanel(), BorderLayout.Position.WEST) {
            layout = VerticalLayout()
        }

        panel.add(GuiButton("Close") {
            mc.displayGuiScreen(null)
        }, BorderLayout.Position.SOUTH).apply {
            location = Location(0, 0, 40, 10)
            secondaryColor = Color(0, 255, 0, 127)
        }

        // Populate the settings
        for (sett in panels) {
            settingsList.add(SettingsButton(sett) {
                selectedSetting?.apply {
                    settingsPanel.remove(this)
                }
                selectSetting(it)
            })
            sett.initGUI()
        }
        selectSetting(selectedSetting ?: panels[0])
    }

    override fun removed() {
        TabbyChatClient.settings.save()
        TabbyChatClient.serverSettings.save()
    }

    override fun init(mc: Minecraft, width: Int, height: Int) {
        selectedSetting?.apply {
            parent = null
            clear()
        }
        super.init(mc, width, height)
    }

    private fun deactivateAll() {
        for (comp in settingsList.children()) {
            if (comp is SettingsButton) {
                comp.displayed = false
            }
        }
    }

    private fun <T : SettingPanel<*>> activate(settingClass: Class<T>) {
        for (comp in settingsList.children()) {
            if (comp is SettingsButton && comp.settings.javaClass == settingClass) {
                comp.displayed = true
                break
            }
        }
    }

    override fun render(mouseX: Int, mouseY: Int, tick: Float) {
        val rect = settingsPanel.location
        fill(rect.xPos, rect.yPos, rect.xWidth, rect.yHeight, Integer.MIN_VALUE)
        super.render(mouseX, mouseY, tick)
    }

    private fun selectSetting(setting: SettingPanel<*>) {
        deactivateAll()
        selectedSetting = settingsPanel.add(setting, BorderLayout.Position.CENTER) {
            activate(this.javaClass)
        }
    }

    companion object {
        private val settings: Map<KClass<out SettingPanel<*>>, () -> SettingPanel<*>> = mapOf(
                GuiSettingsGeneral::class to { GuiSettingsGeneral() },
                GuiSettingsServer::class to { GuiSettingsServer() },
                GuiSettingsChannel::class to { GuiSettingsChannel() },
                GuiSettingsFilters::class to { GuiSettingsFilters() },
                GuiAdvancedSettings::class to { GuiAdvancedSettings() },
                GuiSettingsSpelling::class to { GuiSettingsSpelling() }
        )
    }
}
