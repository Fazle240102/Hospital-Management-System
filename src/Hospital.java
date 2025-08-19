import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import exception.ValidationException;
import model.Bed;
import model.Doctor;
import model.Patient;
import model.Ward;

/**
 * Core class for the Hospital Management System.
 * Manages Patients, Doctors, and a Ward (beds).
 * Ward capacity is 50; at most 50 concurrent admissions.
 */
public class Hospital {
    // JSON file paths for persistence
    private final String PATIENTS_PATH = "src/data/patients.json";
    private final String DOCTORS_PATH = "src/data/doctors.json";
    private final String WARDS_PATH = "src/data/wards.json";

    // In-memory storage
    private final ArrayList<Patient> patients;
    private final ArrayList<Doctor> doctors;
    private final Ward ward; // ward with fixed-size array of beds

    /**
     * Loads lists from JSON. DOES NOT modify data on startup.
     * - If wards.json is missing/empty: initialize 50 empty beds and save once.
     * - Otherwise: just load wards.json as-is (no normalization).
     */
    public Hospital() {
        Type pType = JsonHelper.listOf(Patient.class);
        Type dType = JsonHelper.listOf(Doctor.class);
        Type bType = JsonHelper.listOf(Bed.class);

        this.patients = new ArrayList<>(JsonHelper.loadList(PATIENTS_PATH, pType));
        this.doctors = new ArrayList<>(JsonHelper.loadList(DOCTORS_PATH, dType));

        // Load ward
        List<Bed> bedList = JsonHelper.loadList(WARDS_PATH, bType);
        this.ward = new Ward(50); // default capacity

        if (bedList != null && !bedList.isEmpty()) {
            // Trust file; do NOT change/save anything here
            this.ward.loadFrom(bedList);
        } else {
            // First run: create empty 50-bed ward and save once
            saveWards();
        }

        // IMPORTANT:
        // Do NOT auto-change patients or wards here (no normalization, no auto-assign).
        // Files should remain exactly as they were on disk when the app starts.
    }

    // -------------------- ID Generators --------------------

    /** Returns the next available patient ID (max+1). */
    private int nextPatientId() {
        int max = 0;
        for (Patient p : patients)
            if (p.getPatientId() > max)
                max = p.getPatientId();
        return max + 1;
    }

    /** Returns the next available doctor ID (max+1). */
    private int nextDoctorId() {
        int max = 0;
        for (Doctor d : doctors)
            if (d.getDoctorId() > max)
                max = d.getDoctorId();
        return max + 1;
    }

    // -------------------- Patients CRUD --------------------

    /** Add a new patient with auto-generated ID. */
    public Patient addPatientAuto(String name, int age, String gender, String contact, String bloodGroup) {
        validatePatientBasics(name, age);
        int id = nextPatientId();
        Patient p = new Patient(id, name, age, gender, contact, bloodGroup);
        patients.add(p);
        savePatients();
        return p;
    }

    /** Add a patient with manual ID (not used in UI). */
    public void addPatient(Patient p) {
        validatePatientBasics(p);
        if (getPatientById(p.getPatientId()) != null)
            throw new ValidationException("Patient ID already exists!");
        patients.add(p);
        savePatients();
    }

    /** Returns list of all patients. */
    public List<Patient> getPatients() {
        return patients;
    }

    /** Find patient by ID, or null if not found. */
    public Patient getPatientById(int id) {
        for (Patient p : patients)
            if (p.getPatientId() == id)
                return p;
        return null;
    }

    /** Update existing patient details. */
    public void updatePatient(Patient updated) {
        validatePatientBasics(updated);
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId() == updated.getPatientId()) {
                patients.set(i, updated);
                savePatients();
                return;
            }
        }
        throw new ValidationException("Patient not found (ID: " + updated.getPatientId() + ")");
    }

    /** Delete patient; if admitted, automatically frees their bed first. */
    public void deletePatient(int id) {
        Patient p = getPatientById(id);
        if (p == null)
            throw new ValidationException("Patient not found (ID: " + id + ")");

        if (p.isAdmitted()) {
            ward.freeBedByPatientId(id);
            p.setAdmitted(false);
            saveWards();
        }
        patients.remove(p);
        savePatients();
    }

    /**
     * Admit a patient to the first free bed and mark them as admitted.
     * 
     * @throws ValidationException if patient not found, already admitted, or ward
     *                             is full.
     */
    public void admitFirstFreeBed(int patientId) {
        Patient p = getPatientById(patientId);
        if (p == null)
            throw new ValidationException("Patient not found");

        if (p.isAdmitted())
            throw new ValidationException("Patient is already admitted");

        int bedNo = ward.assignBedToPatient(patientId);
        if (bedNo == -1)
            throw new ValidationException("No free bed available");

        p.setAdmitted(true);
        savePatients();
        saveWards(); // persist bed occupancy change
    }

    // -------------------- Doctors CRUD --------------------

    /** Add a new doctor with auto-generated ID. */
    public Doctor addDoctorAuto(String name, int age, String gender, String contact, String specialization) {
        validateDoctorBasics(name, age);
        int id = nextDoctorId();
        Doctor d = new Doctor(id, name, age, gender, contact, specialization);
        if (getDoctorById(id) != null)
            throw new ValidationException("Doctor ID already exists!"); // should not happen with auto ID
        doctors.add(d);
        saveDoctors();
        return d;
    }

    /** Add a doctor with manual ID (not used in UI). */
    public void addDoctor(Doctor d) {
        validateDoctorBasics(d);
        if (getDoctorById(d.getDoctorId()) != null)
            throw new ValidationException("Doctor ID already exists!");
        doctors.add(d);
        saveDoctors();
    }

    /** Returns list of all doctors. */
    public List<Doctor> getDoctors() {
        return doctors;
    }

    /** Find doctor by ID, or null if not found. */
    public Doctor getDoctorById(int id) {
        for (Doctor d : doctors)
            if (d.getDoctorId() == id)
                return d;
        return null;
    }

    /** Update existing doctor details. */
    public void updateDoctor(Doctor updated) {
        validateDoctorBasics(updated);
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getDoctorId() == updated.getDoctorId()) {
                doctors.set(i, updated);
                saveDoctors();
                return;
            }
        }
        throw new ValidationException("Doctor not found (ID: " + updated.getDoctorId() + ")");
    }

    /** Delete doctor by ID. */
    public void deleteDoctor(int id) {
        Doctor d = getDoctorById(id);
        if (d == null)
            throw new ValidationException("Doctor not found (ID: " + id + ")");
        doctors.remove(d);
        saveDoctors();
    }

    // -------------------- Validators --------------------

    /** Validate basic patient details. */
    private void validatePatientBasics(Patient p) {
        validatePatientBasics(p.getName(), p.getAge());
        if (p.getPatientId() <= 0)
            throw new ValidationException("Patient ID must be positive");
    }

    private void validatePatientBasics(String name, int age) {
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Patient name is required");
        if (age < 0)
            throw new ValidationException("Patient age cannot be negative");
    }

    /** Validate basic doctor details. */
    private void validateDoctorBasics(Doctor d) {
        validateDoctorBasics(d.getName(), d.getAge());
        if (d.getDoctorId() <= 0)
            throw new ValidationException("Doctor ID must be positive");
    }

    private void validateDoctorBasics(String name, int age) {
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Doctor name is required");
        if (age < 0)
            throw new ValidationException("Doctor age cannot be negative");
    }

    // -------------------- Save Helpers --------------------

    /** Save patients list to JSON file. */
    private void savePatients() {
        JsonHelper.saveList(PATIENTS_PATH, patients);
    }

    /** Save doctors list to JSON file. */
    private void saveDoctors() {
        JsonHelper.saveList(DOCTORS_PATH, doctors);
    }

    /** Save ward (beds) to JSON file. */
    private void saveWards() {
        JsonHelper.saveList(WARDS_PATH, ward.toList());
    }

    /** Returns the ward object (contains beds). */
    public Ward getWard() {
        return ward;
    }
}
