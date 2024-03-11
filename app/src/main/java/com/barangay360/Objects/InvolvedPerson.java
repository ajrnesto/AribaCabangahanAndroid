package com.barangay360.Objects;

import android.net.Uri;

public class InvolvedPerson {
    String fullName;
    String fullAddress;
    String involvement;

    public InvolvedPerson() {
    }

    public InvolvedPerson(String fullName, String fullAddress, String involvement) {
        this.fullName = fullName;
        this.fullAddress = fullAddress;
        this.involvement = involvement;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getInvolvement() {
        return involvement;
    }

    public void setInvolvement(String involvement) {
        this.involvement = involvement;
    }
}
