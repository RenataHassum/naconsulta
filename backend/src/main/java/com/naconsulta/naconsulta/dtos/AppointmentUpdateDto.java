package com.naconsulta.naconsulta.dtos;

import com.naconsulta.naconsulta.entities.Appointment;

import java.io.Serializable;
import java.time.Instant;

public class AppointmentUpdateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String diagnosis;
    private String symptom;


    public AppointmentUpdateDto() {
    }

    public AppointmentUpdateDto(String diagnosis, String symptom) {
        this.diagnosis = diagnosis;
        this.symptom = symptom;
    }

    public AppointmentUpdateDto(Appointment entity) {
        diagnosis = entity.getDiagnosis();
        symptom = entity.getSymptom();
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }
}
