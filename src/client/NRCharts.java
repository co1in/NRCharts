package client;

import javax.swing.*;

public class NRCharts {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Nashoba Robotics Charts");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        javax.swing.SwingUtilities.invokeLater(NRFrame::new);
    }
}
