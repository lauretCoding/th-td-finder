package com.github.lauretcoding.thtdfinder

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class CtrlHoverFactoryListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val listener = CtrlHoverListener(editor, ThHandlerService.handler)
        editor.contentComponent.addKeyListener(listener)
        editor.addEditorMouseListener(listener)
        editor.addEditorMouseMotionListener(listener)
    }
}
