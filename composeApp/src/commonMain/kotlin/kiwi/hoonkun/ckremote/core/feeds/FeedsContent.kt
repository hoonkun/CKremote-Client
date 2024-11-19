package kiwi.hoonkun.ckremote.core.feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kiwi.hoonkun.ckremote.core.feeds.contents.Anchor
import kiwi.hoonkun.ckremote.core.feeds.contents.BoldText
import kiwi.hoonkun.ckremote.core.feeds.contents.FeedNode
import kiwi.hoonkun.ckremote.core.feeds.contents.FeedParagraph
import kiwi.hoonkun.ckremote.core.feeds.contents.Heading1
import kiwi.hoonkun.ckremote.core.feeds.contents.Heading2
import kiwi.hoonkun.ckremote.core.feeds.contents.Image
import kiwi.hoonkun.ckremote.core.feeds.contents.ItalicText
import kiwi.hoonkun.ckremote.core.feeds.contents.PlainText
import kiwi.hoonkun.ckremote.core.feeds.contents.UnderlinedText
import kiwi.hoonkun.ckremote.core.feeds.contents.UnorderedList
import kiwi.hoonkun.ckremote.core.feeds.contents.YoutubeFrame

@Composable
fun FeedContent(paragraphs: List<FeedParagraph>) {
    paragraphs.forEach { paragraph ->
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = 24.sp)
        ) {
            when (paragraph) {
                is FeedParagraph.Text -> FeedTextParagraph(paragraph)
                is FeedParagraph.List -> FeedListParagraph(paragraph)
                is FeedParagraph.Media -> FeedMediaParagraph(paragraph)
            }
        }
    }
}

@Composable
fun FeedTextParagraph(paragraph: FeedParagraph.Text) {
    Text(
        text = buildAnnotatedString {
            paragraph.texts.forEachIndexed { index, node ->
                appendNode(node, index == paragraph.texts.lastIndex)
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun FeedListParagraph(paragraph: FeedParagraph.List) {
    when (paragraph.list) {
        is UnorderedList -> {
            paragraph.list.items.forEach { li ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    Text("\u2022", modifier = Modifier.padding(start = 4.dp, end = 4.dp))
                    Text(text = buildAnnotatedString { li.texts.forEach { appendNode(it) } })
                }
            }
        }
    }
}

@Composable
fun FeedMediaParagraph(paragraph: FeedParagraph.Media) {
    when(paragraph.node) {
        is Image -> {
            FeedsImage(paragraph.node)
        }
        is YoutubeFrame -> {
            FeedsYoutubeFrame(paragraph.node)
        }
    }
}

@Composable
fun FeedsImage(node: Image) {
    val image by produceState<ImageBitmap?>(null) {
        value = Feeds.loadImage(node.link)
    }

    val capturedImage = image
    if (capturedImage != null) {
        Image(
            bitmap = capturedImage,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text(
            text = "이미지 로드 중",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).alpha(0.5f)
        )
    }
}

@Composable
expect fun FeedsYoutubeFrame(
    node: YoutubeFrame,
    modifier: Modifier = Modifier.fillMaxWidth().aspectRatio(560f / 315f)
)

fun buildYoutubeFrameSource(id: String) = """
    <html>
    <head>
        <style>
            html, body {
                margin: 0;
                padding: 0;
                overflow: hidden;
            }
        </style>
    </head>
    <body>
        <iframe 
            style="width: 100%; height: 100%;"
            src="https://www.youtube.com/embed/$id" 
            title="YouTube video player" 
            frameborder="0" 
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
            referrerpolicy="strict-origin-when-cross-origin" 
            allowfullscreen
        >    
        </iframe>
    </body>
    </html>
""".trimIndent()

private fun AnnotatedString.Builder.appendNode(node: FeedNode.Text, isLastNode: Boolean = false) {
    when(node) {
        is FeedNode.Text.Single -> {
            var text = node.text
            if (isLastNode && text.endsWith("\n\n")) {
                text = text.removeSuffix("\n\n").plus("\n")
            }

            when(node) {
                is PlainText -> append(text)
                is Anchor -> {
                    withStyle(SpanStyle(color = Color(0xff6ea5ff))) {
                        withLink(LinkAnnotation.Url(node.link)) {
                            append(text)
                        }
                    }
                }
            }
        }
        is FeedNode.Text.Grouped -> {
            withStyle(node.style) {
                node.texts.forEach { appendNode(it, isLastNode) }
            }
        }
    }
}

private val FeedNode.Text.Grouped.style: SpanStyle get() =
    when(this) {
        is Heading1 -> SpanStyle(fontSize = 28.sp)
        is Heading2 -> SpanStyle(fontSize = 24.sp)
        is BoldText -> SpanStyle(fontWeight = FontWeight.Bold)
        is ItalicText -> SpanStyle(fontStyle = FontStyle.Italic)
        is UnderlinedText -> SpanStyle(textDecoration = TextDecoration.Underline)
    }
