package com.example.employeeai.ai;

import com.example.employeeai.config.GeminiConfig;
import com.example.employeeai.dto.AIAnswerResponse;
import com.example.employeeai.entity.EmployeeStatus;
import com.example.employeeai.entity.EmploymentType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiAssistantService {

    private static final String INSTRUCTIONS = """
            You are an enterprise Employee Management Assistant.

            Your job is to help users manage and analyze employee information.

            Rules:
            1. Always use tools when the user asks about current employee data.
            2. Never invent employee information.
            3. Use search/list/get tools for employee information.
            4. Use employee_summary for questions about totals, counts, average salary,
               departments, and employee statistics.
            5. Only create an employee when the user explicitly asks to create one.
            6. Only update an employee when the user explicitly asks to update one.
            7. Only delete an employee when the user explicitly asks to delete one.
            8. Never perform destructive operations based only on an informational question.
            9. Use the database/tool response as the source of truth.
            10. Be concise, professional, and clear.
            11. If no employee matches the request, clearly tell the user.
            12. For salary questions, use actual employee data from the tools.
            """;

    private final RestClient geminiRestClient;
    private final GeminiConfig config;
    private final EmployeeAiTools tools;
    private final ObjectMapper objectMapper;

    /**
     * Main Gemini AI entry point.
     */
    public AIAnswerResponse ask(String question) {

        if (config.apiKey() == null || config.apiKey().isBlank()) {
            return new AIAnswerResponse(
                    "Gemini AI is not configured. Set GEMINI_API_KEY and restart the application.",
                    List.of()
            );
        }

        if (question == null || question.isBlank()) {
            return new AIAnswerResponse(
                    "Please provide a question.",
                    List.of()
            );
        }

        try {

            List<Map<String, Object>> contents =
                    new ArrayList<>();

            contents.add(userContent(question));

            List<String> usedTools =
                    new ArrayList<>();

            /*
             * Gemini may need multiple rounds:
             *
             * User
             *   ↓
             * Gemini
             *   ↓
             * Function Call
             *   ↓
             * EmployeeAiTools
             *   ↓
             * Gemini
             *   ↓
             * Final Answer
             */
            for (int round = 0; round < 6; round++) {

                JsonNode response =
                        generate(contents);

                JsonNode candidate =
                        response
                                .path("candidates")
                                .path(0);

                if (candidate.isMissingNode()
                        || candidate.isNull()) {

                    return new AIAnswerResponse(
                            "Gemini did not return a valid response.",
                            usedTools
                    );
                }

                JsonNode content =
                        candidate.path("content");

                JsonNode parts =
                        content.path("parts");

                List<Map<String, Object>>
                        functionResponses =
                        new ArrayList<>();

                /*
                 * Check whether Gemini requested
                 * one or more function calls.
                 */
                for (JsonNode part : parts) {

                    JsonNode functionCall =
                            part.path("functionCall");

                    if (functionCall.isMissingNode()
                            || functionCall.isNull()) {
                        continue;
                    }

                    String functionName =
                            functionCall
                                    .path("name")
                                    .asText();

                    if (functionName == null
                            || functionName.isBlank()) {
                        continue;
                    }

                    usedTools.add(functionName);

                    Object result;

                    try {

                        result = executeTool(
                                functionName,
                                functionCall.path("args")
                        );

                    } catch (Exception ex) {

                        result = Map.of(
                                "error",
                                safeMessage(ex)
                        );
                    }

                    /*
                     * Gemini function response.
                     */
                    Map<String, Object>
                            functionResponse =
                            new LinkedHashMap<>();

                    functionResponse.put(
                            "functionResponse",
                            Map.of(
                                    "name",
                                    functionName,
                                    "response",
                                    Map.of(
                                            "result",
                                            result
                                    )
                            )
                    );

                    functionResponses.add(
                            functionResponse
                    );
                }

                /*
                 * No function call means Gemini has
                 * generated the final answer.
                 */
                if (functionResponses.isEmpty()) {

                    String answer =
                            extractText(response);

                    if (answer.isBlank()) {
                        answer =
                                "I could not generate an answer.";
                    }

                    return new AIAnswerResponse(
                            answer,
                            usedTools
                    );
                }

                /*
                 * Add Gemini's model response to
                 * conversation history.
                 */
                Map<String, Object> modelContent =
                        objectMapper.convertValue(
                                content,
                                new TypeReference<
                                        Map<String, Object>
                                        >() {
                                }
                        );

                contents.add(modelContent);

                /*
                 * Send tool results back to Gemini.
                 */
                contents.add(
                        Map.of(
                                "role",
                                "user",
                                "parts",
                                functionResponses
                        )
                );
            }

            return new AIAnswerResponse(
                    "The AI reached its tool-call limit. Please try a simpler request.",
                    usedTools
            );

        } catch (Exception ex) {

            return new AIAnswerResponse(
                    "Gemini request failed: "
                            + safeMessage(ex),
                    List.of()
            );
        }
    }

    /**
     * Calls Gemini generateContent REST API.
     *
     * IMPORTANT:
     *
     * API key is sent using:
     *
     * x-goog-api-key
     *
     * NOT:
     *
     * ?key=...
     */
    private JsonNode generate(
            List<Map<String, Object>> contents
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        /*
         * Gemini system instruction.
         */
        body.put(
                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(
                                Map.of(
                                        "text",
                                        INSTRUCTIONS
                                )
                        )
                )
        );

        /*
         * Conversation.
         */
        body.put(
                "contents",
                contents
        );

        /*
         * Function calling tools.
         */
        body.put(
                "tools",
                List.of(
                        Map.of(
                                "functionDeclarations",
                                toolDefinitions()
                        )
                )
        );

        /*
         * Gemini REST call.
         */
        return geminiRestClient
                .post()
                .uri(
                        "/v1beta/models/"
                                + config.model()
                                + ":generateContent"
                )
                .header(
                        "x-goog-api-key",
                        config.apiKey()
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .accept(
                        MediaType.APPLICATION_JSON
                )
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * Creates Gemini user message.
     */
    private Map<String, Object> userContent(
            String text
    ) {

        return Map.of(
                "role",
                "user",
                "parts",
                List.of(
                        Map.of(
                                "text",
                                text
                        )
                )
        );
    }

    /**
     * Extract final text from Gemini response.
     */
    private String extractText(
            JsonNode response
    ) {

        StringBuilder result =
                new StringBuilder();

        JsonNode parts =
                response
                        .path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts");

        for (JsonNode part : parts) {

            if (part.has("text")) {

                String text =
                        part.path("text")
                                .asText();

                if (!text.isBlank()) {
                    result.append(text);
                }
            }
        }

        return result.toString().trim();
    }

    /**
     * Executes Gemini requested function.
     */
    private Object executeTool(
            String name,
            JsonNode args
    ) {

        return switch (name) {

            case "list_employees" ->
                    tools.listEmployees();

            case "get_employee" ->
                    tools.getEmployee(
                            args.path("id").asLong()
                    );

            case "search_employees" ->
                    tools.searchEmployees(
                            text(args, "query")
                    );

            case "employee_summary" ->
                    tools.employeeSummary();

            case "create_employee" ->
                    tools.createEmployee(
                            text(args, "employeeCode"),
                            text(args, "firstName"),
                            text(args, "lastName"),
                            text(args, "email"),
                            text(args, "phone"),
                            text(args, "jobTitle"),
                            text(args, "department"),
                            enumValue(
                                    args,
                                    "employmentType",
                                    EmploymentType.class
                            ),
                            enumValue(
                                    args,
                                    "status",
                                    EmployeeStatus.class
                            ),
                            date(
                                    args,
                                    "hireDate"
                            ),
                            text(args, "location"),
                            text(args, "managerName"),
                            text(args, "skills"),
                            decimal(
                                    args,
                                    "salary"
                            )
                    );

            case "update_employee" ->
                    tools.updateEmployee(
                            args.path("id").asLong(),
                            text(args, "employeeCode"),
                            text(args, "firstName"),
                            text(args, "lastName"),
                            text(args, "email"),
                            text(args, "phone"),
                            text(args, "jobTitle"),
                            text(args, "department"),
                            enumValue(
                                    args,
                                    "employmentType",
                                    EmploymentType.class
                            ),
                            enumValue(
                                    args,
                                    "status",
                                    EmployeeStatus.class
                            ),
                            date(
                                    args,
                                    "hireDate"
                            ),
                            text(args, "location"),
                            text(args, "managerName"),
                            text(args, "skills"),
                            decimal(
                                    args,
                                    "salary"
                            )
                    );

            case "delete_employee" ->
                    tools.deleteEmployee(
                            args.path("id").asLong()
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown AI tool: " + name
                    );
        };
    }

    /**
     * Read String argument.
     */
    private String text(
            JsonNode node,
            String field
    ) {

        if (node == null
                || !node.hasNonNull(field)) {
            return null;
        }

        String value =
                node.path(field).asText();

        return value.isBlank()
                ? null
                : value;
    }

    /**
     * Read BigDecimal argument.
     */
    private BigDecimal decimal(
            JsonNode node,
            String field
    ) {

        if (node == null
                || !node.hasNonNull(field)) {
            return null;
        }

        return node.path(field).decimalValue();
    }

    /**
     * Read LocalDate argument.
     */
    private LocalDate date(
            JsonNode node,
            String field
    ) {

        String value =
                text(node, field);

        if (value == null) {
            return null;
        }

        return LocalDate.parse(value);
    }

    /**
     * Read Enum argument.
     */
    private <T extends Enum<T>> T enumValue(
            JsonNode node,
            String field,
            Class<T> type
    ) {

        String value =
                text(node, field);

        if (value == null) {
            return null;
        }

        return Enum.valueOf(
                type,
                value.toUpperCase(Locale.ROOT)
        );
    }

    /**
     * Get safe exception message.
     */
    private String safeMessage(
            Exception ex
    ) {

        Throwable cause = ex;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message =
                cause.getMessage();

        if (message == null
                || message.isBlank()) {

            return cause
                    .getClass()
                    .getSimpleName();
        }

        return message;
    }

    /**
     * Gemini function definitions.
     */
    private List<Map<String, Object>>
    toolDefinitions() {

        return List.of(

                function(
                        "list_employees",
                        "List all employees.",
                        schema(Map.of()),
                        List.of()
                ),

                function(
                        "get_employee",
                        "Get one employee by numeric database ID.",
                        schema(
                                Map.of(
                                        "id",
                                        Map.of(
                                                "type",
                                                "integer",
                                                "description",
                                                "Employee database ID"
                                        )
                                )
                        ),
                        List.of("id")
                ),

                function(
                        "search_employees",
                        "Search employees by employee code, first name, last name, email, job title, or department.",
                        schema(
                                Map.of(
                                        "query",
                                        Map.of(
                                                "type",
                                                "string",
                                                "description",
                                                "Search text"
                                        )
                                )
                        ),
                        List.of("query")
                ),

                function(
                        "employee_summary",
                        "Return total employees, status counts, average salary, and department counts.",
                        schema(Map.of()),
                        List.of()
                ),

                function(
                        "create_employee",
                        "Create a new employee. Only use when the user explicitly asks to create an employee.",
                        employeeSchema(false),
                        List.of(
                                "firstName",
                                "lastName",
                                "email"
                        )
                ),

                function(
                        "update_employee",
                        "Update an employee. Only use when the user explicitly asks to update an employee.",
                        employeeSchema(true),
                        List.of("id")
                ),

                function(
                        "delete_employee",
                        "Delete an employee. Only use when the user explicitly asks to delete an employee.",
                        schema(
                                Map.of(
                                        "id",
                                        Map.of(
                                                "type",
                                                "integer",
                                                "description",
                                                "Employee database ID"
                                        )
                                )
                        ),
                        List.of("id")
                )
        );
    }

    /**
     * Employee schema.
     *
     * IMPORTANT:
     * Do NOT add additionalProperties.
     * Gemini rejects that property in this schema.
     */
    private Map<String, Object> employeeSchema(
            boolean includeId
    ) {

        Map<String, Object> properties =
                new LinkedHashMap<>();

        if (includeId) {

            properties.put(
                    "id",
                    Map.of(
                            "type",
                            "integer",
                            "description",
                            "Employee database ID"
                    )
            );
        }

        properties.put(
                "employeeCode",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "firstName",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "lastName",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "email",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "phone",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "jobTitle",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "department",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "employmentType",
                Map.of(
                        "type",
                        "string",
                        "enum",
                        List.of(
                                "FULL_TIME",
                                "PART_TIME",
                                "CONTRACT",
                                "INTERN"
                        )
                )
        );

        properties.put(
                "status",
                Map.of(
                        "type",
                        "string",
                        "enum",
                        List.of(
                                "ACTIVE",
                                "ON_LEAVE",
                                "INACTIVE"
                        )
                )
        );

        properties.put(
                "hireDate",
                Map.of(
                        "type",
                        "string",
                        "description",
                        "Employment start date in yyyy-MM-dd format"
                )
        );

        properties.put(
                "location",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "managerName",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "skills",
                Map.of(
                        "type",
                        "string"
                )
        );

        properties.put(
                "salary",
                Map.of(
                        "type",
                        "number"
                )
        );

        return schema(properties);
    }

    /**
     * Creates Gemini function schema.
     */
    private Map<String, Object> function(
            String name,
            String description,
            Map<String, Object> parameters,
            List<String> required
    ) {

        Map<String, Object> function =
                new LinkedHashMap<>();

        function.put(
                "name",
                name
        );

        function.put(
                "description",
                description
        );

        Map<String, Object> finalParameters =
                new LinkedHashMap<>(
                        parameters
                );

        if (required != null
                && !required.isEmpty()) {

            finalParameters.put(
                    "required",
                    required
            );
        }

        function.put(
                "parameters",
                finalParameters
        );

        return function;
    }

    /**
     * Creates Gemini-compatible object schema.
     *
     * DO NOT use:
     *
     * additionalProperties
     */
    private Map<String, Object> schema(
            Map<String, Object> properties
    ) {

        Map<String, Object> schema =
                new LinkedHashMap<>();

        schema.put(
                "type",
                "object"
        );

        schema.put(
                "properties",
                properties
        );

        return schema;
    }
}