package kiwi.hoonkun.ckremote.core.feeds.contents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kiwi.hoonkun.ckremote.core.feeds.Feed


@Composable
fun rememberFeedContentParagraphs(feed: Feed) = remember(feed.contents) { feed.parseParagraphs() }

private fun Feed.parseParagraphs(): List<FeedParagraph> {
    var nodes = listOf<FeedNode>(PlainText(contents))
    nodes = parseListTag(nodes)
    nodes = parseNormalTags(nodes)

    val paragraphs = mutableListOf<FeedParagraph>()

    for (node in nodes) {
        when(node) {
            is FeedNode.Text -> {
                var lastElement = paragraphs.lastOrNull()
                if (lastElement !is FeedParagraph.Text) {
                    lastElement = FeedParagraph.Text()
                    paragraphs.add(lastElement)
                }
                val newTexts = lastElement.texts
                    .toMutableList()
                    .apply { add(node) }
                paragraphs[paragraphs.lastIndex] =
                    lastElement.copy(texts = newTexts)
            }
            is FeedNode.Media ->
                paragraphs.add(FeedParagraph.Media(node))
            is FeedNode.List ->
                paragraphs.add(FeedParagraph.List(node))
        }
    }

    return paragraphs
}

private fun parseListTag(input: List<FeedNode>): List<FeedNode> {
    var nodes = input

    // special handling for list, which contains newline character in element;
    // in common code, match result with ((?:.|\n).+?) pattern in regex causes SIGSEGV
    while(nodes.filterIsInstance<PlainText>().any { it.text.contains("[list]") && it.text.contains("[/list]") }) {
        val nodesBuffer = nodes.toMutableList()
        for (node in nodes) {
            if (node !is PlainText) continue

            val openingList = node.text.indexOf("[list]")
            val closingList = node.text.indexOf("[/list]")

            if (openingList < 0 || closingList < 0) continue

            val prev = PlainText(node.text.slice(0..<openingList))
            val list = UnorderedList(
                items = node.text.slice((openingList + 6)..<closingList)
                    .split("[*]")
                    .filter { it.isNotBlank() }
                    .map { UnorderedList.Item(parseNormalTags(listOf(PlainText(it.trim()))).filterIsInstance<FeedNode.Text.Single>()) }
            )
            val next = PlainText(node.text.slice((closingList + 7)..<node.text.length))

            val index = nodesBuffer.indexOf(node)
            nodesBuffer.removeAt(index)
            nodesBuffer.addAll(index, listOf(prev, list, next))
        }
        nodes = nodesBuffer
    }

    return nodes
}

private fun parseNormalTags(input: List<FeedNode>): List<FeedNode> {
    var nodes = input

    for (tagName in FeedNode.tagNames) {
        val nodesBuffer = nodes.toMutableList()
        val regex = Regex("\\[$tagName(?:=(.+))?](.+?)?\\[/$tagName]")

        for (node in nodes) {
            if (node !is PlainText) continue

            val matches = regex.findAll(node.text).toList().map {
                FeedNode.fromMatchResult(
                    tagName,
                    it
                )
            }
            val segments = node.text.split(regex).map { PlainText(it) }

            if (matches.isEmpty()) continue

            val index = nodesBuffer.indexOf(node)
            val destructed = mutableListOf<FeedNode>()

            segments.forEachIndexed { segmentIndex, segment ->
                destructed.add(segment)
                if (segmentIndex < matches.size) destructed.add(matches[segmentIndex])
            }

            nodesBuffer.removeAt(index)
            nodesBuffer.addAll(index, destructed)
        }
        nodes = nodesBuffer
    }

    return nodes
}

private val MatchResult.children get() = groupValues.getOrNull(2) ?: ""
private val MatchResult.props get() = groupValues.getOrNull(1) ?: ""
private val MatchResult.singleTextNodes get() = parseNormalTags(listOf(PlainText(children))).filterIsInstance<FeedNode.Text.Single>()

sealed interface FeedParagraph {
    data class Media(
        val node: FeedNode.Media
    ): FeedParagraph

    data class Text(
        val texts: kotlin.collections.List<FeedNode.Text> = mutableListOf()
    ): FeedParagraph

    data class List(
        val list: FeedNode.List
    ): FeedParagraph
}

sealed interface FeedNode {

    companion object {
        val tagNames = listOf("h1", "h2", "b", "i", "u", "url", "img", "previewyoutube")

        fun fromMatchResult(tagName: String, matchResult: MatchResult): FeedNode {
            return when(tagName) {
                "url" -> Anchor(link = matchResult.props, text = matchResult.children)
                "img" -> Image(link = matchResult.children)
                "previewyoutube" ->
                    YoutubeFrame(
                        id = matchResult.props.takeWhile { it != ';' },
                        full = matchResult.props.contains(";full")
                    )
                "h1" -> Heading1(texts = matchResult.singleTextNodes)
                "h2" -> Heading2(texts = matchResult.singleTextNodes)
                "b" -> BoldText(texts = matchResult.singleTextNodes)
                "i" -> ItalicText(texts = matchResult.singleTextNodes)
                "u" -> UnderlinedText(texts = matchResult.singleTextNodes)
                else -> throw RuntimeException("Unknown tag name for FeedContentNode.fromMatchResult")
            }
        }
    }

    sealed interface Text: FeedNode {
        sealed interface Single: Text {
            val text: String
        }

        sealed interface Grouped: Text {
            val texts: kotlin.collections.List<Single>
        }
    }

    sealed interface Media: FeedNode

    sealed interface List: FeedNode

}

data class PlainText(
    override val text: String
): FeedNode.Text.Single

data class Anchor(
    val link: String,
    override val text: String
): FeedNode.Text.Single

data class Image(
    val link: String
): FeedNode.Media

data class YoutubeFrame(
    val id: String,
    val full: Boolean
): FeedNode.Media

data class UnorderedList(
    val items: List<Item>
): FeedNode.List {
    data class Item(
        val texts: List<FeedNode.Text.Single>
    )
}

data class Heading1(
    override val texts: List<FeedNode.Text.Single>
): FeedNode.Text.Grouped

data class Heading2(
    override val texts: List<FeedNode.Text.Single>
): FeedNode.Text.Grouped

data class BoldText(
    override val texts: List<FeedNode.Text.Single>
): FeedNode.Text.Grouped

data class ItalicText(
    override val texts: List<FeedNode.Text.Single>
): FeedNode.Text.Grouped

data class UnderlinedText(
    override val texts: List<FeedNode.Text.Single>
): FeedNode.Text.Grouped
