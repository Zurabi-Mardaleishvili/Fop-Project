import java.util.HashMap;
import java.util.Map;

public class GolangLikeInterpreter {
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
            } else if (line.startsWith("if")) {
                i = handleIf(lines, i);
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
            try {
                // Try to parse the value as an integer
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                // If NumberFormatException occurs, evaluate the expression
                value = evaluateExpression(valueString);
            }
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

        if (forLine.contains(";")) {
            // Traditional for loop: for i = 1; i <= a; i++
            String[] parts = forLine.substring(forLine.indexOf("for") + 3, forLine.indexOf("{")).trim().split(";");
            String initialization = parts[0].trim();
            String condition = parts[1].trim();
            String increment = parts[2].trim();
            handleAssignment(initialization);

            int i = currentIndex + 1;
            while (evaluateCondition(condition)) {
                int blockStart = i;
                while (i < lines.length && !lines[i].trim().equals("}")) {
                    String line = lines[i].trim();
                    if (line.startsWith("if")) {
                        i = handleIf(lines, i);
                    } else {
                        eval(line);
                        i++;
                    }
                }
                handleAssignment(increment);
                i = blockStart;
            }
            while (i < lines.length && !lines[i].trim().equals("}")) {
                i++;
            }
            return i; // Move to the next line after the closing brace
        } else {
            // Condition-only for loop: for num != 0
            String condition = forLine.substring(forLine.indexOf("for") + 3, forLine.indexOf("{")).trim();

            int i = currentIndex + 1;
            while (evaluateCondition(condition)) {
                int blockStart = i;
                while (i < lines.length && !lines[i].trim().equals("}")) {
                    String line = lines[i].trim();
                    if (line.startsWith("if")) {
                        i = handleIf(lines, i);
                    } else {
                        eval(line);
                        i++;
                    }
                }
                i = blockStart;
            }
            while (i < lines.length && !lines[i].trim().equals("}")) {
                i++;
            }
            return i; // Move to the next line after the closing brace
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
                String line = lines[i].trim();
                if (line.startsWith("for")) {
                    i = handleForLoop(lines, i);
                } else {
                    eval(line);
                    i++;
                }
            }
        } else {
            // Skip the block if the condition is false
            while (i < lines.length && !lines[i].trim().equals("}")) {
                i++;
            }
        }
        return i; // Move to the next line after the closing brace
    }



    private boolean evaluateCondition(String condition) {
        if (condition.contains("<=")) {
            String[] tokens = condition.split("<=");
            return getValue(tokens[0]) <= getValue(tokens[1]) || getValue(tokens[0]) <= Integer.parseInt(tokens[1].trim());
        } else if (condition.contains("<")) {
            String[] tokens = condition.split("<");
            return getValue(tokens[0]) < getValue(tokens[1]) || getValue(tokens[0]) < Integer.parseInt(tokens[1].trim());
        } else if (condition.contains(">=")) {
            String[] tokens = condition.split(">=");
            return getValue(tokens[0]) <= getValue(tokens[1]) || getValue(tokens[0]) >= Integer.parseInt(tokens[1].trim());
        } else if (condition.contains(">")) {
            String[] tokens = condition.split(">");
            return getValue(tokens[0]) > getValue(tokens[1]) || getValue(tokens[0]) > Integer.parseInt(tokens[1].trim());
        } else if (condition.contains("==")) {
            String[] tokens = condition.split("==");
            return getValue(tokens[0]) == getValue(tokens[1]) || getValue(tokens[0]) == Integer.parseInt(tokens[1].trim());
        }
        throw new RuntimeException("Unsupported condition: " + condition);


    }

    private int evaluateExpression(String expression) {
        if (expression.contains("+")) {
            String[] tokens = expression.split("\\+");
            return getValue(tokens[0]) + getValue(tokens[1]);
        } else if (expression.contains("-")) {
            String[] tokens = expression.split("-");
            return getValue(tokens[0]) - getValue(tokens[1]);
        } else if (expression.contains("*")) {
            String[] tokens = expression.split("\\*");
            return getValue(tokens[0]) * getValue(tokens[1]);
        } else if (expression.contains("/")) {
            String[] tokens = expression.split("/");
            return getValue(tokens[0]) / getValue(tokens[1]);
        } else if (expression.contains("%")) {
            String[] tokens = expression.split("%");
            return getValue(tokens[0]) % getValue(tokens[1]);
        } else {
            return getValue(expression);
        }
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
        GolangLikeInterpreter interpreter = new GolangLikeInterpreter();



        String program = """
                Sum
                var sum = 0
                for i = 1; i <= 10; i++ {
                    sum = sum + i
                }
                print(sum)
                 
                                
                Factorial
                var factorial = 1
                var i = 1
                for i = 1; i <= 5; i++ {
                    factorial = factorial * i
                }
                println(factorial)
                  
                                
                GCD
                var a = 48
                var b = 18
                var temp = 0
                for b > 0 {
                    temp = b
                    b = a % b
                    a = temp
                }
                println(a)
                  
                                
                ReverseNumber
                var num = 1234
                var reversed = 0
                var lastDigit = 0
                    for num > 0 {
                        lastDigit = num % 10
                        reversed = reversed * 10
                        reversed = reversed + lastDigit
                        num =  num / 10
                    }
                     for num < 0 {
                        lastDigit = num % 10
                        reversed = reversed * 10
                        reversed = reversed + lastDigit
                        num =  num / 10
                    }
                    println(reversed)
                
                
                IsPalindrome
                var num = 12321
                var reversed = 0
                var lastDigit = 0
                var input = num
                           for num > 0 {
                               lastDigit = num % 10
                               reversed = reversed * 10
                               reversed = reversed + lastDigit
                               num = num / 10
                           }
                           for num < 0 {
                               lastDigit = num % 10
                               reversed = reversed*10
                               reversed = reversed + lastDigit
                               num = num / 10
                           }
                           if reversed == input {
                               print(reversed)
                           }
                           
                                    
                SumOfDigits
                          var num = 123456
                              if num < 0 {
                                  num = -num
                              }
                              var sum = 0
                              var digit = 0
                              for num > 0 {
                                  digit = num % 10
                                  sum = sum + digit
                                  num = num / 10
                              }
                              print(sum)
                              

                MultiplicationTable
                       var num = 5
                       var i = 1
                                   for i = 1; i <= 10; i++ {
                                   var result = num * i
                                       print(result)

                   """;
        interpreter.eval(program);













    }
}

