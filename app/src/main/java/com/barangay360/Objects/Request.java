package com.barangay360.Objects;

public class Request {
    String uid;
    String userUid;
    String firstName;
    String middleName;
    String lastName;
    long birthdate;
    String civilStatus;
    String addressPurok;
    String residencyStatus;
    String businessName;
    long timestamp;
    String status;
    String documentType;
    String purpose;
    String letterOfRequest;

    public Request() {
    }

    public Request(String uid, String userUid, String firstName, String middleName, String lastName, long birthdate, String civilStatus, String addressPurok, String residencyStatus, String businessName, long timestamp, String status, String documentType, String purpose, String letterOfRequest) {
        this.uid = uid;
        this.userUid = userUid;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.civilStatus = civilStatus;
        this.addressPurok = addressPurok;
        this.residencyStatus = residencyStatus;
        this.businessName = businessName;
        this.timestamp = timestamp;
        this.status = status;
        this.documentType = documentType;
        this.purpose = purpose;
        this.letterOfRequest = letterOfRequest;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public String getAddressPurok() {
        return addressPurok;
    }

    public void setAddressPurok(String addressPurok) {
        this.addressPurok = addressPurok;
    }

    public String getResidencyStatus() {
        return residencyStatus;
    }

    public void setResidencyStatus(String residencyStatus) {
        this.residencyStatus = residencyStatus;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getLetterOfRequest() {
        return letterOfRequest;
    }

    public void setLetterOfRequest(String letterOfRequest) {
        this.letterOfRequest = letterOfRequest;
    }
}