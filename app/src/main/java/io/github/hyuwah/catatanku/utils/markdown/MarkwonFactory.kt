package io.github.hyuwah.catatanku.utils.markdown

import android.content.Context
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j

object MarkwonFactory {
    fun get(context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugins(
                listOf(
                    SyntaxHighlightPlugin.create(
                        Prism4j(AppGrammarLocator()),
                        Prism4jThemeDefault.create()
                    ),
                    LinkifyPlugin.create(),
                    TablePlugin.create(context),
                    MovementMethodPlugin.create(TableAwareMovementMethod.create()),
                    MovementMethodPlugin.create(LinkArrowKeyMovementMethod()),
                    StrikethroughPlugin.create(),
                    TaskListPlugin.create(context)
                )
            )
            .build()
    }
}