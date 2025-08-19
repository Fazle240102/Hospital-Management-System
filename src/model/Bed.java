package model;

/** Represents a hospital bed with number, occupancy, and assigned patient. */
public class Bed {
    private int number; // 1-based bed number
    private boolean occupied; // true if occupied
    private Integer patientId; // which patient uses this bed (null if free)

    public Bed() {
    }

    public Bed(int number) {
        this.number = number;
        this.occupied = false;
        this.patientId = null;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isOccupied() {
        return occupied;
    }

    /** Returns the patientId using this bed (null if free). */
    public Integer getPatientId() {
        return patientId;
    }

    /** Occupy this bed for a specific patient. */
    public void occupyFor(int patientId) {
        this.occupied = true;
        this.patientId = patientId;
    }

    /** Free this bed. */
    public void free() {
        this.occupied = false;
        this.patientId = null;
    }

    /** Kept for compatibility. Prefer occupyFor()/free(). */
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
        if (!occupied)
            this.patientId = null;
    }
}