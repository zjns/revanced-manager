package app.revanced.manager.network.api

import android.os.Build
import app.revanced.manager.domain.manager.PreferencesManager
import app.revanced.manager.network.dto.ReVancedRelease
import app.revanced.manager.network.service.ReVancedService
import app.revanced.manager.network.utils.getOrThrow
import app.revanced.manager.network.utils.transform

class ReVancedAPI(
    private val service: ReVancedService,
    private val prefs: PreferencesManager
) {
    private suspend fun apiUrl() = prefs.api.get()

    suspend fun getContributors() = service.getContributors(apiUrl()).transform { it.repositories }

    suspend fun getLatestRelease(name: String) =
        service.getLatestRelease(apiUrl(), name).transform { it.release }

    suspend fun getReleases(name: String) =
        service.getReleases(apiUrl(), name).transform { it.releases }

    suspend fun getAppUpdate() =
        getLatestRelease("revanced-manager")
            .getOrThrow()
            .takeIf { it.version != Build.VERSION.RELEASE }

    suspend fun getInfo(api: String? = null) = service.getInfo(api ?: apiUrl()).transform { it.info }


    companion object Extensions {
        fun ReVancedRelease.findAssetByType(mime: String) =
            assets.singleOrNull { it.contentType == mime } ?: throw MissingAssetException(mime)
    }
}

class MissingAssetException(type: String) : Exception("No asset with type $type")