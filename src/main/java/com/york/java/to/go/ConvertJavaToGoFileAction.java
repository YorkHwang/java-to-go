package com.york.java.to.go;

import com.google.common.io.CharStreams;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.york.java.to.go.ui.TextCopyForm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConvertJavaToGoFileAction extends AnAction {
    @Override
    public void update(AnActionEvent e) {
        // Using the event, evaluate the context, and enable or disable the action.
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        final StringBuilder selectedText = new StringBuilder();
        if (file == null) {
            selectedText.append("no file selected!");
        } else {
            InputStreamReader inputStreamReader = null;
            try {
                if (!file.isDirectory() && Objects.equals(file.getExtension(), "java")) {
                    inputStreamReader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                    final String fileText = CharStreams.toString(inputStreamReader);
                    selectedText.append(fileText);
                } else {
                    selectedText.append("What your selected is not a java file");
                }
            } catch (Exception exception) {
                selectedText.append("read file error");
            } finally {
                if( inputStreamReader!=null){
                    try {
                        inputStreamReader.close();
                    } catch (IOException ex) {
                       //do nothing
                    }
                }
            }
        }
        TextCopyForm tf = new TextCopyForm(project);
        tf.setBuilder(selectedText.toString());
        tf.gen(1);
        tf.pack();
        tf.show();
    }

}
