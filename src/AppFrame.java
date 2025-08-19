import exception.ValidationException;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Main GUI window for Hospital Management System.
 * Uses Swing components with tabs for Patients and Doctors.
 * Supports CRUD operations and bed admission.
 */
public class AppFrame extends JFrame {
    private final Hospital hospital = new Hospital(); // main hospital data handler

    // ---------------- Patients Section ----------------
    // Table model for patients (columns fixed, not editable)
    private final DefaultTableModel patientModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Age", "Gender", "Blood Group", "Admitted" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false; // prevent direct editing in the table
        }
    };
    private JTable patientTable;
    private JTextField pIdView, pName, pAge, pGender, pBG;

    // ---------------- Doctors Section ----------------
    // Table model for doctors (columns fixed, not editable)
    private final DefaultTableModel doctorModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Age", "Gender", "Specialization" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JTable doctorTable;
    private JTextField dIdView, dName, dAge, dGender, dSpec;

    /** Constructor: builds frame with tabs for patients and doctors. */
    public AppFrame() {
        setTitle("Hospital Management System");
        setSize(900, 610);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center the window

        // Create tabs for patient and doctor management
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Patients", makePatientPanel());
        tabs.add("Doctors", makeDoctorPanel());
        add(tabs);

        // Fill initial data into tables
        refreshPatientsTable();
        refreshDoctorsTable();
    }

    // ================= PATIENTS PANEL =================
    private JPanel makePatientPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));

        // ---------- Form fields (ID is read-only) ----------
        JPanel form = new JPanel(new GridLayout(2, 10, 6, 6));
        pIdView = new JTextField();
        pIdView.setEditable(false);
        pIdView.setBackground(new Color(245, 245, 245)); // gray background for read-only
        pName = new JTextField();
        pAge = new JTextField();
        pGender = new JTextField();
        pBG = new JTextField();

        form.add(new JLabel("ID"));
        form.add(pIdView);
        form.add(new JLabel("Name"));
        form.add(pName);
        form.add(new JLabel("Age"));
        form.add(pAge);
        form.add(new JLabel("Gender"));
        form.add(pGender);
        form.add(new JLabel("Blood Group"));
        form.add(pBG);

        // ---------- Action buttons ----------
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton admitBtn = new JButton("Admit");

        JPanel actions = new JPanel();
        actions.add(addBtn);
        actions.add(updateBtn);
        actions.add(deleteBtn);
        actions.add(clearBtn);
        actions.add(admitBtn);

        // ---------- Table ----------
        patientTable = new JTable(patientModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.getSelectionModel().addListSelectionListener(this::onPatientRowSelect);

        // Layout assembly
        root.add(form, BorderLayout.NORTH);
        root.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        // ---------- Button handlers ----------
        // Add new patient with auto-generated ID
        addBtn.addActionListener(e -> {
            try {
                Patient p = hospital.addPatientAuto(
                        pName.getText().trim(),
                        parseInt(pAge.getText()),
                        pGender.getText().trim(),
                        "N/A",
                        pBG.getText().trim());
                refreshPatientsTable();
                JOptionPane.showMessageDialog(this, "Patient added! ID = " + p.getPatientId());
                clearPatientForm();
            } catch (ValidationException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Update selected patient
        updateBtn.addActionListener(e -> {
            try {
                int sel = patientTable.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(this, "Select a patient row first");
                    return;
                }
                int id = (int) patientModel.getValueAt(sel, 0);
                Patient p = new Patient(
                        id,
                        pName.getText().trim(),
                        parseInt(pAge.getText()),
                        pGender.getText().trim(),
                        "N/A",
                        pBG.getText().trim());
                // Preserve admitted status
                Patient current = hospital.getPatientById(id);
                if (current != null)
                    p.setAdmitted(current.isAdmitted());

                hospital.updatePatient(p);
                refreshPatientsTable();
                JOptionPane.showMessageDialog(this, "Patient updated!");
            } catch (ValidationException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Delete selected patient
        deleteBtn.addActionListener(e -> {
            try {
                int sel = patientTable.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(this, "Select a patient row first");
                    return;
                }
                int id = (int) patientModel.getValueAt(sel, 0);
                hospital.deletePatient(id);
                refreshPatientsTable();
                JOptionPane.showMessageDialog(this, "Patient deleted!");
                clearPatientForm();
            } catch (ValidationException ex1) {
                JOptionPane.showMessageDialog(this, ex1.getMessage());
            }
        });

        // Clear form fields
        clearBtn.addActionListener(e -> clearPatientForm());

        // Admit patient into first free bed
        // Admit patient into first free bed
        admitBtn.addActionListener(e -> {
            String s = JOptionPane.showInputDialog(this, "Enter Patient ID to admit:");
            if (s == null)
                return;
            try {
                hospital.admitFirstFreeBed(parseInt(s)); // throws if full / invalid
                JOptionPane.showMessageDialog(this, "Admitted!");
                refreshPatientsTable();
            } catch (ValidationException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage()); // "No free bed available" etc.
            }
        });

        return root;
    }

    /** When a patient row is selected, load data into form fields. */
    private void onPatientRowSelect(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int r = patientTable.getSelectedRow();
        if (r < 0)
            return;
        pIdView.setText(String.valueOf(patientModel.getValueAt(r, 0)));
        pName.setText(String.valueOf(patientModel.getValueAt(r, 1)));
        pAge.setText(String.valueOf(patientModel.getValueAt(r, 2)));
        pGender.setText(String.valueOf(patientModel.getValueAt(r, 3)));
        pBG.setText(String.valueOf(patientModel.getValueAt(r, 4)));
    }

    /** Refresh patient table with current data. */
    private void refreshPatientsTable() {
        patientModel.setRowCount(0);
        hospital.getPatients().forEach(pt -> patientModel.addRow(new Object[] {
                pt.getPatientId(), pt.getName(), pt.getAge(), pt.getGender(),
                pt.getBloodGroup(), pt.isAdmitted()
        }));
    }

    /** Clear patient form fields. */
    private void clearPatientForm() {
        pIdView.setText("");
        pName.setText("");
        pAge.setText("");
        pGender.setText("");
        pBG.setText("");
        patientTable.clearSelection();
    }

    // ================= DOCTORS PANEL =================
    private JPanel makeDoctorPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));

        // ---------- Form fields ----------
        JPanel form = new JPanel(new GridLayout(2, 10, 6, 6));
        dIdView = new JTextField();
        dIdView.setEditable(false);
        dIdView.setBackground(new Color(245, 245, 245));
        dName = new JTextField();
        dAge = new JTextField();
        dGender = new JTextField();
        dSpec = new JTextField();

        form.add(new JLabel("ID"));
        form.add(dIdView);
        form.add(new JLabel("Name"));
        form.add(dName);
        form.add(new JLabel("Age"));
        form.add(dAge);
        form.add(new JLabel("Gender"));
        form.add(dGender);
        form.add(new JLabel("Specialization"));
        form.add(dSpec);

        // ---------- Action buttons ----------
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        JPanel actions = new JPanel();
        actions.add(addBtn);
        actions.add(updateBtn);
        actions.add(deleteBtn);
        actions.add(clearBtn);

        // ---------- Table ----------
        doctorTable = new JTable(doctorModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.getSelectionModel().addListSelectionListener(this::onDoctorRowSelect);

        root.add(form, BorderLayout.NORTH);
        root.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        // ---------- Button handlers ----------
        // Add doctor
        addBtn.addActionListener(e -> {
            try {
                Doctor d = hospital.addDoctorAuto(
                        dName.getText().trim(),
                        parseInt(dAge.getText()),
                        dGender.getText().trim(),
                        "N/A",
                        dSpec.getText().trim());
                refreshDoctorsTable();
                JOptionPane.showMessageDialog(this, "Doctor added! ID = " + d.getDoctorId());
                clearDoctorForm();
            } catch (ValidationException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Update doctor
        updateBtn.addActionListener(e -> {
            try {
                int sel = doctorTable.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(this, "Select a doctor row first");
                    return;
                }
                int id = (int) doctorModel.getValueAt(sel, 0);
                Doctor d = new Doctor(
                        id,
                        dName.getText().trim(),
                        parseInt(dAge.getText()),
                        dGender.getText().trim(),
                        "N/A",
                        dSpec.getText().trim());
                hospital.updateDoctor(d);
                refreshDoctorsTable();
                JOptionPane.showMessageDialog(this, "Doctor updated!");
            } catch (ValidationException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Delete doctor
        deleteBtn.addActionListener(e -> {
            try {
                int sel = doctorTable.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(this, "Select a doctor row first");
                    return;
                }
                int id = (int) doctorModel.getValueAt(sel, 0);
                hospital.deleteDoctor(id);
                refreshDoctorsTable();
                JOptionPane.showMessageDialog(this, "Doctor deleted!");
                clearDoctorForm();
            } catch (ValidationException ex1) {
                JOptionPane.showMessageDialog(this, ex1.getMessage());
            }
        });

        // Clear form
        clearBtn.addActionListener(e -> clearDoctorForm());

        return root;
    }

    /** When a doctor row is selected, load data into form fields. */
    private void onDoctorRowSelect(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int r = doctorTable.getSelectedRow();
        if (r < 0)
            return;
        dIdView.setText(String.valueOf(doctorModel.getValueAt(r, 0)));
        dName.setText(String.valueOf(doctorModel.getValueAt(r, 1)));
        dAge.setText(String.valueOf(doctorModel.getValueAt(r, 2)));
        dGender.setText(String.valueOf(doctorModel.getValueAt(r, 3)));
        dSpec.setText(String.valueOf(doctorModel.getValueAt(r, 4)));
    }

    /** Refresh doctor table with current data. */
    private void refreshDoctorsTable() {
        doctorModel.setRowCount(0);
        hospital.getDoctors().forEach(d -> doctorModel.addRow(new Object[] {
                d.getDoctorId(), d.getName(), d.getAge(), d.getGender(), d.getSpecialization()
        }));
    }

    /** Clear doctor form fields. */
    private void clearDoctorForm() {
        dIdView.setText("");
        dName.setText("");
        dAge.setText("");
        dGender.setText("");
        dSpec.setText("");
        doctorTable.clearSelection();
    }

    // ================= UTILS =================
    /** Utility: parse integer safely (with trim). */
    private int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }
}