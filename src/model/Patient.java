package model;

/**
 * Represents a patient in the hospital.
 * Inherits common fields from Person.
 */
public class Patient extends Person {
    private int patientId; // Unique ID for the patient
    private String bloodGroup; // Patient's blood group
    private boolean admitted; // True if patient is admitted in hospital

    /** Default constructor. */
    public Patient() {
    }

    /** Creates a patient with given details. */
    public Patient(int patientId, String name, int age, String gender, String contact, String bloodGroup) {
        super(name, age, gender, contact);
        this.patientId = patientId;
        this.bloodGroup = bloodGroup;
        this.admitted = false; // initially not admitted
    }

    /** Returns the patient ID. */
    public int getPatientId() {
        return patientId;
    }

    /** Returns the blood group. */
    public String getBloodGroup() {
        return bloodGroup;
    }

    /** Returns true if admitted. */
    public boolean isAdmitted() {
        return admitted;
    }

    /** Sets the blood group. */
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    /** Sets admitted status. */
    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }
}