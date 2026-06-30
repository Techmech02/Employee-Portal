package com.employee.bean;

import com.employee.dao.EmployeeDAO;
import com.employee.model.CustomField;
import com.employee.model.Employee;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@ManagedBean(name = "detailBean")
@ViewScoped
public class EmployeeDetailBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Employee employee;
    private boolean editMode;
    private String newFieldLabel;
    private String newFieldValue;
    private transient Part newImage;

    private final EmployeeDAO dao = new EmployeeDAO();

    public void toggleEdit() {
        editMode = !editMode;
    }

    public void saveEmployee() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ensureEmployeeLoaded(ctx)) {
            return;
        }

        if (newImage != null && newImage.getSize() > 0) {
            String savedPath = saveImage(newImage, ctx);
            if (savedPath != null) {
                employee.setImagePath(savedPath);
            }
        }

        if (dao.updateEmployee(employee)) {
            editMode = false;
            newImage = null;
            employee = dao.getEmployeeById(employee.getId());
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Saved", "Employee information updated successfully."));
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Could not save changes. Please try again."));
        }
    }

    public void saveCustomField(CustomField customField) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (customField == null) {
            return;
        }

        if (dao.updateCustomField(customField)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Saved", "Field '" + customField.getFieldLabel() + "' updated."));
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Could not update field."));
        }
    }

    public void deleteCustomField(int customFieldId) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (dao.deleteCustomField(customFieldId) && employee != null) {
            employee.getCustomFields().removeIf(field -> field.getId() == customFieldId);
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Deleted", "Custom field removed."));
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Could not delete field."));
        }
    }

    public void addCustomField() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ensureEmployeeLoaded(ctx)) {
            return;
        }

        if (newFieldLabel == null || newFieldLabel.trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Warning", "Field label cannot be empty."));
            return;
        }

        CustomField customField = new CustomField(
            newFieldLabel.trim(),
            newFieldValue == null ? "" : newFieldValue.trim()
        );
        customField.setEmployeeId(employee.getId());

        if (dao.addCustomField(customField)) {
            employee = dao.getEmployeeById(employee.getId());
            newFieldLabel = null;
            newFieldValue = null;
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Added", "New field added successfully."));
        } else {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Could not add field."));
        }
    }

    public String goBack() {
        return "employees?faces-redirect=true";
    }

    public Employee getEmployee() {
        ensureEmployeeLoaded(FacesContext.getCurrentInstance());
        return employee;
    }

    private boolean ensureEmployeeLoaded(FacesContext ctx) {
        if (employee != null) {
            return true;
        }
        Integer employeeId = resolveEmployeeId(ctx);
        if (employeeId == null) {
            return false;
        }
        employee = dao.getEmployeeById(employeeId);
        return employee != null;
    }

    private Integer resolveEmployeeId(FacesContext ctx) {
        String idParam = ctx.getExternalContext().getRequestParameterMap().get("empId");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                return Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        Object sessionId = ctx.getExternalContext().getSessionMap().get("loggedInEmployeeId");
        if (sessionId instanceof Integer) {
            return (Integer) sessionId;
        }
        return null;
    }

    private String saveImage(Part part, FacesContext ctx) {
        String fileName = System.currentTimeMillis() + "_" + getSubmittedFileName(part)
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        String uploadDir = request.getServletContext().getRealPath("/resources/uploads");
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, Paths.get(uploadDir, fileName), StandardCopyOption.REPLACE_EXISTING);
            return "resources/uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "upload";
        }
        for (String token : contentDisposition.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String fileName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                return Paths.get(fileName).getFileName().toString();
            }
        }
        return "upload";
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getNewFieldLabel() {
        return newFieldLabel;
    }

    public void setNewFieldLabel(String newFieldLabel) {
        this.newFieldLabel = newFieldLabel;
    }

    public String getNewFieldValue() {
        return newFieldValue;
    }

    public void setNewFieldValue(String newFieldValue) {
        this.newFieldValue = newFieldValue;
    }

    public Part getNewImage() {
        return newImage;
    }

    public void setNewImage(Part newImage) {
        this.newImage = newImage;
    }
}
