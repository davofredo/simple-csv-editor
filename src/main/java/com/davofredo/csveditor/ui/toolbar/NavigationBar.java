package com.davofredo.csveditor.ui.toolbar;

import com.davofredo.csveditor.data.CsvPagination;

// Import all javax.swing classes individually to avoid loading unused classes
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class NavigationBar extends JToolBar {
    private CsvPagination pagination;

    private JLabel lblPageSize;
    private JTextField txtPageSize;
    private JButton btnPrevPage;
    private JLabel lblPageNum;
    private JTextField txtCurrPage;
    private JLabel lblPageTotal;
    private JButton btnNextPage;

    public NavigationBar(CsvPagination pagination) {
        this.pagination = pagination;
        initComponents();
    }

    private void initComponents() {
        var prefHeight = 28;
        lblPageSize = new JLabel("Page Size: ");
        txtPageSize = new JTextField();
        btnPrevPage = new JButton("<");
        lblPageNum = new JLabel(" Page: ");
        txtCurrPage = new JTextField();
        lblPageTotal = new JLabel();
        btnNextPage = new JButton(">");

        // Setup dimensions
        txtPageSize.setMaximumSize(new Dimension(80, prefHeight));
        txtCurrPage.setMaximumSize(new Dimension(80, prefHeight));

        add(lblPageSize);
        add(txtPageSize);
        addSeparator();
        add(btnPrevPage);
        add(lblPageNum);
        add(txtCurrPage);
        add(lblPageTotal);
        add(btnNextPage);

        updateComponents();
    }

    public void updateComponents(CsvPagination pagination) {
        this.pagination = pagination;
        updateComponents();
    }

    public void updateComponents() {
        txtPageSize.setText(String.valueOf(pagination.getPageSize()));
        txtCurrPage.setText(String.valueOf(pagination.getPageIndex() + 1));
        lblPageTotal.setText(" of " + pagination.getPageCount());
        btnPrevPage.setEnabled(pagination.hasPreviousPage());
        btnNextPage.setEnabled(pagination.hasNextPage());
    }

    public void addNextPageListener(ActionListener listener) {
        btnNextPage.addActionListener(listener);
    }

    public void addPreviousPageListener(ActionListener listener) {
        btnPrevPage.addActionListener(listener);
    }

    public void addJumpToPageListener(Consumer<Long> listener) {
        txtCurrPage.addActionListener(e -> {
            try {
                String text = txtCurrPage.getText();
                long page = Long.parseLong(text);
                long totalPages = pagination.getPageCount();

                // Validate
                if (page < 1 || page > totalPages) {
                    // Revert to current (1-based)
                    txtCurrPage.setText(String.valueOf(pagination.getPageIndex() + 1));
                    return;
                }

                // Check if different
                long currentPageOneBased = pagination.getPageIndex() + 1;
                if (page != currentPageOneBased) {
                    listener.accept(page - 1); // Pass 0-based index
                } else {
                    // Even if same, maybe just refocus or ensures text is clean
                    txtCurrPage.setText(String.valueOf(currentPageOneBased));
                }
            } catch (NumberFormatException ex) {
                // Revert on bad input
                txtCurrPage.setText(String.valueOf(pagination.getPageIndex() + 1));
            }
        });

        txtCurrPage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                txtCurrPage.setText(String.valueOf(pagination.getPageIndex() + 1));
            }
        });

        txtCurrPage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    txtCurrPage.setText(String.valueOf(pagination.getPageIndex() + 1));
                    txtCurrPage.transferFocus();
                }
            }
        });
    }

}
