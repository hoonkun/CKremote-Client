package kiwi.hoonkun.ckremote.core.player

import kiwi.hoonkun.ckremote.utils.DefaultJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString


sealed interface RemoteFrame {
    @Serializable
    sealed interface Sending: RemoteFrame

    @Serializable
    sealed interface Receiving: RemoteFrame

    @Transient
    val header: String

    @Serializable
    data class InitRequest(
        val sentence: String,
        val screenRatio: Double
    ): Sending {
        override val header: String = "init"
    }

    @Serializable
    data class InitResponse(
        val screenRatio: Double
    ): Receiving {
        override val header: String = "init"
    }

    @Serializable
    data class RtcOffer(
        val sdp: String
    ): Receiving {
        override val header: String = "r_o"
    }

    @Serializable
    data class RtcAnswer(
        val sdp: String
    ): Sending {
        override val header: String = "r_a"
    }

    @Serializable
    data class RtcCandidate(
        val sdp: String,
        val sdpMLineIndex: Int?,
        val sdpMid: String? = "",
    ): Sending, Receiving {
        override val header: String = "r_c"
    }

}

fun RemoteFrame.Sending.encodeToString(): String =
    "$header ${DefaultJson.encodeToString(this).replace(Regex("\"type\":\"(.+?)\","), "")}"

fun String.decodeToRemoteFrame(): RemoteFrame.Receiving {
    val (header, payload) = split(" ", limit = 2)
    return when (header) {
        "init" -> DefaultJson.decodeFromString<RemoteFrame.InitResponse>(payload)
        "r_o" -> DefaultJson.decodeFromString<RemoteFrame.RtcOffer>(payload)
        "r_c" -> DefaultJson.decodeFromString<RemoteFrame.RtcCandidate>(payload)
        else -> throw RuntimeException("Invalid frame header: $header")
    }
}
