package com.york.java.to.go.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.york.java.to.go.domain.JtgParm;
import com.york.java.to.go.domain.GoCodeResult;
import com.york.java.to.go.service.JavaToGo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class TextCopyForm extends DialogWrapper {
    private static final String tagPackageKey = "go-tag-tpl";

    private JPanel panel1;
    private JTextArea t1TextArea;
    private JTextArea t2TextArea;
    private JButton copyButton;
    private JTextField tagTextField;
    private JCheckBox hessain2CheckBox;
    private JButton copyBnt;

    public TextCopyForm(@Nullable Project project) {
        super(project, null, false, IdeModalityType.IDE, false);
        setTitle("Convert To Go");
        init();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return TextCopyForm.class.getName();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(t2TextArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            dispose();
        });

        copyBnt.addActionListener(e -> {
            StringSelection selection = new StringSelection(t2TextArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });



        tagTextField.setText("");
        tagTextField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        gen(2);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        gen(2);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        gen(2);
                    }
        });

        hessain2CheckBox.addChangeListener(e -> gen(2));
        hessain2CheckBox.setSelected(Boolean.TRUE);
        return panel1;
    }

    private String getTpl() {
        return PropertiesComponent.getInstance().getValue(tagPackageKey, "");
    }

    private void setTpl(String tpl) {
        PropertiesComponent.getInstance().setValue(tagPackageKey, tpl, "");
    }

    public void gen(int type) {
        final JtgParm jtgParm = new JtgParm();
        final String javaCode = this.t1TextArea.getText();
        if(StringUtils.isBlank(javaCode)) {
            this.t1TextArea.setText("请选中java类代码");
            return;
        }

        jtgParm.setJava(this.t1TextArea.getText());
        jtgParm.setStructType(hessain2CheckBox.isSelected()?1:0);
        if(StringUtils.isNotBlank(tagTextField.getText())) {
            jtgParm.setPackageName(tagTextField.getText());
        }
        GoCodeResult result;
        try {
            result = JavaToGo.parseToGo(jtgParm);
            this.t2TextArea.setText(result.getGoCode());
            if (type != 2) {
                this.tagTextField.setText(result.getPackageName());
            }
        } catch (Exception e){
            this.t2TextArea.setText("转换失败，请检查是否选择完整的java类代码");
        }
    }

    public void setBuilder(String text) {
        this.t1TextArea.setText(text);
    }
}

