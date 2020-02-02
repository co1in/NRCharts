package client;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

class SingleChartPanel extends JPanel {
    private XChartPanel<XYChart> chartPanel;
    private XYChart chart;
    private String selectedKey = null;
    JComboBox<String> keySelector;
    private String[] keys;

    private boolean needsRefresh = false;

    private Runnable selectionChangedListener;
    private Runnable requestRefreshListener;

    private Runnable dataChangedListener = () -> {
        requestRefreshListener.run();
        needsRefresh = true;
    };

    ActionListener keyActionListener = e -> {
        if (keySelector.getSelectedIndex() == 0) {
            NRDataStore.getInstance().removeDataChangeListener(selectedKey, dataChangedListener);
            selectedKey = null;
            needsRefresh = true;
            refreshChartData();
        } else {
            String newKey = keys[keySelector.getSelectedIndex()-1];
            if (!newKey.equals(selectedKey)) {
                if (selectedKey != null)
                    NRDataStore.getInstance().removeDataChangeListener(selectedKey, dataChangedListener);
                selectedKey = newKey;
                NRDataStore.getInstance().addDataChangeListener(selectedKey, dataChangedListener);
                needsRefresh = true;
                refreshChartData();
            }
        }
        if(selectionChangedListener != null) {
            selectionChangedListener.run();
        }
    };

    public SingleChartPanel(String initialKey, Runnable selectionChangedListener, Runnable requestRefreshListener) {
        this.requestRefreshListener = requestRefreshListener;
        chart = new XYChart(600, 400);
        chart.setXAxisTitle("Time (s)");
        chartPanel = new XChartPanel<>(chart);

        this.setLayout(new BorderLayout());
        keySelector = new JComboBox<>();
        String[] keys = NRDataStore.getInstance().getKeys();
        updateKeysList(keys);
        if (initialKey != null && !initialKey.equals("")) {
            int initialIndex = -1;
            for (int i = 0; i < keys.length; i++) {
                if(keys[i].equals(initialKey)) {
                    initialIndex = i+1;
                    break;
                }
            }

            if (initialIndex != -1) {
                keySelector.setSelectedIndex(initialIndex);
            }
        }

        this.add(keySelector, BorderLayout.NORTH);
        this.selectionChangedListener = selectionChangedListener;
    }

    public void clearKeys() {
        updateKeysList(new String[0]);
        needsRefresh = true;
        refreshChartData();
    }

    public String getSelectedKey() {
        if (selectedKey == null)
            return "";
        return selectedKey;
    }

    public void refreshChartData() {
        if (needsRefresh) {
            // If we are unselecting this chart
            if (selectedKey == null) {
                remove(chartPanel);
                String[] prevKeys = chart.getSeriesMap().keySet().toArray(new String[0]);
                for (String prevKey : prevKeys) {
                    chart.removeSeries(prevKey);
                }
            } else {
                ChartSeriesData data = NRDataStore.getInstance().getData(selectedKey);
                if(chart.getSeriesMap().keySet().size() == 0) {
                    chart.setYAxisTitle(selectedKey);
                    chart.getStyler().setLegendVisible(false);
                    if(!data.isEmpty())
                        chart.addSeries(selectedKey, data.getXData(), data.getYData());
                } else {
                    chart.updateXYSeries(selectedKey, data.getXData(), data.getYData(), null);
                }
                if(chartPanel.getParent() != this) {
                    add(chartPanel, BorderLayout.CENTER);
                }
            }
            revalidate();
            repaint();
        }
    }

    public void updateKeysList(String[] keys) {
        this.keys = keys;
        keySelector.removeActionListener(keyActionListener);
        int oldIndex = keySelector.getSelectedIndex();
        keySelector.removeAllItems();
        keySelector.addItem("(Select a chart to view)");
        for (String key : keys) {
            keySelector.addItem(key);
        }
        keySelector.setSelectedIndex(oldIndex == -1 ? 0 : oldIndex);
        keySelector.addActionListener(keyActionListener);
    }
}