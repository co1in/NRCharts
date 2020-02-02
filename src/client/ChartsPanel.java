package client;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ChartsPanel extends JPanel {
    public final int MAX_CHARTS = 4;
    private LinkedList<SingleChartPanel> panels = new LinkedList<>();

    public ChartsPanel() {
        String[] prevCharts = Prefs.getInstance().get(Prefs.SELECTED_CHARTS_PREF).split("~", -1);
        if (prevCharts.length > 0) {
            setNumCharts(prevCharts.length, prevCharts);
        }

        refreshGrid();

        NRDataStore.getInstance().setKeysChangedListener(newKeys -> {
            panels.forEach(panel -> panel.updateKeysList(newKeys));
        });
    }

    public void increaseNumCharts() {
        int newNum = Math.min(panels.size()+1, MAX_CHARTS);
        if (newNum == 3)
            newNum = 4;

        setNumCharts(newNum);
    }

    public void decreaseNumCharts() {
        int newNum = Math.max(panels.size()-1, 1);
        if (newNum == 3)
            newNum = 2;
        setNumCharts(newNum);
    }

    public int getNumCharts() {
        return panels.size();
    }

    public void setNumCharts(int num) {
        setNumCharts(num, null);
    }

    public void setNumCharts(int num, String[] selections) {
        if (num > MAX_CHARTS || num < 1) {
            System.err.println("Invalid amount of charts" + num);
        } else {
            while(panels.size() < num) {
                String initialKey = selections != null && selections.length > panels.size() ? selections[panels.size()] : null;
                panels.add(new SingleChartPanel(initialKey, this::refreshSelections, this::refreshRequested));
            }
            while(panels.size() > num) {
                panels.removeLast();
            }

            refreshGrid();
            refreshSelections();
        }
    }

    private final Object timerLock = new Object();
    private boolean refreshScheduled = false;

    private void refreshRequested() {
        synchronized (timerLock) {
            if (!refreshScheduled) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (timerLock) {
                            repaintCharts();
                            refreshScheduled = false;
                        }
                    }
                }, 200);
            }
        }
    }

    private void refreshSelections() {
        Prefs.getInstance().put(
            Prefs.SELECTED_CHARTS_PREF,
            panels.stream()
                    .map(SingleChartPanel::getSelectedKey)
                    .collect(Collectors.joining("~"))
        );
    }

    public void refreshGrid() {
        this.removeAll();
        if(panels.size() == 1) {
            setLayout(new BorderLayout());
            add(panels.get(0), BorderLayout.CENTER);
        } else if (panels.size() == 2) {
            setLayout(new GridLayout(2, 1));
            panels.forEach(this::add);
        } else {
            setLayout(new GridLayout(2, 2));
            panels.forEach(this::add);
        }

        revalidate();
        repaint();
    }

    public void repaintCharts() {
        panels.forEach(SingleChartPanel::refreshChartData);
    }
}