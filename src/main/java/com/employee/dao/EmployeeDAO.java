package com.employee.dao;

import com.employee.model.CustomField;
import com.employee.model.Employee;
import com.employee.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String lastErrorMessage;

    public boolean addEmployee(Employee emp) {
        lastErrorMessage = null;
        String sql = "INSERT INTO employees "
            + "(employee_id, name, fathers_name, email, gender, contact, image_path) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeId());
            ps.setString(2, emp.getName());
            ps.setString(3, emp.getFathersName());
            ps.setString(4, emp.getEmail());
            ps.setString(5, emp.getGender());
            ps.setString(6, emp.getContact());
            ps.setString(7, emp.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            lastErrorMessage = "Employee ID or Email already exists.";
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean employeeIdExists(String employeeId) {
        return valueExists("SELECT COUNT(*) FROM employees WHERE employee_id = ?", employeeId);
    }

    public boolean emailExists(String email) {
        return valueExists("SELECT COUNT(*) FROM employees WHERE email = ?", email);
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = mapRow(rs);
                    emp.setCustomFields(getCustomFields(id, conn));
                    return emp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee findByEmployeeIdAndEmail(String employeeId, String email) {
        lastErrorMessage = null;
        String sql = "SELECT * FROM employees "
            + "WHERE TRIM(employee_id) = ? AND LOWER(TRIM(email)) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId == null ? "" : employeeId.trim());
            ps.setString(2, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = mapRow(rs);
                    emp.setCustomFields(getCustomFields(emp.getId(), conn));
                    return emp;
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        return null;
    }

    public Employee findByEmployeeId(String employeeId) {
        lastErrorMessage = null;
        String sql = "SELECT * FROM employees WHERE TRIM(employee_id) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId == null ? "" : employeeId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = mapRow(rs);
                    emp.setCustomFields(getCustomFields(emp.getId(), conn));
                    return emp;
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees "
            + "SET name=?, fathers_name=?, email=?, gender=?, contact=?, image_path=? "
            + "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getName());
            ps.setString(2, emp.getFathersName());
            ps.setString(3, emp.getEmail());
            ps.setString(4, emp.getGender());
            ps.setString(5, emp.getContact());
            ps.setString(6, emp.getImagePath());
            ps.setInt(7, emp.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomField> getCustomFields(int employeeId, Connection conn) throws SQLException {
        List<CustomField> fields = new ArrayList<>();
        String sql = "SELECT * FROM employee_custom_fields WHERE employee_id = ? ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomField cf = new CustomField();
                    cf.setId(rs.getInt("id"));
                    cf.setEmployeeId(rs.getInt("employee_id"));
                    cf.setFieldLabel(rs.getString("field_label"));
                    cf.setFieldValue(rs.getString("field_value"));
                    fields.add(cf);
                }
            }
        }
        return fields;
    }

    public boolean addCustomField(CustomField cf) {
        String sql = "INSERT INTO employee_custom_fields (employee_id, field_label, field_value) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cf.getEmployeeId());
            ps.setString(2, cf.getFieldLabel());
            ps.setString(3, cf.getFieldValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomField(CustomField cf) {
        String sql = "UPDATE employee_custom_fields SET field_label=?, field_value=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cf.getFieldLabel());
            ps.setString(2, cf.getFieldValue());
            ps.setInt(3, cf.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomField(int fieldId) {
        String sql = "DELETE FROM employee_custom_fields WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private boolean valueExists(String sql, String value) {
        lastErrorMessage = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getInt("id"));
        emp.setEmployeeId(rs.getString("employee_id"));
        emp.setName(rs.getString("name"));
        emp.setFathersName(rs.getString("fathers_name"));
        emp.setEmail(rs.getString("email"));
        emp.setGender(rs.getString("gender"));
        emp.setContact(rs.getString("contact"));
        emp.setImagePath(rs.getString("image_path"));
        emp.setCreatedAt(rs.getString("created_at"));
        return emp;
    }
}
