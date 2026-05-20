import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class CustomConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    public CustomConfirmationDialog(Frame parent, String title, String message, String detailMessage) {
        super(parent, title, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 280);
        setResizable(false);
        setLocationRelativeTo(parent);

        // Main content panel with gradient background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color centerColor = new Color(30, 58, 138); // lighter navy
                Color edgeColor = new Color(15, 23, 42); // very dark navy

                Point2D center = new Point2D.Float(w / 2f, h / 2f);
                float radius = (float) Math.max(w, h);
                float[] dist = {0.0f, 1.0f};
                Color[] colors = {centerColor, edgeColor};
                RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);

                g2.setPaint(p);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };

        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mainLabel = new JLabel(message);
        mainLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainLabel.setForeground(Color.WHITE);
        mainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messagePanel.add(mainLabel);

        if (detailMessage != null && !detailMessage.isEmpty()) {
            messagePanel.add(Box.createRigidArea(new Dimension(0, 15)));
            JLabel detailLabel = new JLabel(detailMessage);
            detailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            detailLabel.setForeground(new Color(200, 200, 200));
            detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(detailLabel);
        }

        contentPanel.add(messagePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GlowingButton yesButton = new GlowingButton("Yes", new Color(34, 197, 94)); // Green
        yesButton.setPreferredSize(new Dimension(120, 50));
        yesButton.setMaximumSize(new Dimension(120, 50));
        yesButton.setMinimumSize(new Dimension(120, 50));
        yesButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        GlowingButton noButton = new GlowingButton("No", new Color(239, 68, 68)); // Red
        noButton.setPreferredSize(new Dimension(120, 50));
        noButton.setMaximumSize(new Dimension(120, 50));
        noButton.setMinimumSize(new Dimension(120, 50));
        noButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(yesButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(noButton);
        buttonPanel.add(Box.createHorizontalGlue());

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
}
