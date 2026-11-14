package com.example.data.model

/**
 * Standard Android TV Remote Protocol v2 KeyCodes (matching android.view.KeyEvent).
 */
object TvKeyCodes {
    const val HOME = 3
    const val BACK = 4
    const val DPAD_UP = 19
    const val DPAD_DOWN = 20
    const val DPAD_LEFT = 21
    const val DPAD_RIGHT = 22
    const val DPAD_CENTER = 23
    const val VOLUME_UP = 24
    const val VOLUME_DOWN = 25
    const val POWER = 26
    const val MENU = 82
    const val SEARCH = 84
    const val MEDIA_PLAY_PAUSE = 85
    const val MEDIA_STOP = 86
    const val MEDIA_NEXT = 87
    const val MEDIA_PREVIOUS = 88
    const val MEDIA_REWIND = 89
    const val MEDIA_FAST_FORWARD = 90
    const val CHANNEL_UP = 166
    const val CHANNEL_DOWN = 167
    const val INFO = 165
    const val TV_INPUT = 178
    const val CAPTIONS = 175
    const val SETTINGS = 176
    const val MUTE = 164
    const val GUIDE = 172
    const val BRIGHTNESS_DOWN = 220
    const val BRIGHTNESS_UP = 221
    const val VOICE_ASSIST = 231
    const val ALL_APPS = 284
    const val SLEEP = 223
    const val DEL = 67
    const val ENTER = 66
}

enum class CustomActionType {
    KEYCODE,
    APP_LAUNCH
}

data class TvAppCatalogItem(
    val packageName: String,
    val appName: String,
    val launchUri: String,
    val iconKey: String,
    val accentHex: Long,
    val category: String
)

object TvAppCatalog {
    val apps: List<TvAppCatalogItem> = listOf(
        TvAppCatalogItem(
            packageName = "com.google.android.youtube.tv",
            appName = "YouTube",
            launchUri = "market://launch?id=com.google.android.youtube.tv",
            iconKey = "youtube",
            accentHex = 0xFFFF3D3D,
            category = "동영상"
        ),
        TvAppCatalogItem(
            packageName = "com.netflix.ninja",
            appName = "Netflix",
            launchUri = "market://launch?id=com.netflix.ninja",
            iconKey = "netflix",
            accentHex = 0xFFE50914,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.disney.disneyplus",
            appName = "Disney+",
            launchUri = "market://launch?id=com.disney.disneyplus",
            iconKey = "disney",
            accentHex = 0xFF33A1FD,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "net.cj.cjhv.gs.tving",
            appName = "TVING",
            launchUri = "market://launch?id=net.cj.cjhv.gs.tving",
            iconKey = "tving",
            accentHex = 0xFFFF153C,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "kr.co.captv.pooqV2",
            appName = "Wavve",
            launchUri = "market://launch?id=kr.co.captv.pooqV2",
            iconKey = "wavve",
            accentHex = 0xFF2B67FF,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.coupang.mobile.play",
            appName = "쿠팡플레이",
            launchUri = "market://launch?id=com.coupang.mobile.play",
            iconKey = "coupang",
            accentHex = 0xFF00A7E1,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.frograms.watcha",
            appName = "Watcha",
            launchUri = "market://launch?id=com.frograms.watcha",
            iconKey = "watcha",
            accentHex = 0xFFFF0558,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.spotify.tv.android",
            appName = "Spotify",
            launchUri = "market://launch?id=com.spotify.tv.android",
            iconKey = "spotify",
            accentHex = 0xFF1DB954,
            category = "음악"
        ),
        TvAppCatalogItem(
            packageName = "com.amazon.amazonvideo.livingroom",
            appName = "Prime Video",
            launchUri = "market://launch?id=com.amazon.amazonvideo.livingroom",
            iconKey = "prime",
            accentHex = 0xFF00A8E1,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.apple.atve.androidtv.appletv",
            appName = "Apple TV",
            launchUri = "market://launch?id=com.apple.atve.androidtv.appletv",
            iconKey = "appletv",
            accentHex = 0xFFE5E7EB,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "tv.twitch.android.app",
            appName = "Twitch",
            launchUri = "market://launch?id=tv.twitch.android.app",
            iconKey = "twitch",
            accentHex = 0xFF9146FF,
            category = "스트리밍"
        ),
        TvAppCatalogItem(
            packageName = "com.plexapp.android",
            appName = "Plex",
            launchUri = "market://launch?id=com.plexapp.android",
            iconKey = "plex",
            accentHex = 0xFFE5A00D,
            category = "미디어 서버"
        ),
        TvAppCatalogItem(
            packageName = "org.xbmc.kodi",
            appName = "Kodi",
            launchUri = "market://launch?id=org.xbmc.kodi",
            iconKey = "kodi",
            accentHex = 0xFF17B2E7,
            category = "미디어 서버"
        ),
        TvAppCatalogItem(
            packageName = "com.google.android.videos",
            appName = "Google TV",
            launchUri = "market://launch?id=com.google.android.videos",
            iconKey = "googletv",
            accentHex = 0xFF6CD98E,
            category = "시스템"
        )
    )

    fun findByPackage(packageName: String): TvAppCatalogItem? =
        apps.firstOrNull { it.packageName.equals(packageName, ignoreCase = true) }

    fun displayNameForPackage(packageName: String): String {
        findByPackage(packageName)?.let { return it.appName }
        val tail = packageName.substringAfterLast('.').ifBlank { packageName }
        return tail.replaceFirstChar { it.uppercaseChar() }
    }

    fun launchUriForPackage(packageName: String): String {
        findByPackage(packageName)?.let { return it.launchUri }
        return "market://launch?id=$packageName"
    }

    fun iconKeyForPackage(packageName: String): String =
        findByPackage(packageName)?.iconKey ?: "app_generic"

    fun accentForPackage(packageName: String): Long =
        findByPackage(packageName)?.accentHex ?: 0xFF6CD98E
}
