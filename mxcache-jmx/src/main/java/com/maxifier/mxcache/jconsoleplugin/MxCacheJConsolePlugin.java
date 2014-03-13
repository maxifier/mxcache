/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jconsoleplugin;

import com.sun.tools.jconsole.JConsolePlugin;

import javax.management.openmbean.InvalidKeyException;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.awt.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings("UnusedDeclaration")
// used by JConsole
public class MxCacheJConsolePlugin extends JConsolePlugin {
    private final JPanel panel;

    private final JPanel resourcesPanel;

    private final JList resourcesList;
    private final DefaultListModel resourcesListModel;

    private final MxCacheTableModel model;
    private final JCheckBox regex;
    private final JTextField searchText;
    private final TableRowSorter<TableModel> sorter;
    private final JCheckBox matchCase;

    private final JComboBox tagsCombo;
    private final JComboBox groupsCombo;
    private final JComboBox classesCombo;
    private final JComboBox annotationsCombo;

    private final Color searchTextBg;
    private final Map<com.maxifier.mxcache.jconsoleplugin.Attribute, JCheckBox> searchIn;

    private final JLabel errorLabel;

    private static final String[] STRING_SIGNATURE = { String.class.getCanonicalName() };
    private static final ObjectName JMX_NAME;
    private static final Color INVALID_PATTERN_BG = new Color(0xFF, 0x80, 0x80);

    static {
        try {
            JMX_NAME = new ObjectName("com.maxifier.mxcache:service=CacheControl");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    public MxCacheJConsolePlugin() {
        panel = new JPanel(new BorderLayout());
        resourcesPanel = new JPanel(new BorderLayout());

        resourcesListModel = new DefaultListModel();
        resourcesList = new JList(resourcesListModel);

        resourcesPanel.add(new JScrollPane(resourcesList), BorderLayout.CENTER);
        JButton clearResourceButton = new JButton("Clear resource");
        resourcesPanel.add(clearResourceButton, BorderLayout.SOUTH);

        clearResourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String resName = (String) resourcesList.getSelectedValue();
                try {
                    getContext().getMBeanServerConnection().invoke(JMX_NAME, "clearByResource", new Object[] { resName }, STRING_SIGNATURE);
                } catch (Exception er) {
                    er.printStackTrace();
                }
            }
        });

        model = new MxCacheTableModel();

        final JTable table = new JTable(model);

        final JPopupMenu popup = new JPopupMenu();

        table.getTableHeader().addMouseListener(new ShowPopupMouseAdapter(popup, table));

        Enumeration<TableColumn> columnEnumeration = table.getColumnModel().getColumns();
        final List<TableColumn> columns = new ArrayList<TableColumn>();
        while (columnEnumeration.hasMoreElements()) {
            columns.add(columnEnumeration.nextElement());
        }
        final boolean[] visible = new boolean[columns.size()];
        Arrays.fill(visible, true);

        for (final TableColumn column : columns) {
            final JCheckBox c = new JCheckBox(column.getIdentifier().toString());
            c.setSelected(true);
            popup.add(c);

            c.addChangeListener(new ColumnVisibilityChangeListener(column, c, visible, table));
        }

        sorter = new TableRowSorter<TableModel>(model);

        for (com.maxifier.mxcache.jconsoleplugin.Attribute attribute : com.maxifier.mxcache.jconsoleplugin.Attribute.values()) {
            Comparator comparator = attribute.getComparator();
            if (comparator != null) {
                sorter.setComparator(attribute.ordinal(), comparator);
            }
        }

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));

        search.add(new JLabel("Search text "));

        regex = new JCheckBox("Regex");

        matchCase = new JCheckBox("Match case");

        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateSorting();
            }
        };

        searchText = new JTextField(10);
        search.add(searchText);
        search.add(regex);
        search.add(matchCase);
        search.add(new JLabel("Search in "));
        searchIn = new EnumMap<com.maxifier.mxcache.jconsoleplugin.Attribute, JCheckBox>(com.maxifier.mxcache.jconsoleplugin.Attribute.class);
        for (com.maxifier.mxcache.jconsoleplugin.Attribute attribute : com.maxifier.mxcache.jconsoleplugin.Attribute.values()) {
            if (attribute.isSearchable()) {
                JCheckBox check = new JCheckBox(attribute.getName(), true);
                searchIn.put(attribute, check);
                check.addChangeListener(listener);
                search.add(check);
            }
        }
        JButton all = new JButton("All");
        search.add(all);
        all.addActionListener(new SetAllAction(true));

        JButton none = new JButton("None");
        search.add(none);
        none.addActionListener(new SetAllAction(false));

        errorLabel = new JLabel();
        search.add(errorLabel);

        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateSorting();
            }
        });
        searchTextBg = searchText.getBackground();
        regex.addChangeListener(listener);
        matchCase.addChangeListener(listener);

        table.setRowSorter(sorter);

        table.setDefaultRenderer(Object.class, new MxCacheTableRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JPanel clear = new JPanel(new FlowLayout(FlowLayout.LEFT));

        classesCombo = initClearComponent(clear, "Class: ", "clearByClass", true);
        groupsCombo = initClearComponent(clear, "Group: ", "clearByGroup", false);
        tagsCombo = initClearComponent(clear, "Tag: ", "clearByTag", false);
        annotationsCombo = initClearComponent(clear, "Annotation: ", "clearByTag", true);

        panel.add(search, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(clear, BorderLayout.SOUTH);
    }

    private JComboBox initClearComponent(JPanel clear, String caption, String method, boolean shortcutClassNames) {
        clear.add(new JLabel(caption));
        JComboBox combo = new JComboBox();
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new CleanerAction(combo, method));
        clear.add(combo);
        clear.add(clearButton);
        if (shortcutClassNames) {
            combo.setRenderer(new ShortcutListCellRenderer());
        }
        return combo;
    }

    static class MxCacheTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                Insets insets = l.getInsets();
                int w = l.getPreferredSize().width
                        + insets.left + insets.right;
                TableColumn col = table.getColumnModel().getColumn(column);
                col.setPreferredWidth(Math.max(col.getPreferredWidth(), w));
            }
            return c;
        }
    }

    private void updateSorting() {
        int[] columns = getColumnsToSearch();
        boolean ok;
        try {
            RowFilter<Object, Object> filter = RowFilter.regexFilter(getPattern(), columns);
            sorter.setRowFilter(filter);
            ok = true;
        } catch (PatternSyntaxException e) {
            ok = false;
        }
        searchText.setBackground(ok ? searchTextBg : INVALID_PATTERN_BG);
    }

    private int[] getColumnsToSearch() {
        EnumSet<com.maxifier.mxcache.jconsoleplugin.Attribute> attributes = EnumSet.noneOf(com.maxifier.mxcache.jconsoleplugin.Attribute.class);
        for (Map.Entry<com.maxifier.mxcache.jconsoleplugin.Attribute, JCheckBox> e : searchIn.entrySet()) {
            if (e.getValue().isSelected()) {
                attributes.add(e.getKey());
            }
        }
        int[] columns = new int[attributes.size()];
        int i = 0;
        for (com.maxifier.mxcache.jconsoleplugin.Attribute attribute : attributes) {
            columns[i++] = attribute.ordinal();
        }
        return columns;
    }

    private String getPattern() {
        String text = searchText.getText();
        String rawPattern = regex.isSelected() ? text : Pattern.quote(text);
        return matchCase.isSelected() ? rawPattern : ("(?i)" + rawPattern);
    }

    @Override
    public Map<String, JPanel> getTabs() {
        Map<String, JPanel> res = new HashMap<String, JPanel>();
        res.put("MxCache", panel);
        res.put("MxResources", resourcesPanel);
        return res;
    }

    @Override
    public SwingWorker<?, ?> newSwingWorker() {
        return new UpdateUIWorker();
    }

    private void fillList(DefaultListModel listModel, Set<String> resources) {
        listModel.clear();
        for (String resource : resources) {
            listModel.addElement(resource);
        }
    }

    private void fillCombo(JComboBox combo, Set<String> set) {
        combo.removeAllItems();
        for (String tag : set) {
            combo.addItem(tag);
        }
    }

    private class CleanerAction implements ActionListener {
        private final JComboBox combo;
        private final String methodName;

        public CleanerAction(JComboBox combo, String methodName) {
            this.combo = combo;
            this.methodName = methodName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object tag = combo.getSelectedItem();
            if (tag != null) {
                String tagString = tag.toString().trim();
                if (!tagString.isEmpty()) {
                    try {
                        getContext().getMBeanServerConnection().invoke(JMX_NAME, methodName, new Object[] { tagString }, STRING_SIGNATURE);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                }
            }
        }
    }

    private class SetAllAction implements ActionListener {
        private final boolean flag;

        public SetAllAction(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (JCheckBox check : searchIn.values()) {
                check.setSelected(flag);
            }
        }
    }

    private class UpdateUIWorker extends SwingWorker {
        private Object[][] rows;
        private final Set<String> tags = new TreeSet<String>();
        private final Set<String> groups = new TreeSet<String>();
        private final Set<String> classes = new TreeSet<String>();
        private final Set<String> annotations = new TreeSet<String>();

        private final Set<String> resources = new TreeSet<String>();

        private boolean failed;
        private String error;

        @Override
        protected Object doInBackground() throws Exception {
            try {
                CompositeData[] resources = (CompositeData[]) getContext().getMBeanServerConnection().getAttribute(JMX_NAME, "Resources");

                for (CompositeData resource : resources) {
                    this.resources.add(resource.get("name").toString());
                }

                CompositeData[] caches = (CompositeData[]) getContext().getMBeanServerConnection().getAttribute(JMX_NAME, "Caches");

                com.maxifier.mxcache.jconsoleplugin.Attribute[] attributes = com.maxifier.mxcache.jconsoleplugin.Attribute.values();
                rows = new Object[caches.length][attributes.length];

                for (int i = 0; i < caches.length; i++) {
                    CompositeData cache = caches[i];
                    String[] tagsAndAnnotations = (String[]) cache.get("tags");
                    if (tagsAndAnnotations != null) {
                        fillTags(tagsAndAnnotations);
                    }
                    String group = (String) cache.get("group");
                    if (group != null) {
                        groups.add(group);
                    }

                    String owner = (String) cache.get("owner");
                    if (owner != null) {
                        classes.add(owner);
                    }
                    fillRow(cache, rows[i]);
                }
                return null;
            } catch (Exception e) {
                failed = true;
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
                throw e;
            }
        }

        private void fillRow(CompositeData cache, Object[] row) {
            for (com.maxifier.mxcache.jconsoleplugin.Attribute attr : com.maxifier.mxcache.jconsoleplugin.Attribute.values()) {
                try {
                    Object v = cache.get(attr.getKey());
                    Object s = v == null ? "" : attr.transform(v);
                    // todo add posibility to switch shortcutting off
                    row[attr.ordinal()] = attr.isShortcutable() ? com.maxifier.mxcache.jconsoleplugin.Attribute.shortcutClassNames(s) : s;
                } catch (InvalidKeyException e) {
                    // some attributes were added lately, so they may be missing
                    row[attr.ordinal()] = "";
                }
            }
        }

        private void fillTags(String[] tagsAndAnnotations) {
            for (String tag : tagsAndAnnotations) {
                if (tag.startsWith("@")) {
                    this.annotations.add(tag);
                } else {
                    this.tags.add(tag);
                }
            }
        }

        @Override
        protected void done() {
            if (failed) {
                errorLabel.setText("<html><font color=\"red\"><b>" + error + "</b></font><html>");
            } else {
                errorLabel.setText("");
                fillCombo(classesCombo, classes);
                fillCombo(groupsCombo, groups);
                fillCombo(tagsCombo, tags);
                fillCombo(annotationsCombo, annotations);
                fillList(resourcesListModel, resources);
                model.setRows(rows);
            }
        }

    }

    private static class ShortcutListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("");
            } else {
                setText(com.maxifier.mxcache.jconsoleplugin.Attribute.shortcutClassNames(value.toString()));
            }
            return this;
        }
    }

    private static class ShowPopupMouseAdapter extends MouseAdapter {
        private final JPopupMenu popup;
        private final JTable table;

        public ShowPopupMouseAdapter(JPopupMenu popup, JTable table) {
            this.popup = popup;
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                popup.show(table.getTableHeader(), e.getX(), e.getY());
            }
        }
    }

    private static class ColumnVisibilityChangeListener implements ChangeListener {
        private final TableColumn column;
        private final JCheckBox c;
        private final boolean[] visible;
        private final JTable table;

        public ColumnVisibilityChangeListener(TableColumn column, JCheckBox c, boolean[] visible, JTable table) {
            this.column = column;
            this.c = c;
            this.visible = visible;
            this.table = table;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            int index = column.getModelIndex();
            boolean v = c.isSelected();
            if (v != visible[index]) {
                visible[index] = v;
                TableColumnModel columnModel = table.getColumnModel();
                if (v) {
                    columnModel.addColumn(column);
                } else {
                    columnModel.removeColumn(column);
                }
            }
        }
    }
}
