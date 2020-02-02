package client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class NRDataStore {
    private static NRDataStore instance;
    public static NRDataStore getInstance() {
        if(instance == null) {
            instance = new NRDataStore();
        }
        return instance;
    }

    private Consumer<String> dataListener;

    private final HashMap<String, ChartSeriesData> data = new HashMap<>();

    public void setDataChangedListener(Consumer<String> consumer) {
        this.dataListener = consumer;
    }

    private Consumer<String[]> keysChangedListener;
    public void setKeysChangedListener(Consumer<String[]> listener) {
        this.keysChangedListener = listener;
    }

    private boolean dataPaused = false;

    private NRDataStore() {
        String keys = Prefs.getInstance().get(Prefs.KEYS_PREF);
        String[] split = keys.split("~");
        for (String s : split) {
            if (!s.equals(""))
                data.put(s, new ChartSeriesData());
        }
    }

    public void setDataInputPaused(boolean value) {
        dataPaused = value;
    }

    public boolean isDataPaused() {
        return dataPaused;
    }

    private HashMap<String, LinkedList<Runnable>> dataChangeListeners = new HashMap<>();

    public void addDataChangeListener(String key, Runnable r) {
        if (!dataChangeListeners.containsKey(key))
            dataChangeListeners.put(key, new LinkedList<>());
        dataChangeListeners.get(key).push(r);
    }

    public void removeDataChangeListener(String key, Runnable r) {
        dataChangeListeners.getOrDefault(key, new LinkedList<>()).remove(r);
    }

    public void putData(String key, long timestamp, double value) {
        if(!dataPaused) {
            synchronized (data) {
                if(!data.containsKey(key)) {
                    data.put(key, new ChartSeriesData());
                    if (keysChangedListener != null) {
                        keysChangedListener.accept(getKeys());
                    }
                    Prefs.getInstance().put(Prefs.KEYS_PREF, String.join("~", getKeys()));
                }
                ChartSeriesData dataForKey = data.get(key);
                if(dataForKey.isEmpty()) {
                    dataForKey.setStartTime(timestamp);
                }
                dataForKey.addDataPoint((timestamp - dataForKey.getStartTime()) / 1000.0, value);

                if (dataChangeListeners.containsKey(key)) {
                    dataChangeListeners.get(key).forEach(Runnable::run);
                }
            }

            if(dataListener != null) {
                dataListener.accept(key);
            }
        }
    }

    public ChartSeriesData getData(String key) {
        return data.getOrDefault(key, null);
    }

    public String[] getKeys() {
        synchronized (data) {
            return new LinkedList<>(data.keySet()).toArray(new String[0]);
        }
    }

    public void clearAllData() {
        synchronized (data) {
            data.forEach((key, value) -> {
                value.clear();
                if (dataChangeListeners.containsKey(key))
                    dataChangeListeners.get(key).forEach(Runnable::run);
                if (dataListener != null) {
                    dataListener.accept(key);
                }
            });
        }
    }

    public void exportData() {
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
            PrintWriter out = new PrintWriter(new FileOutputStream(new File("nrcharts " + date + ".csv")));

            synchronized (data) {
                String[] keys = getKeys();
                int numLines = 0;
                ArrayList<ChartSeriesData> allData = new ArrayList<>();

                for (String key : keys) {
                    numLines = Math.max(numLines, data.get(key).size());
                    allData.add(data.get(key));

                    out.print(key.replace(",", "") + " Value,");
                    out.print(key.replace(",", "") + " Time (s)");
                    if (!key.equals(keys[keys.length-1]))
                        out.print(",");
                }
                out.println();


                for(int i = 0; i < numLines; i++) {
                    for (int p = 0; p < allData.size(); p++) {
                        if (i < allData.get(p).size()) {
                            out.print(allData.get(p).getYData().get(i) + "," + allData.get(p).getXData().get(i));
                        } else {
                            out.print(",");
                        }

                        if(p != allData.size() - 1) {
                            out.print(",");
                        }
                    }

                    out.println();
                }
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class ChartSeriesData {
    private LinkedList<Double> yData = new LinkedList<>();
    private LinkedList<Double> xData = new LinkedList<>();

    long startTime;

    public void clear() {
        yData.clear();
        xData.clear();
    }

    public void addDataPoint(double x, double y) {
        xData.add(x);
        yData.add(y);
    }

    public boolean isEmpty() {
        return yData.size() == 0;
    }

    public int size() {
        return yData.size();
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public LinkedList<Double> getXData() {
        return xData;
    }

    public LinkedList<Double> getYData() {
        return yData;
    }
}
