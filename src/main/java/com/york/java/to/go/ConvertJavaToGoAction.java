package com.york.java.to.go;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.york.java.to.go.ui.TextCopyForm;
import org.jetbrains.annotations.NotNull;

public class ConvertJavaToGoAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project =
            e.getData(PlatformDataKeys.PROJECT);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        String selectedText = editor.getDocument().getText();
        TextCopyForm tf = new TextCopyForm(project);
        tf.setBuilder(selectedText);
        tf.gen(1);
        tf.pack();
        tf.show();
    }
}
