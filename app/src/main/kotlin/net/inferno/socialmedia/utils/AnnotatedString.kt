package net.inferno.socialmedia.utils

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.text.getSpans
import kotlin.math.min

private val tags = linkedMapOf(
    "<p>" to "</p>",
    "<h1>" to "</h1>",
    "<h2>" to "</h2>",
    "<h3>" to "</h3>",
    "<h4>" to "</h4>",
    "<h5>" to "</h5>",
    "<h6>" to "</h6>",
    "<strong>" to "</strong>",
    "<em>" to "</em>",
    "<u>" to "</u>",
    "<del>" to "</del>",
)

/**
 * The main entry point. Call this on a String and use the result in a Text.
 */
fun String.parseAsHtml(): AnnotatedString {
    val newlineReplace = this.replace("<br>", "\n")

    return buildAnnotatedString {
        recurse(newlineReplace)
    }
}

/**
 * Recurses through the given HTML String to convert it to an AnnotatedString.
 *
 * @param string the String to examine.
 */
private fun  AnnotatedString.Builder.recurse(string: String) {
    //Find the opening tag that the given String starts with, if any.
    val startTag = tags.keys.find { string.startsWith(it) }

    //Find the closing tag that the given String starts with, if any.
    val endTag = tags.values.find { string.startsWith(it) }

    when {
        //If the String starts with a closing tag, then pop the latest-applied
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.value) } -> {
            pop()
            recurse(string.removeRange(0, endTag!!.length))
        }
        //If the String starts with an opening tag, apply the appropriate
        //SpanStyle and continue recursing.
        tags.any { string.startsWith(it.key) } -> {
            pushStyle(tagToStyle(startTag!!))
            recurse(string.removeRange(0, startTag.length))
        }
        //If the String doesn't start with an opening or closing tag, but does contain either,
        //find the lowest index (that isn't -1/not found) for either an opening or closing tag.
        //Append the text normally up until that lowest index, and then recurse starting from that index.
        tags.any { string.contains(it.key) || string.contains(it.value) } -> {
            val firstStart =
                tags.keys.map { string.indexOf(it) }.filterNot { it == -1 }.minOrNull() ?: -1
            val firstEnd =
                tags.values.map { string.indexOf(it) }.filterNot { it == -1 }.minOrNull() ?: -1
            val first = when {
                firstStart == -1 -> firstEnd
                firstEnd == -1 -> firstStart
                else -> min(firstStart, firstEnd)
            }

            append(string.substring(0, first))

            recurse(string.removeRange(0, first))
        }
        //There weren't any supported tags found in the text. Just append it all normally.
        else -> {
            append(string)
        }
    }
}

/**
 * Get a [SpanStyle] for a given (opening) tag.
 * Add your own tag styling here by adding its opening tag to
 * the when clause and then instantiating the appropriate [SpanStyle].
 *
 * @return a [SpanStyle] for the given tag.
 */
private fun tagToStyle(tag: String) = when (tag) {
    "<p>" -> SpanStyle()
    "<strong>" -> SpanStyle(fontWeight = FontWeight.Bold)
    "<em>" -> SpanStyle(fontStyle = FontStyle.Italic)
    "<u>" -> SpanStyle(textDecoration = TextDecoration.Underline)
    "<del>" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    else -> {
        if (tag.startsWith("<h")) {
            val level = tag.removePrefix("<h").removeSuffix(">").toInt()

            SpanStyle(
                fontWeight = FontWeight.Bold,
                fontSize = (25 - level).sp,
            )
        } else SpanStyle()
    }
}

fun Spanned.parseAnnotated(): AnnotatedString {
    val spanned = this

    return buildAnnotatedString {
        append(spanned.toString())

        getSpans<CharacterStyle>().forEach {
            val start = spanned.getSpanStart(it)
            val end = spanned.getSpanEnd(it)

            when (it) {
                is StyleSpan -> {
                    when (it.style) {
                        Typeface.BOLD -> addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold),
                            start,
                            end,
                        )

                        Typeface.ITALIC -> addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            start,
                            end,
                        )

                        Typeface.BOLD_ITALIC -> addStyle(
                            SpanStyle(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            ),
                            start,
                            end,
                        )
                    }
                }

                is UnderlineSpan -> {
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start,
                        end,
                    )
                }

                is StrikethroughSpan -> {
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        start,
                        end,
                    )
                }
            }
        }
    }
}