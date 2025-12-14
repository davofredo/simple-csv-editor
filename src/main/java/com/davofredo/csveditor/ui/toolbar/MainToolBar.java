package com.davofredo.csveditor.ui.toolbar;

import com.davofredo.event.EventGateway;
import com.davofredo.csveditor.ui.toolbar.event.OpenFileButtonListener;
import com.davofredo.csveditor.ui.toolbar.event.UndoButtonListener;
import com.davofredo.csveditor.ui.toolbar.event.RedoButtonListener;
import com.davofredo.ui.util.IconUtils;

import javax.swing.*;

public class MainToolBar extends JToolBar {
    private JButton btnOpenFile;
    private JButton btnUndo;
    private JButton btnRedo;
    private JButton btnSave;
    private EventGateway eventGateway;

    public MainToolBar(EventGateway eventGateway) {
        this.eventGateway = eventGateway;
        initComponents();
    }

    private void initComponents() {
        btnOpenFile = new JButton();
        btnOpenFile.setText(IconUtils.getIcon("folder-open"));
        btnOpenFile.setFont(IconUtils.getIconFont().deriveFont(16.0f));
        btnOpenFile.addActionListener(l -> postOpenFileButtonClicked());

        btnUndo = new JButton(IconUtils.getIcon("undo"));
        btnUndo.setFont(IconUtils.getIconFont().deriveFont(16.0f));
        btnUndo.addActionListener(l -> postUndoButtonClicked());

        btnRedo = new JButton(IconUtils.getIcon("redo"));
        btnRedo.setFont(IconUtils.getIconFont().deriveFont(16.0f));
        btnRedo.addActionListener(l -> postRedoButtonClicked());

        btnSave = new JButton(IconUtils.getIcon("floppy-disk"));
        btnSave.setFont(IconUtils.getIconFont().deriveFont(16.0f));
        btnSave.addActionListener(l -> postSaveButtonClicked());

        add(btnOpenFile);
        add(btnSave);
        addSeparator();
        add(btnUndo);
        add(btnRedo);
    }

    private void postRedoButtonClicked() {
        this.eventGateway
                .getEventListeners(RedoButtonListener.class)
                .forEach(RedoButtonListener::onRedoButtonClicked);
    }

    private void postSaveButtonClicked() {
        /*
         * this.eventGateway
         * .getEventListeners(SaveButtonListener.class)
         * .forEach(SaveButtonListener::onSaveButtonClicked);
         */
        System.err.println("Save action not implemented yet");
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
