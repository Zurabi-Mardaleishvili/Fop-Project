import java.util.HashMap;
import java.util.Map;

public class Main {
    private final Map<String, Integer> variables = new HashMap<>(); // Variable storage

    public void eval(String code) {
        String[] lines = code.split("\\n"); // Split by newlines for multi-line code
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("var")) {
                handleVariableDeclaration(line);
            } else if (line.startsWith("for")) {
                i = handleForLoop(lines, i);
            } else if (line.startsWith("print")) {
                handlePrint(line);
            } else if (line.contains("=") || line.endsWith("++") || line.endsWith("--")) {
                handleAssignment(line);
            }
        }
    }

    private void handleVariableDeclaration(String line) {
        String[] parts = line.split("=");
        String varName = parts[0].replace("var", "").replace(";", "").trim();
        int value = 0; // Default value for uninitialized variables
        if (parts.length > 1) {
            String valueString = parts[1].replace(";", "").trim();
            value = Integer.parseInt(valueString);
        }
        variables.put(varName, value);
    }

    private void handleAssignment(String line) {
        if (line.contains("=")) {
            String[] parts = line.split("=");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid assignment statement: " + line);
            }
            String varName = parts[0].replace(";", "").trim();
            String expression = parts[1].replace(";", "").trim();
            int value = evaluateExpression(expression);
            variables.put(varName, value);
        } else if (line.endsWith("++")) {
            String varName = line.replace("++", "").replace(";", "").trim();
            if (!variables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not initialized: " + varName);
            }
            variables.put(varName, variables.get(varName) + 1);
        } else if (line.endsWith("--")) {
            String varName = line.replace("--", "").replace(";", "").trim();
            if (!variables.containsKey(varName)) {
                throw new IllegalArgumentException("Variable not initialized: " + varName);
            }
            variables.put(varName, variables.get(varName) - 1);
        } else {
            throw new IllegalArgumentException("Unsupported assignment statement: " + line);
        }
    }

    private int handleForLoop(String[] lines, int currentIndex) {
        String forLine = lines[currentIndex].trim();
        String[] parts = forLine.substring(forLine.indexOf("for") + 3, forLine.indexOf("{")).trim().split(";");
        String initialization = parts[0].replace(";", "").trim();
        String condition = parts[1].replace(";", "").trim();
        String increment = parts[2].replace(";", "").trim();
        handleAssignment(initialization);

        int i = currentIndex + 1;
        while (evaluateCondition(condition)) {
            int blockStart = i;
            while (i < lines.length && !lines[i].trim().equals("}")) {
                eval(lines[i].trim());
                i++;
            }
            handleAssignment(increment);
            i = blockStart;
        }
        while (i < lines.length && !lines[i].trim().equals("}")) {
            i++;
        }
        return i;
    }

    private boolean evaluateCondition(String condition) {
        if (condition.contains("<=")) {
            String[] tokens = condition.split("<=");
            return getValue(tokens[0]) <= Integer.parseInt(tokens[1].trim());
        } else if (condition.contains("<")) {
            String[] tokens = condition.split("<");
            return getValue(tokens[0]) < Integer.parseInt(tokens[1].trim());
        } else if (condition.contains(">=")) {
            String[] tokens = condition.split(">=");
            return getValue(tokens[0]) >= Integer.parseInt(tokens[1].trim());
        } else if (condition.contains(">")) {
            String[] tokens = condition.split(">");
            return getValue(tokens[0]) > Integer.parseInt(tokens[1].trim());
        } else if (condition.contains("==")) {
            String[] tokens = condition.split("==");
            return getValue(tokens[0]) == Integer.parseInt(tokens[1].trim());
        }
        throw new RuntimeException("Unsupported condition: " + condition);
    }

    private int evaluateExpression(String expression) {
        if (expression.contains("+")) {
            String[] tokens = expression.split("\\+");
            return getValue(tokens[0]) + getValue(tokens[1]);
        }
        return getValue(expression);
    }

    private int getValue(String token) {
        token = token.replace(";", "").trim();
        if (variables.containsKey(token)) {
            return variables.get(token);
        } else {
            return Integer.parseInt(token);
        }
    }

    private void handlePrint(String line) {
        String varName = line.substring(line.indexOf('(') + 1, line.indexOf(')')).replace(";", "").trim();
        if (variables.containsKey(varName)) {
            System.out.println(variables.get(varName));
        } else {
            System.out.println("Undefined variable: " + varName);
        }
    }

    public static void main(String[] args) {
        Main interpreter = new Main();
        String program = """
            var sum = 0;
            for i = 1; i <= 5; i++ {
                sum = sum + i;
            }
            print(sum);
        """;
        interpreter.eval(program);
    }
}




