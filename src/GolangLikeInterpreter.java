import java.util.HashMap;
import java.util.Map;

public class GolangLikeInterpreter {
    private final Map<String, Integer> variables = new HashMap<>(); // Variable storage

    public void eval(String code) {
        String[] lines = code.split("\\n"); // Split by lines instead of semicolon
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            // Handle variable declaration
            if (line.startsWith("var")) {
                handleVariableDeclaration(line);
            } else if (line.contains("=")) {
                handleAssignment(line);
            }
            // Handle if conditionals
            else if (line.startsWith("if")) {
                i = handleIf(lines, i);
            }
            // Handle print statements
            else if (line.startsWith("print")) {
                handlePrint(line);
            }
        }
    }

    private void handleVariableDeclaration(String line) {
        // Example: var x = 10;
        String[] parts = line.split("=");
        String varName = parts[0].replace("var", "").trim();
        String rawValue = parts[1].trim();

        // Remove any extra characters (e.g., semicolons) and parse to integer
        String sanitizedValue = rawValue.replaceAll("[^\\d-]", ""); // Keep only digits and minus sign
        int value = Integer.parseInt(sanitizedValue);
        variables.put(varName, value);
    }

    private void handleAssignment(String line) {
        // Example: x = x + 5
        String[] parts = line.split("=");
        String varName = parts[0].trim();
        String expression = parts[1].trim();

        // Evaluate the right-hand expression
        int value = evaluateExpression(expression);
        variables.put(varName, value);
    }

    private int evaluateExpression(String expression) {
        // Supported operators: +, -, *, /, %
        if (expression.contains("+")) {
            String[] tokens = expression.split("\\+");
            return evaluateToken(tokens[0]) + evaluateToken(tokens[1]);
        } else if (expression.contains("-")) {
            String[] tokens = expression.split("-");
            return evaluateToken(tokens[0]) - evaluateToken(tokens[1]);
        } else if (expression.contains("*")) {
            String[] tokens = expression.split("\\*");
            return evaluateToken(tokens[0]) * evaluateToken(tokens[1]);
        } else if (expression.contains("/")) {
            String[] tokens = expression.split("/");
            return evaluateToken(tokens[0]) / evaluateToken(tokens[1]);
        } else if (expression.contains("%")) {
            String[] tokens = expression.split("%");
            return evaluateToken(tokens[0]) % evaluateToken(tokens[1]);
        } else {
            return evaluateToken(expression);
        }
    }

    private int evaluateToken(String token) {
        token = token.trim();
        if (variables.containsKey(token)) {
            return variables.get(token);
        } else {
            return Integer.parseInt(token);
        }
    }

    private int handleIf(String[] lines, int currentIndex) {
        // Example: if x > 5 { ... }
        String conditionLine = lines[currentIndex].trim();
        String condition = conditionLine.substring(conditionLine.indexOf("if") + 2, conditionLine.indexOf("{")).trim();
        boolean conditionResult = evaluateCondition(condition);

        int i = currentIndex + 1;
        if (conditionResult) {
            // Execute the block inside the if statement
            while (i < lines.length && !lines[i].trim().equals("}")) {
                eval(lines[i].trim());
                i++;
            }
        } else {
            // Skip to the end of the block
            while (i < lines.length && !lines[i].trim().equals("}")) {
                i++;
            }
        }
        return i;
    }

    private boolean evaluateCondition(String condition) {
        // Example: x > 5
        if (condition.contains(">")) {
            String[] tokens = condition.split(">");
            return evaluateToken(tokens[0]) > evaluateToken(tokens[1]);
        } else if (condition.contains("<")) {
            String[] tokens = condition.split("<");
            return evaluateToken(tokens[0]) < evaluateToken(tokens[1]);
        } else if (condition.contains("==")) {
            String[] tokens = condition.split("==");
            return evaluateToken(tokens[0]) == evaluateToken(tokens[1]);
        } else if (condition.contains("!=")) {
            String[] tokens = condition.split("!=");
            return evaluateToken(tokens[0]) != evaluateToken(tokens[1]);
        } else if (condition.contains(">=")) {
            String[] tokens = condition.split(">=");
            return evaluateToken(tokens[0]) >= evaluateToken(tokens[1]);
        } else if (condition.contains("<=")) {
            String[] tokens = condition.split("<=");
            return evaluateToken(tokens[0]) <= evaluateToken(tokens[1]);
        } else {
            throw new RuntimeException("Unsupported condition: " + condition);
        }
    }

    private void handlePrint(String line) {
        // Example: print(x)
        String varName = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        if (variables.containsKey(varName)) {
            System.out.println(variables.get(varName));
        } else {
            System.out.println("Undefined variable: " + varName);
        }
    }

    public static void main(String[] args) {
        GolangLikeInterpreter interpreter = new GolangLikeInterpreter();

        // Example program using Golang-like syntax
        String program = """
            var x = 10;
            var y = 5;
            if x > y {
                print(x);
            }
        """;

        interpreter.eval(program); // Outputs: 10
    }
}
