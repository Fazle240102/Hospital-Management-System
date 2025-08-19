package model;

import java.util.Arrays;
import java.util.List;

/**
 * Ward keeps a fixed-size array of beds and provides booking operations.
 * Capacity is fixed at 50 beds for this hospital system.
 */
public class Ward {
    private Bed[] beds;

    /** Creates a ward with 50 beds by default. */
    public Ward() {
        this(50);
    }

    /** Creates a ward with the given number of beds (default 50). */
    public Ward(int bedCount) {
        beds = new Bed[bedCount];
        for (int i = 0; i < bedCount; i++) {
            beds[i] = new Bed(i + 1);
        }
    }

    /** Load beds from an existing list (used for JSON load). */
    public void loadFrom(List<Bed> items) {
        if (items == null || items.isEmpty())
            return;
        beds = items.toArray(new Bed[0]);
    }

    /** Convert current beds to a List (used for JSON save). */
    public List<Bed> toList() {
        return Arrays.asList(beds);
    }

    /** Returns index of the first free bed, or -1 if none. */
    public int findFirstFreeBedIndex() {
        for (int i = 0; i < beds.length; i++) {
            if (!beds[i].isOccupied())
                return i;
        }
        return -1;
    }

    /**
     * Assigns first free bed to a patient.
     * Returns bed number (1-based) or -1 if ward is full.
     */
    public int assignBedToPatient(int patientId) {
        int idx = findFirstFreeBedIndex();
        if (idx == -1)
            return -1;
        beds[idx].occupyFor(patientId);
        return beds[idx].getNumber();
    }

    /** Free a bed by bed number (1-based). */
    public void freeBed(int bedNumber) {
        if (bedNumber >= 1 && bedNumber <= beds.length) {
            beds[bedNumber - 1].free();
        }
    }

    /**
     * Free whatever bed the patient is using.
     * Returns true if something was freed.
     */
    public boolean freeBedByPatientId(int patientId) {
        for (Bed b : beds) {
            Integer pid = b.getPatientId();
            if (pid != null && pid == patientId) {
                b.free();
                return true;
            }
        }
        return false;
    }

    /** Find the bed number assigned to a patient; -1 if none. */
    public int getBedNumberForPatient(int patientId) {
        for (Bed b : beds) {
            Integer pid = b.getPatientId();
            if (pid != null && pid == patientId)
                return b.getNumber();
        }
        return -1;
    }

    /** Returns the array of beds. */
    public Bed[] getBeds() {
        return beds;
    }

    /** Returns total beds in the ward (fixed 50). */
    public int getTotalBeds() {
        return beds.length;
    }
}