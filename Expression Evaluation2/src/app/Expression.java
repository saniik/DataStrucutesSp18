package app;

import java.io.*;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     *
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        System.out.println("Expression: " + expr);
        //pattern to be matched
        String lookFor = "([a-zA-Z]+)(\\[)?";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(lookFor);

        // Now create matcher object.
        Matcher m = pattern.matcher(expr);

        while (m.find()) {
            if (m.group(2) == null) {
                Variable var = new Variable(m.group(1));
                if (!vars.contains(var)) {
                    vars.add(var);
                }
            }
            else {
                Array array = new Array(m.group(1));
                if (!arrays.contains(array)) {
                    arrays.add(array);
                }
            }

//            System.out.println("Found value:" + m.group(1));
//            System.out.println("Variables: ");
//            printVariables(vars);
//            System.out.println("Arrays: ");
//            printArrays(arrays);
        }
        System.out.println("Variables: ");
        printVariables(vars);
        System.out.println("Arrays: ");
        printArrays(arrays);
    }

    /**
     * Loads values for variables and arrays in the expression
     *
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // vars symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;
                }
            }
        }
    }

    /**
     * Evaluates the expression.
     *
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        try {
            expr = expr.replaceAll("\\s+","");
            System.out.println("Expression: " + expr);
            return evaluate2(expr, vars, arrays);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception other) {
            other.printStackTrace();
        }
        return -1;
    }

    //
    // Private methods
    //
    private static float evaluate2(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) throws IllegalArgumentException{
        System.out.println();
        System.out.println("Evaluating: " + expr);

        StringTokenizer tokenizer = new StringTokenizer(expr, delims, true);
        int tokenizerPosition = 0;
        float lhs = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            tokenizerPosition += token.length();
            if (isOpenParantheses(token)) {
                int endOfSubExpression = runTokenizerToEndOfCurvySubExpression(tokenizer, tokenizerPosition);
                String subExpression = expr.substring(tokenizerPosition, endOfSubExpression);
                tokenizerPosition = endOfSubExpression+1;
                lhs = evaluate2(subExpression, vars, arrays);
            } else if (isOperation(token) && isHigherOrderOperator(token, nextOperation(expr.substring(tokenizerPosition, expr.length())))) {
                String nextToken = tokenizer.nextToken();
                tokenizerPosition += nextToken.length();
                if (isOpenParantheses(nextToken)) {
                    int endOfSubExpression = runTokenizerToEndOfCurvySubExpression(tokenizer, tokenizerPosition);
                    String subExpression = expr.substring(tokenizerPosition, endOfSubExpression);
                    tokenizerPosition = endOfSubExpression+1;
                    float subExpressionValue = evaluate2(subExpression, vars, arrays);
                    lhs = performOperation(lhs, subExpressionValue, token);
                } else {
                    try {
                        float next = eval(nextToken, vars);
                        lhs = performOperation(lhs, next, token);
                    } catch (NoSuchElementException e) {
                        Array array = findArray(nextToken, arrays);
                        String openSquareBracket = tokenizer.nextToken();
                        tokenizerPosition += nextToken.length();
                        assert array != null;
                        if (isOpenSquareParantheses(openSquareBracket)) {
                            int endOfSquareSubExpression = runTokenizerToEndOfSquareSubExpression(tokenizer, tokenizerPosition);
                            String subExpression = expr.substring(tokenizerPosition, endOfSquareSubExpression);
                            tokenizerPosition = endOfSquareSubExpression+1;
                            int arrayIndex = (int) evaluate2(subExpression, vars, arrays);
                            float arrayValue = arrayValue(array, arrayIndex);
                            System.out.println("Evaluated array " + array.name + "[" + arrayIndex + "] to " + arrayValue);
                            lhs = performOperation(lhs, arrayValue, token);
                        }
                    }
                }
            } else if (isHigherOrderOperator(token, tokenizer.nextToken())) {
                String nextToken = tokenizer.nextToken();
                tokenizerPosition += nextToken.length();
                if (isOpenParantheses(nextToken)) {
                    int endOfSubExpression = runTokenizerToEndOfCurvySubExpression(tokenizer, tokenizerPosition);
                    String subExpression = expr.substring(tokenizerPosition, endOfSubExpression);
                    tokenizerPosition = endOfSubExpression+1;
                    float subExpressionValue = evaluate2(subExpression, vars, arrays);
                    lhs = performOperation(lhs, subExpressionValue, token);
                } else {
                    try {
                        float next = eval(nextToken, vars);
                        lhs = performOperation(lhs, next, token);
                    } catch (NoSuchElementException e) {
                        Array array = findArray(nextToken, arrays);
                        String openSquareBracket = tokenizer.nextToken();
                        tokenizerPosition += nextToken.length();
                        assert array != null;
                        if (isOpenSquareParantheses(openSquareBracket)) {
                            int endOfSquareSubExpression = runTokenizerToEndOfSquareSubExpression(tokenizer, tokenizerPosition);
                            String subExpression = expr.substring(tokenizerPosition, endOfSquareSubExpression);
                            tokenizerPosition = endOfSquareSubExpression+1;
                            int arrayIndex = (int) evaluate2(subExpression, vars, arrays);
                            float arrayValue = arrayValue(array, arrayIndex);
                            System.out.println("Evaluated array " + array.name + "[" + arrayIndex + "] to " + arrayValue);
                            lhs = performOperation(lhs, arrayValue, token);
                        }
                    }
                }
            }else if (isOperation(token)) {
                String rhsExpression = expr.substring(tokenizerPosition, expr.length());
                float rhs = evaluate2(rhsExpression, vars, arrays);
                return performOperation(lhs, rhs, token);
            } else {
                try {
                    lhs = eval(token, vars);
                    System.out.println("Evaluated lhs '" + token + "' to " + lhs);
                } catch (NoSuchElementException e) {
                    Array array = findArray(token, arrays);
                    String openSquareBracket = tokenizer.nextToken();
                    tokenizerPosition += openSquareBracket.length();
                    assert array != null;
                    if (isOpenSquareParantheses(openSquareBracket)) {
                        int endOfSquareSubExpression = runTokenizerToEndOfSquareSubExpression(tokenizer, tokenizerPosition);
                        String subExpression = expr.substring(tokenizerPosition, endOfSquareSubExpression);
                        tokenizerPosition = endOfSquareSubExpression+1;
                        int arrayIndex = (int) evaluate2(subExpression, vars, arrays);
                        lhs = arrayValue(array, arrayIndex);
                        System.out.println("Evaluated array " + array.name + "[" + arrayIndex + "] to " + lhs);
                    }
                }
            }
        }
        return lhs;
    }

    private static float eval(String string, ArrayList<Variable> vars) throws NoSuchElementException {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException exception) {
            Variable variable = findVariable(string, vars);
            if (variable == null) {
                throw new NoSuchElementException();
            }
            return variable.value;
        }
    }

    private static int runTokenizerToEndOfCurvySubExpression(StringTokenizer tokenizer, int tokenizerPosition) {
        int count = 1;
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            tokenizerPosition += next.length();
            if (next.equals("(")) {
                count++;
            }
            if (next.equals(")")) {
                count--;
                if (count == 0) {
                    break;
                }
            }
        }
        return tokenizerPosition - 1;
    }

    private static int runTokenizerToEndOfSquareSubExpression(StringTokenizer tokenizer, int tokenizerPosition) {
        int count = 1;
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            tokenizerPosition += next.length();
            if (next.equals("[")) {
                count++;
            }
            if (next.equals("]")) {
                count--;
                if (count == 0) {
                    break;
                }
            }
        }
        return tokenizerPosition - 1;
    }

    private static String nextOperation(String expr) {
        StringTokenizer tokenizer = new StringTokenizer(expr, delims, true);
        while (tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            if (isOperation(next)) {
                return next;
            }

        }
        return null;
    }

    private static float performOperation(float first, float second, String operator)  throws IllegalArgumentException {
        float result;
        if (operator.equals("+")) {
            result = first + second;
            System.out.println("Adding " + first + " and " + second + " = " + result);
        } else if (operator.equals("-")) {
                result = first - second;
            System.out.println("Subtrating " + first + " and " + second + " = " + result);
        } else if (operator.equals("*")) {
            result = first * second;
            System.out.println("Multiplying " + first + " and " + second + " = " + result);
        } else if (operator.equals("/")) {
            if (second == 0) {
                throw new IllegalArgumentException("Cannot divide by zero");
            }
            result = first / second;
            System.out.println("Dividing " + first + " and " + second + " = " + result);
        } else {
            throw new IllegalArgumentException(operator + " is not a valid operator");
        }
        return result;
    }

    //
    // Operation helpers
    //
    private static boolean isOperation(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private static boolean isOpenSquareParantheses(String token) { return token.equals("["); }
    private static boolean isOpenParantheses(String token) { return token.equals("("); }
    private static boolean isHigherOrderOperator(String token, String nextToken) {
        if (nextToken == null) return true;
        if (token.equals("*") || token.equals("/")) {
            return true;
        } else if (nextToken.equals("*") || nextToken.equals("/")) {
            return false;
        } else {
            return token.equals("-");
        }
    }


    //
    // Find
    //
    private static Variable findVariable(String name, ArrayList<Variable> vars) {
        for (Variable symbol : vars) {
            if (symbol.name.equals(name)) {
                return symbol;
            }
        }
        return null;
    }

    private static Array findArray(String name, ArrayList<Array> arrays) {
        for (Array symbol : arrays) {
            if (symbol.name.equals(name)) {
                return symbol;
            }
        }
        return null;
    }

    private static float arrayValue(Array array, int index) {
        if (array == null) {
            System.out.println("WARNING: Array is null");
            return 0;
        }
        if (index < array.values.length) {
            return array.values[index];
        } else {
            System.out.println("WARNING: Array index out of bounds");
            return 0;
        }
    }

    //
    // Print
    ///
    private static void printVariables(ArrayList<Variable> vars) {
        for (Variable symbol: vars) {
            System.out.println(symbol);
        }
    }

    private static void printArrays(ArrayList<Array> arrays) {
        for (Array symbol: arrays) {
            System.out.println(symbol);
        }
    }
}
