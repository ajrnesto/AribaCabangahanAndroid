package com.barangay360.Objects;

import java.util.ArrayList;

public class Incident {
    String uid;
    String userUid;
    String incidentType;
    String locationPurok;
    ArrayList<InvolvedPerson> involvedPersons;
    String incidentDetails;
    ArrayList<String> mediaFileNames;
    long incidentDate;
    long timestamp;
    String status;

    public Incident() {
    }

    public Incident(String uid, String userUid, String incidentType, String locationPurok, ArrayList<InvolvedPerson> involvedPersons, String incidentDetails, ArrayList<String> mediaFileNames, long incidentDate, long timestamp, String status) {
        this.uid = uid;
        this.userUid = userUid;
        this.incidentType = incidentType;
        this.locationPurok = locationPurok;
        this.involvedPersons = involvedPersons;
        this.incidentDetails = incidentDetails;
        this.mediaFileNames = mediaFileNames;
        this.incidentDate = incidentDate;
        this.timestamp = timestamp;
        this.status = status;
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

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getLocationPurok() {
        return locationPurok;
    }

    public void setLocationPurok(String locationPurok) {
        this.locationPurok = locationPurok;
    }

    public ArrayList<InvolvedPerson> getInvolvedPersons() {
        return involvedPersons;
    }

    public void setInvolvedPersons(ArrayList<InvolvedPerson> involvedPersons) {
        this.involvedPersons = involvedPersons;
    }

    public String getIncidentDetails() {
        return incidentDetails;
    }

    public void setIncidentDetails(String incidentDetails) {
        this.incidentDetails = incidentDetails;
    }

    public ArrayList<String> getMediaFileNames() {
        return mediaFileNames;
    }

    public void setMediaFileNames(ArrayList<String> mediaFileNames) {
        this.mediaFileNames = mediaFileNames;
    }

    public long getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(long incidentDate) {
        this.incidentDate = incidentDate;
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
}
