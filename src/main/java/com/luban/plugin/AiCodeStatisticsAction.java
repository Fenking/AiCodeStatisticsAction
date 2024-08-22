package com.luban.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.TextRange;


public class AiCodeStatisticsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        SelectionModel selectionModel = editor.getSelectionModel();

        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();
        if (start == end) {
            return; // 空集无代码
        }

        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            int startLine = document.getLineNumber(start);
            int endLine = document.getLineNumber(end);

            String startComment = "//AI start Copilot";
            String endComment = "//AI end Copilot";

            // 获取start行缩进
            int startLineStart = document.getLineStartOffset(startLine);
            String startLineText = document.getText(new TextRange(startLineStart, start));
            String startLineIndent = startLineText.replaceAll("[^ \t].*", "");

            // 获取end行的缩进
            int endLineStart = document.getLineStartOffset(endLine);
            String endLineText = document.getText(new TextRange(endLineStart, end));
            String endLineIndent = endLineText.replaceAll("[^ \t].*", "");
            int endLineEnd = document.getLineEndOffset(endLine);

            // 获取前一行的注释
            int prevLineStart = document.getLineStartOffset(startLine - 1);
            String prevLineText = document.getText(new TextRange(prevLineStart, document.getLineEndOffset(startLine - 1)));

            // 获取后一行的注释
            int nextLineStart = document.getLineStartOffset(endLine + 1);
            String nextLineText = document.getText(new TextRange(nextLineStart, document.getLineEndOffset(endLine + 1)));

            if (nextLineText.contains(endComment) && prevLineText.contains(startComment)) {
                // 删除start/end注释
                document.deleteString(nextLineStart, nextLineStart + endLineIndent.length() + endComment.length() + 1);
                document.deleteString(prevLineStart, prevLineStart + startLineIndent.length() + startComment.length() + 1);
            } else {
                // 插入start/end注释
                document.insertString(endLineEnd, "\n" + endLineIndent + endComment);
                document.insertString(startLineStart, startLineIndent + startComment + "\n");
            }

        });
    }
}
