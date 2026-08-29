# Employee AI Standard 2.0

Spring Boot + PostgreSQL employee management backend with Gemini AI function calling.

## Added standard employee fields

- employeeCode
- phone
- jobTitle
- department
- employmentType
- status
- hireDate
- location
- managerName
- skills
- salary

Also included: employee summary endpoint, validation, global error handling, health endpoint, Docker PostgreSQL, and Gemini tools for list/get/search/summary/create/update/delete.

## Run

Requirements: Java 17+, Maven 3.9+, Docker Desktop or PostgreSQL.

Start PostgreSQL:

```bash
docker compose up -d
```

Set Gemini API key in PowerShell:

```powershell
$env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY"
$env:GEMINI_MODEL="gemini-3.7-flash"
```

Run:

```bash
mvn clean test
mvn spring-boot:run
```

The project does not store any API key in source code.

## Database environment variables

Defaults are:

- URL: jdbc:postgresql://localhost:5432/employee_db
- username: postgres
- password: postgres

For another database:

```powershell
$env:DATABASE_URL="jdbc:postgresql://host:5432/database"
$env:DB_USERNAME="..."
$env:DB_PASSWORD="..."
```

## APIs

- GET `/api/health`
- GET `/api/employees`
- GET `/api/employees/{id}`
- GET `/api/employees/search?q=engineering`
- GET `/api/employees/summary`
- POST `/api/employees`
- PUT `/api/employees/{id}`
- DELETE `/api/employees/{id}`
- POST `/api/ai/ask`

Example employee:

```json
{
  "employeeCode": "EMP-00001",
  "firstName": "Amit",
  "lastName": "Sharma",
  "email": "amit.sharma@example.com",
  "phone": "+91-9876543210",
  "jobTitle": "Senior Java Developer",
  "department": "Engineering",
  "employmentType": "FULL_TIME",
  "status": "ACTIVE",
  "hireDate": "2025-04-15",
  "location": "Bengaluru",
  "managerName": "Priya Singh",
  "skills": "Java, Spring Boot, PostgreSQL, Docker",
  "salary": 95000
}
```

`employeeCode` is optional; if omitted, the backend generates `EMP-xxxxx`.

AI request:

```json
{
  "question": "Give me the employee summary"
}
```

Other examples:

- Show all employees.
- Find employees in Engineering.
- Get employee 3.
- What is the average salary?
- How many active employees do we have?
- Create an employee named Rahul Kumar with email rahul@example.com in Engineering.
- Update employee 2 and change the job title to Engineering Manager.
- Delete employee 5.

## Architecture

```text
EmployeeController -> EmployeeService -> EmployeeRepository -> PostgreSQL

AIController -> GeminiAssistantService -> Gemini API
                                      -> EmployeeAiTools
                                      -> EmployeeService
                                      -> PostgreSQL
```

The Gemini integration uses Google's documented `generateContent` function-calling flow: Gemini proposes a function call, the backend executes it, and the backend sends the function result back to Gemini for the final answer.
