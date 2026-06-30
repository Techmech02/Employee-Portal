package com.employee.model;

import java.io.Serializable;

public class CustomField implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int employeeId;
    private String fieldLabel;
    private String fieldValue;

    public CustomField() {
    }

    public CustomField(String fieldLabel, String fieldValue) {
        this.fieldLabel = fieldLabel;
        this.fieldValue = fieldValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
