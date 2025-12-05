import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MediLinkApp extends JFrame {
    private Connection conn;

    // Theme colors and fonts
    private final Color PRIMARY_COLOR = new Color(30, 144, 255);
    private final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public MediLinkApp() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        connectToDatabase();
        createLoginUI();
    }

    // -------------------- DATABASE CONNECTION --------------------
    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/medilink_schema",
                    "Kartikey", // your MySQL username
                    "Kartikey@1234" // your MySQL password
            );
            System.out.println("✅ Database connected successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Database Connection Error: " + e.getMessage(),
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // -------------------- MAIN LOGIN SCREEN --------------------
    private void createLoginUI() {
        setTitle("MediLink - Welcome");
        setSize(450, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Welcome to MediLink. Who are you?", SwingConstants.CENTER);
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(PRIMARY_COLOR.darker());
        panel.add(headerLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton patientBtn = createStyledButton("Patient ");
        JButton doctorBtn = createStyledButton("Doctor ");

        buttonPanel.add(patientBtn);
        buttonPanel.add(doctorBtn);
        panel.add(buttonPanel, BorderLayout.CENTER);

        patientBtn.addActionListener(e -> {
            setVisible(false);
            openPatientUI();
        });

        doctorBtn.addActionListener(e -> {
            setVisible(false);
            openDoctorUI();
        });

        add(panel);
        setVisible(true);
    }

    // -------------------- PATIENT DASHBOARD --------------------
    private void openPatientUI() {
        JFrame frame = new JFrame("Patient Dashboard");
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton bookBtn = createStyledButton(" Book Appointment");
        JButton viewBtn = createStyledButton(" View My Appointments & Prescriptions");
        JButton backBtn = createStyledButton(" Back");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.add(backBtn);
        btnPanel.add(bookBtn);
        btnPanel.add(viewBtn);

        JLabel title = new JLabel("Welcome, Patient", SwingConstants.CENTER);
        title.setFont(HEADER_FONT);
        title.setForeground(PRIMARY_COLOR);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(btnPanel, BorderLayout.CENTER);

        backBtn.addActionListener(e -> {
            frame.dispose();
            setVisible(true);
        });
        bookBtn.addActionListener(e -> openBookingUI(frame));
        viewBtn.addActionListener(e -> openPatientStatusUI(frame));

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void openBookingUI(JFrame parentFrame) {
        JFrame frame = new JFrame("Book Appointment");
        frame.setSize(500, 350);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        JTextField doctorField = new JTextField();
        JTextField dateField = new JTextField();

        formPanel.add(new JLabel("Patient Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Doctor Name:"));
        formPanel.add(doctorField);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);

        JButton bookBtn = createStyledButton("Book Appointment Now");
        JButton backBtn = createStyledButton("⬅ Back");

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(backBtn);
        bottomPanel.add(bookBtn);

        bookBtn.addActionListener(ae -> bookAppointment(frame, nameField.getText(), doctorField.getText(), dateField.getText()));
        backBtn.addActionListener(ae -> frame.dispose());

        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void openPatientStatusUI(JFrame parentFrame) {
        JFrame frame = new JFrame("My Appointments & Prescriptions");
        frame.setSize(650, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(area);
        JButton refreshBtn = createStyledButton("🔄 Refresh");
        JButton backBtn = createStyledButton("⬅ Back");

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(backBtn);
        bottomPanel.add(refreshBtn);

        refreshBtn.addActionListener(e -> showPatientAppointments(area));
        backBtn.addActionListener(e -> frame.dispose());

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        showPatientAppointments(area);
    }

    private void showPatientAppointments(JTextArea area) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM appointments ORDER BY date ASC")) {

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s | %-15s | %-15s | %-10s | %s%n", "ID", "Patient", "Doctor", "Date", "Prescription"));
            sb.append("----------------------------------------------------------------------------\n");

            while (rs.next()) {
                String appointmentId = rs.getString("id");
                String prescription = getPrescriptionByAppointment(appointmentId);
                sb.append(String.format("%-5s | %-15s | %-15s | %-10s | %s%n",
                        appointmentId, rs.getString("patient_id"), rs.getString("doctor_id"),
                        rs.getString("date"), prescription == null ? "Pending" : "✔ Given"));
            }

            area.setText(sb.toString());
        } catch (Exception ex) {
            area.setText("Error loading appointments: " + ex.getMessage());
        }
    }

    private String getPrescriptionByAppointment(String appointmentId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT details FROM prescriptions WHERE appointment_id=?")) {
            ps.setString(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("details");
        } catch (Exception ignored) {}
        return null;
    }

    private void bookAppointment(JFrame parentFrame, String name, String doctor, String date) {
        if (name.trim().isEmpty() || doctor.trim().isEmpty() || date.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO appointments (id, patient_id, doctor_id, date) VALUES (?, ?, ?, ?)");
            ps.setInt(1, (int) (Math.random() * 10000));
            ps.setString(2, name);
            ps.setString(3, doctor);
            ps.setString(4, date);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(parentFrame, "✅ Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "❌ Database Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- DOCTOR DASHBOARD --------------------
    private void openDoctorUI() {
        JFrame frame = new JFrame("Doctor Dashboard");
        frame.setSize(650, 450);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(area);
        JButton refreshBtn = createStyledButton(" Refresh List");
        JButton prescribeBtn = createStyledButton(" Generate Prescription");
        JButton backBtn = createStyledButton("⬅ Back");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(backBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(prescribeBtn);

        refreshBtn.addActionListener(e -> showAppointments(area));
        prescribeBtn.addActionListener(e -> openPrescriptionUI());
        backBtn.addActionListener(e -> {
            frame.dispose();
            setVisible(true);
        });

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        showAppointments(area);
    }

    private void showAppointments(JTextArea area) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM appointments ORDER BY date ASC")) {

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s | %-15s | %-15s | %s%n", "ID", "Patient", "Doctor", "Date"));
            sb.append("-------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-5s | %-15s | %-15s | %s%n",
                        rs.getString("id"), rs.getString("patient_id"),
                        rs.getString("doctor_id"), rs.getString("date")));
            }

            area.setText(sb.toString());
        } catch (Exception ex) {
            area.setText("Error loading appointments: " + ex.getMessage());
        }
    }

    // -------------------- PRESCRIPTION UI --------------------
    private void openPrescriptionUI() {
        JFrame frame = new JFrame("Generate Prescription");
        frame.setSize(450, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField appointmentIdField = new JTextField();
        JTextArea detailsArea = new JTextArea(5, 20);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JButton saveBtn = createStyledButton("💾 Save Prescription");
        JButton backBtn = createStyledButton("⬅ Back");

        panel.add(new JLabel("Appointment ID:"));
        panel.add(appointmentIdField);
        panel.add(new JLabel("Prescription Details:"));
        panel.add(new JScrollPane(detailsArea));
        panel.add(backBtn);
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> savePrescription(frame, appointmentIdField.getText(), detailsArea.getText()));
        backBtn.addActionListener(e -> frame.dispose());

        frame.add(panel);
        frame.setVisible(true);
    }

    private void savePrescription(JFrame frame, String appointmentId, String details) {
        if (appointmentId.isEmpty() || details.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO prescriptions (id, appointment_id, details) VALUES (?, ?, ?)");
            ps.setInt(1, (int) (Math.random() * 10000));
            ps.setString(2, appointmentId);
            ps.setString(3, details);
            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(frame, "✅ Prescription saved successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "❌ Error: " + ex.getMessage());
        }
    }

    // -------------------- UTILITIES --------------------
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(PRIMARY_COLOR.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(PRIMARY_COLOR); }
        });
        return button;
    }

    // -------------------- MAIN --------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MediLinkApp::new);
    }
}
