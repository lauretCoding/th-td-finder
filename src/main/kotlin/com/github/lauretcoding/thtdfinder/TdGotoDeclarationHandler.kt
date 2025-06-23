package com.github.lauretcoding.thtdfinder

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag

class TdGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        if (sourceElement == null) return null

        val tdTag = findParentTag(sourceElement, "td")
        if (tdTag == null) return null

        val correspondingTh = findCorrespondingTh(tdTag)

        if (correspondingTh != null) {
            return arrayOf(correspondingTh)
        }

        return null
    }

    private fun findParentTag(element: PsiElement, tagName: String): XmlTag? {
        var parent = element.parent
        while (parent != null) {
            if (parent is XmlTag && parent.name == tagName) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun findCorrespondingTh(tdTag: XmlTag): PsiElement? {
        val tableTag = findParentTag(tdTag, "table") ?: return null
        val theadTag = tableTag.findSubTags("thead").firstOrNull() ?: return null
        val headerRow = theadTag.findSubTags("tr").firstOrNull() ?: return null
        val thTags = headerRow.findSubTags("th")
        if (thTags.isEmpty()) return null
        val trTag = findParentTag(tdTag, "tr") ?: return null
        val tdList = trTag.findSubTags("td")
        val tdIndex = tdList.indexOf(tdTag)
        if (tdIndex == -1 || tdIndex >= thTags.size) return null

        return thTags[tdIndex]
    }
}
