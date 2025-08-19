package model;

/**
 * Represents a doctor in the hospital.
 * Inherits common fields from Person.
 */
public class Doctor extends Person {
    private int doctorId; // Unique ID for the doctor
    private String specialization; // Doctor's area of expertise

    /** Default constructor. */
    public Doctor() {
    }

    /** Creates a doctor with given details. */
    public Doctor(int doctorId, String name, int age, String gender, String contact, String specialization) {
        super(name, age, gender, contact);
        this.doctorId = doctorId;
        this.specialization = specialization;
    }

    /** Returns the doctor ID. */
    public int getDoctorId() {
        return doctorId;
    }

    /** Returns the specialization. */
    public String getSpecialization() {
        return specialization;
    }

    /** Sets the specialization. */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}