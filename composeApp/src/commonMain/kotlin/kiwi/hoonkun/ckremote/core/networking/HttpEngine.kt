package kiwi.hoonkun.ckremote.core.networking

import io.ktor.client.engine.HttpClientEngineFactory


expect val DefaultHttpClientEngine: HttpClientEngineFactory<*>