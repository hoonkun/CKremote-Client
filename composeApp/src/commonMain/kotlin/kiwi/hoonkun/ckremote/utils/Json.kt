package kiwi.hoonkun.ckremote.utils

import kotlinx.serialization.json.Json


val DefaultJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
}