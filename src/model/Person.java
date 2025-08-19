package model;

/**
 * Abstract base class for people in the hospital system.
 * Demonstrates Encapsulation, Abstraction, and Inheritance.
 */
public abstract class Person {
    private String name; // Person's name
    private int age; // Person's age
    private String gender; // Person's gender
    private String contact; // Contact information

    /** Default constructor. */
    public Person() {
    }

    /** Creates a person with given details. */
    public Person(String name, int age, String gender, String contact) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
    }

    // Getters and setters (Encapsulation)
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getContact() {
        return contact;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}