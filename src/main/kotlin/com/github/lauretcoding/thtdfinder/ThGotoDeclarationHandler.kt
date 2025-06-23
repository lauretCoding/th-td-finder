package com.github.lauretcoding.thtdfinder

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Alarm
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.pom.Navigatable
import com.intellij.ui.awt.RelativePoint
import java.awt.Point

class ThGotoDeclarationHandler : GotoDeclarationHandler {
    companion object {
        private val hoverPopupControllerMap = mutableMapOf<Editor, HoverPopupController>()
    }

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        if (sourceElement == null) return null

        val thTag = findParentTag(sourceElement, "th") ?: return null
        val correspondingTds = findCorrespondingTds(thTag)

        if (correspondingTds.size == 1) {
            return arrayOf(correspondingTds[0])
        } else if (correspondingTds.isNotEmpty()) {
            val controller = hoverPopupControllerMap.getOrPut(editor) {
                HoverPopupController(editor)
            }

            controller.schedulePopup(thTag, correspondingTds) { element, tds ->
                showNavigationPopupUnderElement(editor, element, tds)
            }
            return null
        }

        return null
    }

    fun cancelPopup(editor: Editor) {
        hoverPopupControllerMap[editor]?.cancelPopup()
    }

    private fun showNavigationPopupUnderElement(editor: Editor, element: XmlTag, elements: List<XmlTag>): JBPopup {
        val popupStep = object : BaseListPopupStep<XmlTag>("Go to a matching td : ", elements) {
            override fun getTextFor(value: XmlTag): String {
                val preview = value.value.text.trim().take(30).replace("\n", " ")
                return "• $preview"
            }

            override fun onChosen(selectedValue: XmlTag?, finalChoice: Boolean): PopupStep<*>? {
                if (selectedValue != null) {
                    if (selectedValue is Navigatable) {
                        selectedValue.navigate(true)
                    }
                    val offset = selectedValue.textOffset
                    editor.scrollingModel.scrollTo(editor.offsetToLogicalPosition(offset), ScrollType.CENTER)
                }
                return FINAL_CHOICE
            }
        }

        val popup = JBPopupFactory.getInstance().createListPopup(popupStep)

        val startOffset = element.textOffset
        val point: Point = editor.visualPositionToXY(editor.offsetToVisualPosition(startOffset))

        val popupLocation = RelativePoint(
            editor.contentComponent,
            Point(point.x, point.y + editor.lineHeight)
        )

        popup.show(popupLocation)
        return popup
    }

    fun findParentTag(element: PsiElement, tagName: String): XmlTag? {
        var parent = element.parent
        while (parent != null) {
            if (parent is XmlTag && parent.name == tagName) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun findCorrespondingTds(thTag: XmlTag): List<XmlTag> {
        val correspondingTds = mutableListOf<XmlTag>()

        val tableTag = findParentTag(thTag, "table") ?: return correspondingTds
        val thTrTag = findParentTag(thTag, "tr") ?: return correspondingTds
        val thTags = thTrTag.findSubTags("th")
        val thIndex = thTags.indexOf(thTag)
        if (thIndex == -1) return correspondingTds

        val tbodyTags = tableTag.findSubTags("tbody")
        if (tbodyTags.isEmpty()) return correspondingTds

        val tbodyTag = tbodyTags.first()
        val dataRows = tbodyTag.findSubTags("tr")

        for (row in dataRows) {
            val tdTags = row.findSubTags("td")
            if (thIndex < tdTags.size) {
                correspondingTds.add(tdTags[thIndex])
            }
        }
        return correspondingTds
    }

    private class HoverPopupController(private val editor: Editor) {
        private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, editor.project ?: throw IllegalStateException("No project"))
        private var currentPopupElement: XmlTag? = null
        private var popup: JBPopup? = null

        fun schedulePopup(thElement: XmlTag, elements: List<XmlTag>, showPopupFn: (XmlTag, List<XmlTag>) -> JBPopup) {
            if (thElement == currentPopupElement) return

            currentPopupElement = thElement
            alarm.cancelAllRequests()

            alarm.addRequest({
                ApplicationManager.getApplication().invokeLater {
                    if (!editor.isDisposed) {
                        popup = showPopupFn(thElement, elements)
                    }
                }
            }, 250)
        }

        fun cancelPopup() {
            currentPopupElement = null
            alarm.cancelAllRequests()
            popup?.cancel()
            popup = null
        }
    }
}
