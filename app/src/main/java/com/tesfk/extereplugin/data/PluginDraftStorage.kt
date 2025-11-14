package com.tesfk.extereplugin.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pluginDataStore by preferencesDataStore("plugin_lab")

class PluginDraftStorage(private val context: Context) {
    private val nameKey = stringPreferencesKey("name")
    private val pkgKey = stringPreferencesKey("package")
    private val descKey = stringPreferencesKey("description")
    private val versionKey = stringPreferencesKey("version")
    private val authorKey = stringPreferencesKey("author")
    private val entryKey = stringPreferencesKey("entry")
    private val savedAtKey = longPreferencesKey("saved_at")

    val draft: Flow<PluginDraft> = context.pluginDataStore.data.map { prefs ->
        PluginDraft(
            name = prefs[nameKey] ?: "",
            packageId = prefs[pkgKey] ?: "",
            description = prefs[descKey] ?: "",
            version = prefs[versionKey] ?: "1.0.0",
            author = prefs[authorKey] ?: "",
            entryPoint = prefs[entryKey] ?: PluginBlueprint.DEFAULT_ENTRY_POINT
        )
    }

    val lastSavedAt: Flow<Long> = context.pluginDataStore.data.map { prefs ->
        prefs[savedAtKey] ?: 0L
    }

    suspend fun saveDraft(draft: PluginDraft) {
        context.pluginDataStore.edit { prefs ->
            prefs[nameKey] = draft.name
            prefs[pkgKey] = draft.packageId
            prefs[descKey] = draft.description
            prefs[versionKey] = draft.version
            prefs[authorKey] = draft.author
            prefs[entryKey] = draft.entryPoint
            prefs[savedAtKey] = System.currentTimeMillis()
        }
    }
}
