package com.github.lauretcoding.thtdfinder

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDocumentManager
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class CtrlHoverListener(
    private val editor: Editor,
    private val handler: ThGotoDeclarationHandler
) : KeyListener, EditorMouseListener, EditorMouseMotionListener {

    private var ctrlPressed = false
    private var lastThElement: PsiElement? = null

    override fun mouseMoved(e: EditorMouseEvent) {

        if (!ctrlPressed) {
            if (lastThElement != null) {
                handler.cancelPopup(editor)
                lastThElement = null
            }
            return
        }

        val point = e.mouseEvent.point
        val logicalPos = editor.xyToLogicalPosition(point)
        val offset = editor.logicalPositionToOffset(logicalPos)
        val project = editor.project ?: run {
            handler.cancelPopup(editor)
            lastThElement = null
            return
        }
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: run {
             handler.cancelPopup(editor)
            lastThElement = null
            return
        }
        val element = psiFile.findElementAt(offset) ?: run {
            if (lastThElement != null) {
                 handler.cancelPopup(editor)
                lastThElement = null
            }
            return
        }

        val thElement = handler.findParentTag(element, "th")

        if (thElement != null) {
            if (thElement != lastThElement) {
                lastThElement = thElement
                handler.getGotoDeclarationTargets(thElement, thElement.textOffset, editor)
            }
        } else {
            if (lastThElement != null) {
                 handler.cancelPopup(editor)
                lastThElement = null
            }
        }
    }

    override fun mouseExited(e: EditorMouseEvent) {
        ctrlPressed = false
        lastThElement = null
    }

    override fun keyPressed(e: KeyEvent) {
        ctrlPressed = e.keyCode == KeyEvent.VK_CONTROL
    }

    override fun keyReleased(e: KeyEvent) {
        if (e.keyCode == KeyEvent.VK_CONTROL) {
            handler.cancelPopup(editor)
            ctrlPressed = false
            lastThElement = null
        }
    }

    override fun mouseEntered(e: EditorMouseEvent) {}
    override fun mousePressed(e: EditorMouseEvent) {}
    override fun mouseReleased(e: EditorMouseEvent) {}
    override fun keyTyped(e: KeyEvent) {}
}
