# Employee Portal

JSF 2.2, CSS, MySQL, and Maven WAR project for registering employees, viewing employee cards, editing profiles, and managing custom employee fields.

## Prerequisites

| Tool | Version |
| --- | --- |
| JDK | 8 or 11 |
| Apache Tomcat | 9.x |
| MySQL Server | 8.x |
| Maven | 3.x |

## Database Setup

Run the single supported schema:

```sql
source database_schema.sql;
```

It creates the `employee_portal` database with these tables:

- `employees`
- `employee_custom_fields`

## Database Credentials

By default the app connects to:

```text
jdbc:mysql://localhost:3306/employee_portal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
user: root
password: empty
```

Override these without editing source by setting environment variables or JVM system properties:

```text
EMPLOYEE_DB_URL
EMPLOYEE_DB_USER
EMPLOYEE_DB_PASSWORD
```

The connection helper is `src/main/java/com/employee/util/DBConnection.java`.

## Build

```bash
mvn test
mvn package
```

The WAR is created at:

```text
target/EmployeePortal.war
```

Deploy it to Tomcat and open:

```text
http://localhost:8080/EmployeePortal/
```

## Project Layout

```text
EmployeePortal/
|-- src/main/java/com/employee/
|   |-- bean/
|   |   |-- EmployeeBean.java
|   |   `-- EmployeeDetailBean.java
|   |-- dao/
|   |   `-- EmployeeDAO.java
|   |-- model/
|   |   |-- CustomField.java
|   |   `-- Employee.java
|   `-- util/
|       `-- DBConnection.java
|-- WebContent/
|   |-- WEB-INF/
|   |   |-- faces-config.xml
|   |   |-- lib/
|   |   `-- web.xml
|   |-- resources/css/style.css
|   |-- employeeDetail.xhtml
|   |-- employees.xhtml
|   `-- index.xhtml
|-- database_schema.sql
`-- pom.xml
```

## Notes

- `WebContent` is the WAR source directory configured in `pom.xml`.
- Uploaded images are stored under `resources/uploads` inside the deployed webapp.
- Required JSF, JSTL, and MySQL JARs are currently bundled under `WebContent/WEB-INF/lib`.
