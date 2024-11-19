package kiwi.hoonkun.ckremote.ui.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ckremote.composeapp.generated.resources.Res
import ckremote.composeapp.generated.resources.current_server
import ckremote.composeapp.generated.resources.delete
import ckremote.composeapp.generated.resources.edit
import ckremote.composeapp.generated.resources.icon_back
import ckremote.composeapp.generated.resources.icon_next
import ckremote.composeapp.generated.resources.save
import kiwi.hoonkun.ckremote.core.player.MutableRemoteServer
import kiwi.hoonkun.ckremote.core.player.RemoteServer
import kiwi.hoonkun.ckremote.core.player.toErrors
import kiwi.hoonkun.ckremote.core.player.toMutableRemoteServer
import kiwi.hoonkun.ckremote.ui.standalone.SnackBar
import kiwi.hoonkun.ckremote.ui.standalone.SnackBarAction
import kiwi.hoonkun.ckremote.ui.standalone.ckTextFieldStyle
import kiwi.hoonkun.ckremote.utils.compose.NativeBackPressEffect
import kiwi.hoonkun.ckremote.utils.compose.grayscale
import kiwi.hoonkun.ckremote.utils.compose.horizontalSafeAreaPadding
import kiwi.hoonkun.ckremote.utils.compose.nullIndicationClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun AnimatedContentScope.ServerMutationUi(
    initialStates: RemoteServer,
    identFactory: () -> Int,
    requestSave: (RemoteServer, Boolean) -> Unit,
    requestEdit: (RemoteServer) -> (() -> Unit),
    requestDelete: (Int) -> (() -> Unit),
    requestClose: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    val creation = initialStates.ident == -1

    val target = remember(initialStates) { initialStates.toMutableRemoteServer() }
    val errors = remember(initialStates) { MutableRemoteServer.Errors() }

    val hasChanges = run {
        if (creation) return@run false
        if (initialStates.name != target.name.text) return@run true
        if ("${initialStates.host}:${initialStates.port}" != target.address.text) return@run true
        if (initialStates.sentence != target.sentence.text) return@run true

        false
    }

    val scrollState = remember { ScrollState(0) }

    fun withInvalidHandling(block: (MutableRemoteServer.ValidationResult.Valid) -> Unit) {
        when (val result = target.validate()) {
            is MutableRemoteServer.ValidationResult.Invalid ->
                errors.overwrite(result.toErrors())
            is MutableRemoteServer.ValidationResult.Valid ->
                block(result)
        }
    }

    fun create(connect: Boolean) = withInvalidHandling { result ->
        val instance = result.validated.copy(ident = identFactory())
        requestSave(instance, connect)
        requestClose()

        if (connect) return@withInvalidHandling
        SnackBar.make { ServerSavedSnack() }
    }

    fun edit() = withInvalidHandling { result ->
        val instance = if (creation) result.validated.copy(ident = identFactory()) else result.validated
        val undo = requestEdit(instance)
        requestClose()

        SnackBar.make { ServerEditedSnack(snackId = it, undo = undo) }
    }

    fun delete() {
        val undo = requestDelete(initialStates.ident)
        requestClose()

        SnackBar.make { ServerDeletedSnack(snackId = it, undo = undo) }
    }


    NativeBackPressEffect { requestClose() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .horizontalSafeAreaPadding()
            .imePadding()
            .nullIndicationClickable { keyboard?.hide() }
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .animateEnterExit(
                    enter = slideInHorizontally { -it / 6 },
                    exit = slideOutHorizontally { -it / 6 }
                )
                .width(320.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 24.dp)
                    .height(60.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_back),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = requestClose
                        )
                )
                Text(
                    text = "서버 설정 추가",
                    fontSize = 20.sp,
                )
            }

            MutationFieldLabel(text = "서버 이름", error = errors.name)
            Spacer(modifier = Modifier.height(6.dp))
            MutationField(
                value = target.name,
                onValueChange = {
                    target.name = it
                    errors.name = null
                },
                hasErrors = errors.name != null,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MutationFieldLabel(text = "서버 호스트 및 포트", error = errors.address)
            Spacer(modifier = Modifier.height(6.dp))
            MutationField(
                value = target.address,
                onValueChange = {
                    target.address = it
                    errors.address = null
                },
                hasErrors = errors.address != null,
                imeAction = ImeAction.Next,
                visualTransformation = AddressTransformation
            )

            Spacer(modifier = Modifier.height(16.dp))

            MutationFieldLabel(text = "접속 암호", error = errors.sentence)
            Spacer(modifier = Modifier.height(6.dp))
            MutationField(
                value = target.sentence,
                onValueChange = {
                    errors.sentence = null
                    target.sentence = it
                },
                hasErrors = errors.sentence != null,
                imeAction = ImeAction.Done
            )
        }

        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .animateEnterExit(
                    enter = slideInHorizontally { it / 6 },
                    exit = slideOutHorizontally { it / 6 }
                )
                .fillMaxHeight()
        ) {
            if (creation) {
                MutationAction(
                    text = "저장만 하기",
                    color = Color(0xff166bcc),
                    icon = Res.drawable.save,
                    onClick = { create(connect = false) },
                    modifier = Modifier.offset(y = (-45).dp)
                )
                MutationAction(
                    text = "저장하고 연결하기",
                    offset = 60.dp,
                    color = Color(0xff166bcc),
                    icon = Res.drawable.current_server,
                    withArrow = true,
                    onClick = { create(connect = true) },
                    modifier = Modifier.offset(y = 45.dp)
                )
            } else {
                MutationAction(
                    text = "수정하고 저장",
                    color = Color(0xffb88400).grayscale(if (hasChanges) 0f else 1f),
                    icon = Res.drawable.edit,
                    onClick = ::edit,
                    modifier = Modifier.offset(y = (-45).dp).alpha(if (hasChanges) 1f else 0.5f)
                )
                MutationAction(
                    text = "삭제",
                    offset = 60.dp,
                    color = Color(0xffb83a00),
                    icon = Res.drawable.delete,
                    withArrow = true,
                    onClick = ::delete,
                    modifier = Modifier.offset(y = 45.dp)
                )
            }
        }
    }
}

@Composable
fun DummyServerMutationUi() =
    Box(modifier = Modifier.fillMaxSize())

@Composable
private fun ServerSavedSnack() =
    Text("저장했습니다.")

@Composable
private fun ServerEditedSnack(
    snackId: String,
    undo: () -> Unit
) =
    Row {
        Text("수정 사항을 저장했습니다.")
        SnackBarAction(
            parentIdent = snackId,
            text = "실행 취소",
            onClick = undo
        )
    }

@Composable
private fun ServerDeletedSnack(
    snackId: String,
    undo: () -> Unit
) =
    Row {
        Text("서버 설정을 삭제했습니다.")
        SnackBarAction(
            parentIdent = snackId,
            text = "실행 취소",
            onClick = undo
        )
    }

@Composable
private fun MutationAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
    icon: DrawableResource,
    offset: Dp = 0.dp,
    withArrow: Boolean = false,
    modifier: Modifier = Modifier,
) =
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .height(80.dp)
            .offset(x = offset)
            .offset(x = 200.dp)
            .then(modifier)
            .nullIndicationClickable(onClick = onClick)
    ) {
        Spacer(
            modifier = Modifier
                .size(width = 520.dp, height = 25.dp)
                .align(Alignment.BottomStart)
                .offset(y = (-15).dp)
                .background(color = color)
        )
        Text(
            text = text,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(start = 28.dp)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-225).dp)
                .alpha(0.4f)
        ) {
            if (withArrow) {
                Image(
                    painter = painterResource(Res.drawable.icon_next),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .offset(x = 40.dp)
                        .size(width = 80.dp, height = 80.dp)
                        .padding(24.dp)
                )
            }
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .size(width = 80.dp, height = 80.dp)
            )
        }
    }

@Composable
private fun MutationFieldLabel(text: String, error: String?) =
    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.7f)
        )
        AnimatedContent(
            targetState = error,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {
            if (it != null) {
                Text(
                    text = it,
                    color = Color(0xffa84832),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }

@Composable
private fun MutationField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    imeAction: ImeAction,
    hasErrors: Boolean,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        interactionSource = interaction,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth().ckTextFieldStyle(interaction, hasErrors).then(modifier)
    )
}

private object AddressTransformation: VisualTransformation {

    private object AddressTransformationOffsetMapping: OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            offset + 7

        override fun transformedToOriginal(offset: Int): Int =
            (offset - 7).coerceAtLeast(0)
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val colonIndex = text.indexOf(":")

        val hostSegment =
            text.subSequence(0, colonIndex.takeIf { it > 0 } ?: text.length)

        val portSegment =
            if (colonIndex > 0) text.subSequence(colonIndex + 1, text.length)
            else null

        return TransformedText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White.copy(alpha = 0.4f))) {
                    append("http://")
                }
                withStyle(SpanStyle(color = Color.White)) {
                    append(hostSegment)
                }
                if (colonIndex > 0) {
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.4f))) {
                        append(":")
                    }
                }
                if (portSegment != null) {
                    val isPortValid = portSegment.text.toIntOrNull() != null
                    val portColor = if (isPortValid) 0xffffffff else 0xfff75464
                    withStyle(SpanStyle(color = Color(portColor))) {
                        append(portSegment)
                    }
                }
            },
            offsetMapping = AddressTransformationOffsetMapping
        )
    }

}
