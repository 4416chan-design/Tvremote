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

data class TvKeyActionOption(
    val id: String,
    val label: String,
    val keyCode: Int,
    val iconKey: String,
    val description: String
)

data class TvAppCatalogItem(
    val packageName: String,
    val appName: String,
    val launchUri: String,
    val iconKey: String,
    val accentHex: Long,
    val category: String
)

object TvAppCatalog {
    val keyActionOptions: List<TvKeyActionOption> = listOf(
        TvKeyActionOption("input", "외부입력", TvKeyCodes.TV_INPUT, "input", "HDMI/외부입력 전환"),
        TvKeyActionOption("captions", "자막", TvKeyCodes.CAPTIONS, "captions", "자막 켜기/끄기"),
        TvKeyActionOption("channel_up", "채널 +", TvKeyCodes.CHANNEL_UP, "channel_up", "다음 채널 이동"),
        TvKeyActionOption("channel_down", "채널 -", TvKeyCodes.CHANNEL_DOWN, "channel_down", "이전 채널 이동"),
        TvKeyActionOption("info", "정보", TvKeyCodes.INFO, "info", "프로그램/영상 정보"),
        TvKeyActionOption("menu", "메뉴", TvKeyCodes.MENU, "menu", "TV 옵션 메뉴 열기"),
        TvKeyActionOption("guide", "편성표", TvKeyCodes.GUIDE, "guide", "TV 채널 편성표"),
        TvKeyActionOption("tv_settings", "TV 설정", TvKeyCodes.SETTINGS, "tv_settings", "안드로이드 TV 설정"),
        TvKeyActionOption("next_track", "다음 영상", TvKeyCodes.MEDIA_NEXT, "next", "다음 콘텐츠/트랙"),
        TvKeyActionOption("prev_track", "이전 영상", TvKeyCodes.MEDIA_PREVIOUS, "prev", "이전 콘텐츠/트랙"),
        TvKeyActionOption("stop", "정지", TvKeyCodes.MEDIA_STOP, "stop", "미디어 재생 정지"),
        TvKeyActionOption("sleep", "취침/절전", TvKeyCodes.SLEEP, "sleep", "TV 절전 모드 전환")
    )

    val apps: List<TvAppCatalogItem> = listOf(
        TvAppCatalogItem(
            packageName = "com.google.android.youtube.tv",
            appName = "YouTube",
            launchUri = "https://www.youtube.com",
            iconKey = "youtube",
            accentHex = 0xFFFF3D3D,
            category = "동영상"
        ),
        TvAppCatalogItem(
            packageName = "com.netflix.ninja",
            appName = "Netflix",
            launchUri = "https://www.netflix.com",
            iconKey = "netflix",
            accentHex = 0xFFE50914,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.disney.disneyplus",
            appName = "Disney+",
            launchUri = "https://www.disneyplus.com",
            iconKey = "disney",
            accentHex = 0xFF33A1FD,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "net.cj.cjhv.gs.tving",
            appName = "TVING",
            launchUri = "tving://home",
            iconKey = "tving",
            accentHex = 0xFFFF153C,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "kr.co.captv.pooqV2",
            appName = "Wavve",
            launchUri = "wavve://home",
            iconKey = "wavve",
            accentHex = 0xFF2B67FF,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.coupang.mobile.play",
            appName = "쿠팡플레이",
            launchUri = "coupangplay://home",
            iconKey = "coupang",
            accentHex = 0xFF00A7E1,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.frograms.watcha",
            appName = "Watcha",
            launchUri = "watcha://home",
            iconKey = "watcha",
            accentHex = 0xFFFF0558,
            category = "국내 OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.spotify.tv.android",
            appName = "Spotify",
            launchUri = "spotify://home",
            iconKey = "spotify",
            accentHex = 0xFF1DB954,
            category = "음악"
        ),
        TvAppCatalogItem(
            packageName = "com.amazon.amazonvideo.livingroom",
            appName = "Prime Video",
            launchUri = "https://app.primevideo.com",
            iconKey = "prime",
            accentHex = 0xFF00A8E1,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "com.apple.atve.androidtv.appletv",
            appName = "Apple TV",
            launchUri = "https://tv.apple.com",
            iconKey = "appletv",
            accentHex = 0xFFE5E7EB,
            category = "OTT"
        ),
        TvAppCatalogItem(
            packageName = "tv.twitch.android.app",
            appName = "Twitch",
            launchUri = "twitch://home",
            iconKey = "twitch",
            accentHex = 0xFF9146FF,
            category = "스트리밍"
        ),
        TvAppCatalogItem(
            packageName = "com.plexapp.android",
            appName = "Plex",
            launchUri = "plex://home",
            iconKey = "plex",
            accentHex = 0xFFE5A00D,
            category = "미디어 서버"
        ),
        TvAppCatalogItem(
            packageName = "org.xbmc.kodi",
            appName = "Kodi",
            launchUri = "kodi://home",
            iconKey = "kodi",
            accentHex = 0xFF17B2E7,
            category = "미디어 서버"
        ),
        TvAppCatalogItem(
            packageName = "com.google.android.videos",
            appName = "Google TV",
            launchUri = "https://tv.google.com",
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
        return "market://details?id=$packageName"
    }

    fun iconKeyForPackage(packageName: String): String =
        findByPackage(packageName)?.iconKey ?: "app_generic"

    fun accentForPackage(packageName: String): Long =
        findByPackage(packageName)?.accentHex ?: 0xFF6CD98E
}
