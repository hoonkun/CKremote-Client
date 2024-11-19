package kiwi.hoonkun.ckremote.core.networking

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin


actual val DefaultHttpClientEngine: HttpClientEngineFactory<*> = Darwin