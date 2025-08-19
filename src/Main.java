import javax.swing.SwingUtilities;

/**
 * Entry point of the Hospital Management System.
 * Launches the Swing GUI (AppFrame) on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Always start Swing GUI on Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}