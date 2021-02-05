package com.rachel.managetenanants.Classes;

public class Tenant extends Person {

    private int apartmentNumber;

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(int apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public Tenant() {
    }

    public Tenant(String firstName, String lastName, String id, int apartmentNumber) {
        super(firstName, lastName, id);
        this.apartmentNumber = apartmentNumber;
    }

    @Override
    public String toString() {
        return super.toString()+"\nTenant{" +
                "apartmentNumber=" + apartmentNumber +
                '}';
    }
}

