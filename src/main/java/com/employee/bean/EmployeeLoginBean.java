package com.employee.bean;

import com.employee.dao.EmployeeDAO;
import com.employee.model.Employee;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "loginBean")
@SessionScoped
public class EmployeeLoginBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String email;
    private Integer loggedInEmployeeId;

    private final EmployeeDAO dao = new EmployeeDAO();

    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (isBlank(employeeId) || isBlank(email)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Employee ID and Email are required."));
            return null;
        }

        Employee employee = dao.findByEmployeeIdAndEmail(employeeId.trim(), email.trim());
        if (employee == null) {
            String detail = dao.getLastErrorMessage();
            if (isBlank(detail)) {
                detail = "Invalid login. Use the same Employee ID and Email saved during registration.";
            }
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
            return null;
        }

        loggedInEmployeeId = employee.getId();
        ctx.getExternalContext().getSessionMap().put("loggedInEmployeeId", employee.getId());
        clearCredentials();
        return "employeeDetail?faces-redirect=true&empId=" + employee.getId();
    }

    public String loginWithEmployeeIdOnly() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (isBlank(employeeId)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error", "Employee ID is required."));
            return null;
        }

        Employee employee = dao.findByEmployeeId(employeeId.trim());
        if (employee == null) {
            String detail = dao.getLastErrorMessage();
            if (isBlank(detail)) {
                detail = "No employee found for Employee ID '" + employeeId.trim() + "'.";
            }
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
            return null;
        }

        loggedInEmployeeId = employee.getId();
        ctx.getExternalContext().getSessionMap().put("loggedInEmployeeId", employee.getId());
        clearCredentials();
        return "employeeDetail?faces-redirect=true&empId=" + employee.getId();
    }

    public String logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        loggedInEmployeeId = null;
        ctx.getExternalContext().getSessionMap().remove("loggedInEmployeeId");
        ctx.getExternalContext().invalidateSession();
        return "login?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return getLoggedInEmployeeId() != null;
    }

    public boolean getLoggedIn() {
        return isLoggedIn();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearCredentials() {
        employeeId = null;
        email = null;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Employee getLoggedInEmployee() {
        Integer id = getLoggedInEmployeeId();
        return id == null ? null : dao.getEmployeeById(id);
    }

    public Integer getLoggedInEmployeeId() {
        if (loggedInEmployeeId != null) {
            return loggedInEmployeeId;
        }
        Object sessionId = FacesContext.getCurrentInstance()
            .getExternalContext()
            .getSessionMap()
            .get("loggedInEmployeeId");
        if (sessionId instanceof Integer) {
            loggedInEmployeeId = (Integer) sessionId;
        }
        return loggedInEmployeeId;
    }
}
