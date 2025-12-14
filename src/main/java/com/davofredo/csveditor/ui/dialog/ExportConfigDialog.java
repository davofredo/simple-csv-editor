package com.davofredo.csveditor.ui.dialog;

import com.davofredo.csveditor.data.CsvExportOptions;
import com.davofredo.csveditor.data.QuoteMode;

import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ExportConfigDialog extends JDialog {
    private JRadioButton rbDataAlways;
    private JRadioButton rbDataAsNeeded;
    private JRadioButton rbDataNever;

    private JRadioButton rbHeaderAlways;
    private JRadioButton rbHeaderAsNeeded;
    private JRadioButton rbHeaderNever;

    private JTextField txtSeparator;
    private boolean cancelled = true;

    public ExportConfigDialog(Frame owner) {
        super(owner, "Export Configuration", true);
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Data Quoting
        formPanel.add(new JLabel("Data Quoting:"), gbc);
        gbc.gridy++;

        JPanel pnlData = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup bgData = new ButtonGroup();
        rbDataAlways = new JRadioButton("Always");
        rbDataAsNeeded = new JRadioButton("As Needed");
        rbDataNever = new JRadioButton("Never");
        bgData.add(rbDataAlways);
        bgData.add(rbDataAsNeeded);
        bgData.add(rbDataNever);
        pnlData.add(rbDataAlways);
        pnlData.add(rbDataAsNeeded);
        pnlData.add(rbDataNever);
        rbDataAlways.setSelected(true); // Default

        // Warning label for Never
        JLabel lblWarning = new JLabel(
                "<html><body style='width: 300px'>Warning: 'Never' may produce invalid CSV<br>if data contains separators.</body></html>");
        lblWarning.setForeground(java.awt.Color.RED);
        lblWarning.setVisible(false);

        rbDataNever.addActionListener(e -> {
            lblWarning.setVisible(rbDataNever.isSelected());
            formPanel.revalidate();
            formPanel.repaint();
        });
        rbDataAlways.addActionListener(e -> lblWarning.setVisible(false));
        rbDataAsNeeded.addActionListener(e -> lblWarning.setVisible(false));

        formPanel.add(pnlData, gbc);

        gbc.gridy++;
        formPanel.add(lblWarning, gbc);

        // Header Quoting
        gbc.gridy++;
        formPanel.add(new JLabel("Header Quoting:"), gbc);
        gbc.gridy++;

        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup bgHeader = new ButtonGroup();
        rbHeaderAlways = new JRadioButton("Always");
        rbHeaderAsNeeded = new JRadioButton("As Needed");
        rbHeaderNever = new JRadioButton("Never");
        bgHeader.add(rbHeaderAlways);
        bgHeader.add(rbHeaderAsNeeded);
        bgHeader.add(rbHeaderNever);
        pnlHeader.add(rbHeaderAlways);
        pnlHeader.add(rbHeaderAsNeeded);
        pnlHeader.add(rbHeaderNever);
        rbHeaderAsNeeded.setSelected(true); // Default

        formPanel.add(pnlHeader, gbc);

        // Separator
        gbc.gridy++;
        JPanel pnlSep = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSep.add(new JLabel("Separator: "));
        txtSeparator = new JTextField(",", 3);
        txtSeparator.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (txtSeparator.getText().length() >= 1 || Character.isLetterOrDigit(c)) {
                    e.consume(); // Limit to 1 char, no alphanumeric
                }
            }
        });
        pnlSep.add(txtSeparator);
        formPanel.add(pnlSep, gbc);

        // Wrap form in ScrollPane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(450, 300)); // Default size suggestion

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons (Fixed at bottom)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        btnOk.addActionListener(e -> {
            if (txtSeparator.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Separator cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cancelled = false;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);

        mainPanel.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public CsvExportOptions getOptions() {
        if (cancelled)
            return null;

        QuoteMode dataMode = QuoteMode.ALWAYS;
        if (rbDataAsNeeded.isSelected())
            dataMode = QuoteMode.AS_NEEDED;
        if (rbDataNever.isSelected())
            dataMode = QuoteMode.NEVER;

        QuoteMode headerMode = QuoteMode.ALWAYS;
        if (rbHeaderAsNeeded.isSelected())
            headerMode = QuoteMode.AS_NEEDED;
        if (rbHeaderNever.isSelected())
            headerMode = QuoteMode.NEVER;

        return CsvExportOptions.builder()
                .dataQuoteMode(dataMode)
                .headerQuoteMode(headerMode)
                .separator(txtSeparator.getText())
                .build();
    }
}
