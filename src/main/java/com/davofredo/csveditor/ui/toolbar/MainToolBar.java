package com.davofredo.csveditor.ui.toolbar;

import com.davofredo.event.EventGateway;
import com.davofredo.csveditor.ui.toolbar.event.OpenFileButtonListener;
import com.davofredo.csveditor.ui.toolbar.event.UndoButtonListener;
import com.davofredo.ui.util.IconUtils;

import javax.swing.*;

public class MainToolBar extends JToolBar {
    private JButton btnOpenFile;
    private JButton btnUndo;
    private EventGateway eventGateway;

    public MainToolBar(EventGateway eventGateway) {
        this.eventGateway = eventGateway;
        initComponents();
    }

    private void initComponents() {
        btnOpenFile = new JButton();
        btnOpenFile.setText(IconUtils.OPEN_ICON);
        btnOpenFile.setFont(IconUtils.getIconFont().deriveFont(16.0f));
        btnOpenFile.addActionListener(l -> postOpenFileButtonClicked());

        btnUndo = new JButton("\u21BA"); // Unicode for Counter-Clockwise Open Circle Arrow
        btnUndo.setFont(new java.awt.Font("Segoe UI Symbol", java.awt.Font.PLAIN, 16));
        btnUndo.addActionListener(l -> postUndoButtonClicked());

        add(btnOpenFile);
        add(btnUndo);
    }

    private void postOpenFileButtonClicked() {
        this.eventGateway
                .getEventListeners(OpenFileButtonListener.class)
                .forEach(OpenFileButtonListener::onOpenFileButtonClicked);
    }

    private void postUndoButtonClicked() {
        this.eventGateway
                .getEventListeners(UndoButtonListener.class)
                .forEach(UndoButtonListener::onUndoButtonClicked);
    }

}
