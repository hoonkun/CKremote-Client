package kiwi.hoonkun.ckremote.core.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import kiwi.hoonkun.ckremote.utils.DefaultJson
import kiwi.hoonkun.ckremote.utils.UserSpaceStore
import kiwi.hoonkun.ckremote.utils.collectAsState
import kiwi.hoonkun.ckremote.utils.rememberUserSpaceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString


@Serializable
@Immutable
data class RemoteServer(
    val ident: Int,
    val name: String,
    val host: String,
    val port: Int,
    val sentence: String
) {
    companion object {
        fun Empty() = RemoteServer(-1, "", "", 0, "")
    }
}

@Stable
class MutableRemoteServer(
    private val ident: Int = -1,
    name: String = "",
    address: String = "",
    sentence: String = ""
) {
    var name by mutableStateOf(TextFieldValue(name))
    var address by mutableStateOf(TextFieldValue(address))
    var sentence by mutableStateOf(TextFieldValue(sentence))

    @Stable
    class Errors(
        name: String? = null,
        address: String? = null,
        sentence: String? = null
    ) {
        var name by mutableStateOf(name)
        var address by mutableStateOf(address)
        var sentence by mutableStateOf(sentence)

        fun overwrite(other: Errors) {
            name = other.name
            address = other.address
            sentence = other.sentence
        }
    }

    fun validate(): ValidationResult {
        var invalid: ValidationResult.Invalid? = null

        val addError: (ValidationResult.Invalid.() -> ValidationResult.Invalid) -> Unit = { applier ->
            invalid = (invalid ?: ValidationResult.Invalid()).applier()
        }

        if (name.text.isEmpty()) {
            addError { copy(name = "이름은 비워둘 수 없어요!") }
        }

        if (address.text.isEmpty()) {
            addError { copy(address = "호스트는 비워둘 수 없어요!") }
        } else {
            val port = address.text.split(":").getOrNull(1)
            if (port == null) {
                addError { copy(address = "포트를 반드시 지정해야해요.") }
            } else if (port.toIntOrNull() == null) {
                addError { copy(address = "포트에 잘못된 값이 입력되었어요.") }
            }
        }

        if (sentence.text.isEmpty()) {
            addError { copy(sentence = "접속 암호는 비워둘 수 없어요!") }
        }

        invalid?.let { return it }

        val name = name.text
        val (host, port) = address.text.split(":").let { it[0] to it[1].toInt() }
        val sentence = sentence.text

        return ValidationResult.Valid(RemoteServer(ident, name, host, port, sentence))
    }

    sealed interface ValidationResult {
        @Immutable
        data class Invalid(
            val name: String? = null,
            val address: String? = null,
            val sentence: String? = null
        ) : ValidationResult

        @Immutable
        data class Valid(val validated: RemoteServer) : ValidationResult
    }
}

@Stable
fun RemoteServer.toMutableRemoteServer() =
    MutableRemoteServer(
        ident = ident,
        name = name,
        address = if (ident == -1) "" else "$host:$port",
        sentence = sentence
    )

@Stable
fun MutableRemoteServer.ValidationResult.Invalid.toErrors() =
    MutableRemoteServer.Errors(
        name, address, sentence
    )

@Composable
fun rememberRemoteServers(): Pair<SnapshotStateList<RemoteServer>, (() -> Unit) -> Unit> {
    val store = rememberUserSpaceStore()

    val servers = remember(store) {
        mutableStateListOf(
            *DefaultJson.decodeFromString<List<RemoteServer>>(store["servers"] ?: "[]")
                .toTypedArray()
        )
    }

    fun mutate(mutation: () -> Unit) {
        mutation()
        store["servers"] = DefaultJson.encodeToString(servers.toList())
        RemoteServerMutation.flow.value = servers.toList()
    }

    return servers to ::mutate
}

object RemoteServerMutation {

    val flow = MutableStateFlow<List<RemoteServer>?>(null)

    fun loadFromStore(store: UserSpaceStore) =
        DefaultJson.decodeFromString<List<RemoteServer>>(store["servers"] ?: "[]")

    @Composable
    fun collectAsState() =
        flow.collectAsState()

}
