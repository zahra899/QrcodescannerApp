package ui.language

data class LanguageModel(
    val name: String,
    val subText: String = "",
    var isSelected: Boolean = false
)