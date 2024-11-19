package kiwi.hoonkun.ckremote.core.networking

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp


actual val DefaultHttpClientEngine: HttpClientEngineFactory<*> get() = OkHttp