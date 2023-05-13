package net.inferno.socialmedia.view

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.inferno.socialmedia.model.MIXED_MD
import net.inferno.socialmedia.theme.SocialMediaTheme
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.ins.Ins
import org.commonmark.ext.ins.InsExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

private const val TAG_IMAGE_URL = "imageUrl"

@Composable
fun MDDocument(
    text: String,
    modifier: Modifier = Modifier,
    clip: Boolean = false,
) {
    var height by remember { mutableStateOf(0) }
    val parser = Parser.builder()
        .extensions(
            listOf(
                StrikethroughExtension.create(),
                InsExtension.create(),
            )
        )
        .build()

    val document = parser.parse(text) as Document

    val density = LocalDensity.current

    Box {
        MDBlockChildren(
            document,
            modifier
                .onPlaced {
                    height = it.size.height
                }
                .then(
                    if (clip) Modifier.heightIn(max = 150.dp)
                    else Modifier
                )
        )

        if (clip && height > 100 * density.density) {
            Box(
                Modifier
                    .height(48.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f),
                            )
                        )
                    )
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun MDDocument(
    document: Document,
    modifier: Modifier = Modifier,
) {
    MDBlockChildren(document, modifier)
}

@Composable
private fun MDBlockChildren(
    parent: Node,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var child = parent.firstChild

        while (child != null) {
            when (child) {
                is BlockQuote -> MDBlockQuote(child)
                is ThematicBreak -> MDThematicBreak(child)
                is Heading -> MDHeading(child)
                is Paragraph -> MDParagraph(child)
                is FencedCodeBlock -> MDFencedCodeBlock(child)
                is IndentedCodeBlock -> MDIndentedCodeBlock(child)
                is BulletList -> MDBulletList(child)
                is OrderedList -> MDOrderedList(child)
            }

            child = child.next
        }
    }
}

@Composable
private fun MDParagraph(
    paragraph: Paragraph,
    modifier: Modifier = Modifier,
) {
    val padding = if (paragraph.parent is Document) 8.dp else 0.dp

    Box(
        modifier = modifier
            .padding(
                bottom = padding
            )
    ) {
        val styledText = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.bodyLarge.toSpanStyle())
            appendMarkdownChildren(paragraph, MaterialTheme.colorScheme)
            pop()
        }

        MarkdownText(styledText, MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun MDIndentedCodeBlock(
    indentedCodeBlock: IndentedCodeBlock,
    modifier: Modifier = Modifier,
) {
    // Ignored
}

@Composable
private fun MDThematicBreak(
    thematicBreak: ThematicBreak,
    modifier: Modifier = Modifier,
) {
    //Ignored
}

@Composable
private fun MDBulletList(
    bulletList: BulletList,
    modifier: Modifier = Modifier,
) {
    val marker = bulletList.bulletMarker

    MDListItems(bulletList, modifier = modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.bodyLarge.toSpanStyle())
            append("$marker ")
            appendMarkdownChildren(it, MaterialTheme.colorScheme)
            pop()
        }

        MarkdownText(text, MaterialTheme.typography.bodyLarge, modifier)
    }
}

@Composable
private fun MDOrderedList(
    orderedList: OrderedList,
    modifier: Modifier = Modifier,
) {
    var number = orderedList.startNumber
    val delimiter = orderedList.delimiter

    MDListItems(orderedList, modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.bodyLarge.toSpanStyle())
            append("${number++}$delimiter ")
            appendMarkdownChildren(it, MaterialTheme.colorScheme)
            pop()
        }

        MarkdownText(text, MaterialTheme.typography.bodyLarge, modifier)
    }
}

@Composable
private fun MDListItems(
    listBlock: ListBlock,
    modifier: Modifier = Modifier,
    item: @Composable (node: Node) -> Unit
) {
    val bottom = if (listBlock.parent is Document) 8.dp else 0.dp
    val start = if (listBlock.parent is Document) 0.dp else 8.dp

    Column(
        modifier = modifier
            .padding(
                start = start,
                bottom = bottom,
            )
    ) {
        var listItem = listBlock.firstChild
        while (listItem != null) {
            var child = listItem.firstChild
            while (child != null) {
                when (child) {
                    is BulletList -> MDBulletList(child, modifier)
                    is OrderedList -> MDOrderedList(child, modifier)
                    else -> item(child)
                }
                child = child.next
            }
            listItem = listItem.next
        }
    }
}

@Composable
private fun MDBlockQuote(
    blockQuote: BlockQuote,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .drawBehind {
                drawLine(
                    color = color,
                    strokeWidth = 2f,
                    start = Offset(12.dp.value, 0f),
                    end = Offset(12.dp.value, size.height)
                )
            }
            .padding(
                start = 16.dp,
                top = 4.dp,
                bottom = 4.dp,
            )
    ) {
        val text = buildAnnotatedString {
            pushStyle(
                MaterialTheme.typography.bodyLarge.toSpanStyle()
                    .plus(SpanStyle(fontStyle = FontStyle.Italic))
            )
            appendMarkdownChildren(blockQuote, MaterialTheme.colorScheme)
            pop()
        }
        Text(text, modifier)
    }
}

@Composable
private fun MDFencedCodeBlock(
    fencedCodeBlock: FencedCodeBlock,
    modifier: Modifier = Modifier,
) {
    val padding = if (fencedCodeBlock.parent is Document) 8.dp else 0.dp

    Box(
        modifier = modifier.padding(
            start = 8.dp,
            bottom = padding,
        )
    ) {
        Text(
            text = fencedCodeBlock.literal,
            style = TextStyle(fontFamily = FontFamily.Monospace),
            modifier = modifier,
        )
    }
}

@Composable
private fun MDHeading(heading: Heading) {
    val style = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = (25 - heading.level).sp,
    )

    val padding = if (heading.parent is Document) 8.dp else 0.dp

    Box(
        modifier = Modifier
            .padding(
                bottom = padding
            ),
    ) {
        val text = buildAnnotatedString {
            appendMarkdownChildren(heading, MaterialTheme.colorScheme)
        }
        MarkdownText(text, style)
    }
}

@OptIn(ExperimentalTextApi::class)
private fun AnnotatedString.Builder.appendMarkdownChildren(
    parent: Node,
    colors: ColorScheme,
) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> appendMarkdownChildren(child, colors)
            is Text -> append(child.literal)

            is Ins -> {
                pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                appendMarkdownChildren(child, colors)
                pop()
            }


            is Strikethrough -> {
                pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                appendMarkdownChildren(child, colors)
                pop()
            }

            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendMarkdownChildren(child, colors)
                pop()
            }

            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendMarkdownChildren(child, colors)
                pop()
            }

            is Code -> {
                pushStyle(
                    TextStyle(
                        fontFamily = FontFamily.Monospace
                    ).toSpanStyle()
                )
                append(child.literal)
                pop()
            }

            is HardLineBreak -> {
                append("\n")
            }

            is Link -> {
                val underline = SpanStyle(
                    colors.primary,
                    textDecoration = TextDecoration.Underline,
                )
                pushStyle(underline)
                pushUrlAnnotation(UrlAnnotation(child.destination))

                appendMarkdownChildren(child, colors)

                pop()
                pop()
            }
        }
        child = child.next
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun MarkdownText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    var layoutResult by remember {
        mutableStateOf<TextLayoutResult?>(null)
    }

    Text(
        text = text,
//        modifier = modifier
//            .pointerInput(Unit) {
//                detectTapGestures { pos ->
//                    layoutResult?.let {
//                        val position = it.getOffsetForPosition(pos)
//
//                        text.getUrlAnnotations(position, position).firstOrNull()?.let { sa ->
//                            uriHandler.openUri(sa.item.url)
//                        }
//                    }
//                }
//            },
        style = style,
        onTextLayout = {
            layoutResult = it
        }
    )
}

@Preview(
    showBackground = true
)
@Composable
fun DefaultPreview() {
    SocialMediaTheme {
        Surface {
            MDDocument(
                MIXED_MD,
                clip = true,
                modifier = Modifier
                    .fillMaxWidth()
//                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }
}