package com.davofredo.ui.dialog;

import com.davofredo.event.listener.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class ProgressDialog extends JDialog {
    private SwingWorker<Void, Float> worker;
    private Frame owner;
    private JProgressBar progressBar;

    public ProgressDialog(Frame owner, String title) {
        this(owner, title, owner != null);
        this.owner = owner;
    }

    public ProgressDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        initComponents();
    }

    private void initComponents() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar);
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Ensure this background process to post progress by calling
     * <code>ProgressListener::onProgressUpdate</code>
     *
     * @param backgroundProcess process to run in background, which posts progress updates.
     * @return this ProgressDialog instance.
     */
    public ProgressDialog doInBackground(BackgroundProcess backgroundProcess) {
        if (worker != null && worker.getState() == SwingWorker.StateValue.STARTED)
            throw new IllegalStateException("Cannot setup a background worker because there is a worker in progress.");

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                backgroundProcess.run(this::publish);
                return null;
            }

            @Override
            protected void process(java.util.List<Float> chunks) {
                int progress = (int) (chunks.getLast() * 100);
                progressBar.setValue(progress);
            }

            @Override
            protected void done() {
                ProgressDialog.this.dispose();
                JOptionPane.showMessageDialog(owner, "Completed!");
            }
        };
        return this;
    }

    public ProgressDialog addWorkerPropertyChangeListener(PropertyChangeListener listener) {
        worker.addPropertyChangeListener(listener);
        return this;
    }

    public void start() {
        SwingUtilities.invokeLater(() -> setVisible(true));
        worker.execute();
    }

    public interface BackgroundProcess {
        void run(ProgressListener listener);
    }

}
