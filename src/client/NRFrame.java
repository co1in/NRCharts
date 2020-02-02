package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class NRFrame extends JFrame {
    JPanel mainPanel = new JPanel();
    JPanel menuPanel = new JPanel(new BorderLayout());
    ChartsPanel chartPanel = new ChartsPanel();

    JPanel statusLight = new JPanel();
    JLabel statusText = new JLabel();
    JPanel statusPanel = new JPanel(new GridBagLayout());

    JPanel controlsPanel = new JPanel();
    JTextField serverField = new JTextField(20);

    JButton addCharts, removeCharts;

    public NRFrame() {
        setupFrame();
        NRClient.initialize();
        NRClient.getInstance().addConnectionListener(this::setConnectionState);

        String server = Prefs.getInstance().get(Prefs.SERVER_PREF);
        serverField.setText(server);
        if (server != null) {
            NRClient.getInstance().setServerAddress(serverField.getText());
        }
    }

    private void setupFrame() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Prefs.getInstance().getInt(Prefs.WIDTH_PREF, (int)Math.round(screenSize.getWidth() * 0.75));
        int height = Prefs.getInstance().getInt(Prefs.HEIGHT_PREF, (int)Math.round(screenSize.getHeight() * 0.75));
        this.setSize(width, height);
        this.setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Prefs.getInstance().putInt(Prefs.WIDTH_PREF, getWidth());
                Prefs.getInstance().putInt(Prefs.HEIGHT_PREF, getHeight());
            }
        });

        setTitle("Nashoba Robotics Charts");

        mainPanel.setLayout(new BorderLayout());

        setConnectionState(false);

        menuPanel.setBackground(new Color(200, 200, 200));

        statusLight.setPreferredSize(new Dimension(15, 15));
        statusPanel.setBackground(null);
        GridBagConstraints c = new GridBagConstraints();

        statusPanel.add(createSpacer(15, 0));
        statusPanel.add(statusLight, c);
        statusPanel.add(createSpacer(5, 0));
        statusPanel.add(statusText, c);

        menuPanel.add(statusPanel, BorderLayout.WEST);

        controlsPanel.setBackground(null);

        addCharts = new JButton("+");
        addCharts.addActionListener(e -> {
            chartPanel.increaseNumCharts();
            refreshButtonsEnabled();
        });
        removeCharts = new JButton("-");
        removeCharts.addActionListener(e -> {
            chartPanel.decreaseNumCharts();
            refreshButtonsEnabled();
        });
        refreshButtonsEnabled();

        controlsPanel.add(removeCharts);
        controlsPanel.add(new JLabel("# Charts"));
        controlsPanel.add(addCharts);

        controlsPanel.add(createSpacer(10, 0));

        JButton exportButton = new JButton("Export Data");
        exportButton.addActionListener(e -> NRDataStore.getInstance().exportData());
        controlsPanel.add(exportButton);

        JButton clearCharts = new JButton("Clear Charts");
        clearCharts.addActionListener(e -> NRDataStore.getInstance().clearAllData());
        clearCharts.setFocusPainted(false);
        controlsPanel.add(clearCharts);

        controlsPanel.add(new JLabel("Server: "));

        serverField.addActionListener(e -> {
            NRClient.getInstance().setServerAddress(serverField.getText());
            Prefs.getInstance().put(Prefs.SERVER_PREF, serverField.getText());
        });
        controlsPanel.add(serverField);

        menuPanel.add(controlsPanel, BorderLayout.EAST);

        mainPanel.add(menuPanel, BorderLayout.NORTH);

        mainPanel.add(chartPanel, BorderLayout.CENTER);

        this.setContentPane(mainPanel);
        this.setVisible(true);
    }

    private void refreshButtonsEnabled() {
        addCharts.setEnabled(chartPanel.getNumCharts() < chartPanel.MAX_CHARTS);
        removeCharts.setEnabled(chartPanel.getNumCharts() > 1);
    }

    private JPanel createSpacer(int width, int height) {
        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(width, height));
        return spacer;
    }

    private void setConnectionState(boolean connected) {
        if(connected) {
            statusLight.setBackground(Color.GREEN);
            statusText.setText("Connected");
        } else {
            statusLight.setBackground(Color.RED);
            statusText.setText("Disconnected");
        }
    }
}
