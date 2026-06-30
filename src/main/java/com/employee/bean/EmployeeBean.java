package com.employee.bean;

import com.employee.dao.EmployeeDAO;
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
import java.util.List;

@ManagedBean(name = "employeeBean")
@ViewScoped
public class EmployeeBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String name;
    private String fathersName;
    private String email;
    private String gender;
    private String contact;
    private transient Part uploadedImage;
    private List<Employee> employees;

    private final EmployeeDAO dao = new EmployeeDAO();

    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (isBlank(employeeId) || isBlank(name) || isBlank(email)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Employee ID, Name, and Email are required."));
            return null;
        }

        String normalizedEmployeeId = employeeId.trim();
        String normalizedEmail = email.trim();
        if (dao.employeeIdExists(normalizedEmployeeId)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Employee ID '" + normalizedEmployeeId + "' is already registered."));
            return null;
        }
        if (dao.emailExists(normalizedEmail)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Email '" + normalizedEmail + "' is already registered."));
            return null;
        }

        Employee emp = new Employee();
        emp.setEmployeeId(normalizedEmployeeId);
        emp.setName(name.trim());
        emp.setFathersName(trimToEmpty(fathersName));
        emp.setEmail(normalizedEmail);
        emp.setGender(gender);
        emp.setContact(trimToEmpty(contact));
        emp.setImagePath(saveImage(ctx));

        if (!dao.addEmployee(emp)) {
            String detail = dao.getLastErrorMessage();
            if (isBlank(detail)) {
                detail = "Registration failed. Please check the database connection and server logs.";
            }
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", detail));
            return null;
        }

        clearForm();
        employees = null;
        return "employees?faces-redirect=true";
    }

    public String viewEmployees() {
        return "employees?faces-redirect=true";
    }

    public String goToDetail(int employeeDbId) {
        return "employeeDetail?faces-redirect=true&empId=" + employeeDbId;
    }

    public List<Employee> getEmployees() {
        if (employees == null) {
            employees = dao.getAllEmployees();
        }
        return employees;
    }

    private String saveImage(FacesContext ctx) {
        String imagePath = "resources/uploads/default_avatar.png";
        if (uploadedImage == null || uploadedImage.getSize() == 0) {
            return imagePath;
        }

        String fileName = System.currentTimeMillis() + "_" + getSubmittedFileName(uploadedImage)
            .replaceAll("[^a-zA-Z0-9._-]", "_");

        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        String uploadDir = request.getServletContext().getRealPath("/resources/uploads");
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Warning", "Upload directory could not be created; default avatar used."));
            return imagePath;
        }

        try (InputStream in = uploadedImage.getInputStream()) {
            Files.copy(in, Paths.get(uploadDir, fileName), StandardCopyOption.REPLACE_EXISTING);
            return "resources/uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Warning", "Image could not be saved; default avatar used."));
            return imagePath;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void clearForm() {
        employeeId = null;
        name = null;
        fathersName = null;
        email = null;
        gender = null;
        contact = null;
        uploadedImage = null;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFathersName() {
        return fathersName;
    }

    public void setFathersName(String fathersName) {
        this.fathersName = fathersName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Part getUploadedImage() {
        return uploadedImage;
    }

    public void setUploadedImage(Part uploadedImage) {
        this.uploadedImage = uploadedImage;
    }
}
