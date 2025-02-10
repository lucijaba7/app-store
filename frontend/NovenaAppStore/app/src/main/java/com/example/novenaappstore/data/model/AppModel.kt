package com.example.novenaappstore.data.model

import java.sql.Blob

data class App(
    val id: Int,
    val appName: String,
    val fileName: String,
    val version: String,
    val packageName: String,
    val icon: String
)

enum class AppState {
    NOT_INSTALLED,
    OUTDATED,
    UP_TO_DATE
}

data class AppWithState(
    val app: App,
    val state: AppState
)