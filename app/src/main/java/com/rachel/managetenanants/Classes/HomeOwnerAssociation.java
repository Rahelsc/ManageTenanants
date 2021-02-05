package com.rachel.managetenanants.Classes;

public class HomeOwnerAssociation extends Person{

    private int seniority;

    public int getSeniority() {
        return seniority;
    }

    public void setSeniority(int seniority) {
        this.seniority = seniority;
    }

    public HomeOwnerAssociation(int seniority) {
        this.seniority = seniority;
    }

    public HomeOwnerAssociation(String firstName, String lastName, String id, int seniority) {
        super(firstName, lastName, id);
        this.seniority = seniority;
    }

    public HomeOwnerAssociation(){}
}
