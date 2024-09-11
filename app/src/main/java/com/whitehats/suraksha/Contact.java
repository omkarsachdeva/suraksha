package com.whitehats.suraksha;

public class Contact {

    private String id;
    private String name;
    private String phoneNumber;

    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public Contact(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
