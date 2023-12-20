package oppen.oppenbok.settings

sealed class SettingsOption {
    object AboutOption: SettingsOption()
    object NewOption: SettingsOption()
    data class PageOption(val pageNumber: Int): SettingsOption()
}