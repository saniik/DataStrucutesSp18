package apps;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
        scalars = new ArrayList <ScalarSymbol>();
        arrays = new ArrayList <ArraySymbol>();

        String input = expr;
        System.out.println("Expression: " + expr);
        //pattern to be matched
        String lookFor = "([a-zA-Z]+)(\\[)?";
        
        // Create a Pattern object
        Pattern pattern = Pattern.compile(lookFor);

        // Now create matcher object.
        Matcher m = pattern.matcher(input);

        while (m.find()) {
            if (m.group(2) == null) {
                ScalarSymbol scalar = new ScalarSymbol(m.group(1));
                if (!scalars.contains(scalar)) {
                    scalars.add(scalar);
                }
            }
            else {
                ArraySymbol array = new ArraySymbol(m.group(1));
                if (!arrays.contains(array)) {
                    arrays.add(array);
                }
            }
            
            System.out.println("Found value:" + m.group(1));
            System.out.println("Scalar Array: ");
            printScalars();
            System.out.println("Array Array: ");
            printArrays();
        }
        
    }

    private boolean isOperation(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @return Result of evaluation
     */


    private float eval(String string) throws NoSuchElementException {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException exception) {
            ScalarSymbol scaSymbol = findScalar(string);
            if (scaSymbol == null) {
                throw new NoSuchElementException();
            }
            return scaSymbol.value;
        }
    }

    public float evaluate() {
        try {
            expr = expr.replaceAll("\\s+","");
            System.out.println("Expression: " + expr);
            return evaluate(expr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception other) {
            other.printStackTrace();
        }
        return -1;
    }
    public float evaluate(String expr) throws IllegalArgumentException{
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
                tokenizerPosition = endOfSubExpression;
                lhs = evaluate(subExpression);
            } else if (isHigherOrderOperator(token)) {
                String nextToken = tokenizer.nextToken();
                tokenizerPosition += nextToken.length();
                if (isOpenParantheses(nextToken)) {
                    int endOfSubExpression = runTokenizerToEndOfCurvySubExpression(tokenizer, tokenizerPosition);
                    String subExpression = expr.substring(tokenizerPosition, endOfSubExpression);
                    tokenizerPosition = endOfSubExpression;
                    float subExpressionValue = evaluate(subExpression);
                    lhs = performOperation(lhs, subExpressionValue, token);
                } else {
                    try {
                        float next = eval(nextToken);
                        lhs = performOperation(lhs, next, token);
                    } catch (NoSuchElementException e) {
                        ArraySymbol arraySymbol = findArray(nextToken);
                        String openSquareBracket = tokenizer.nextToken();
                        tokenizerPosition += token.length();
                        assert arraySymbol != null;
                        if (isOpenSquareParantheses(openSquareBracket)) {
                            int endOfSquareSubExpression = runTokenizerToEndOfSquareSubExpression(tokenizer, tokenizerPosition);
                            String subExpression = expr.substring(tokenizerPosition, endOfSquareSubExpression);
                            tokenizerPosition = endOfSquareSubExpression;
                            int arrayIndex = (int) evaluate(subExpression);
                            float arrayValue = arrayValue(arraySymbol, arrayIndex);
                            System.out.println("Evaluated array " + arraySymbol.name + "[" + arrayIndex + "] to " + arrayValue);
                            lhs = performOperation(lhs, arrayValue, token);
                        }
                    }
                }
            } else if (isOperation(token)) {
                String rhsExpression = expr.substring(tokenizerPosition, expr.length());
                float rhs = evaluate(rhsExpression);
                return performOperation(lhs, rhs, token);
            } else {
                try {
                    lhs = eval(token);
                    System.out.println("Evaluated lhs '" + token + "' to " + lhs);
                } catch (NoSuchElementException e) {
                    ArraySymbol arraySymbol = findArray(token);
                    String openSquareBracket = tokenizer.nextToken();
                    tokenizerPosition += token.length();
                    assert arraySymbol != null;
                    if (isOpenSquareParantheses(openSquareBracket)) {
                        int endOfSquareSubExpression = runTokenizerToEndOfSquareSubExpression(tokenizer, tokenizerPosition);
                        String subExpression = expr.substring(tokenizerPosition, endOfSquareSubExpression);
                        tokenizerPosition = endOfSquareSubExpression;
                        int arrayIndex = (int) evaluate(subExpression);
                        lhs = arrayValue(arraySymbol, arrayIndex);
                        System.out.println("Evaluated array " + arraySymbol.name + "[" + arrayIndex + "] to " + lhs);
                    }
                }
            }
        }
        return lhs;
    }
    private float performOperation(float first, float second, String operator)  throws IllegalArgumentException {
        float result;
        if (operator.equals("+")) {
            result = first + second;
            System.out.println("Adding " + first + " and " + second + " = " + result);
        } else if (operator.equals("-")) {
            result = first  - second;
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


    private int runTokenizerToEndOfCurvySubExpression(StringTokenizer tokenizer, int tokenizerPosition) {
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
        return tokenizerPosition;
    }

    private int runTokenizerToEndOfSquareSubExpression(StringTokenizer tokenizer, int tokenizerPosition) {
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
        return tokenizerPosition;
    }

    private boolean isOpenSquareParantheses(String token) { return token.equals("["); }
    private boolean isOpenParantheses(String token) { return token.equals("("); }
    private boolean isHigherOrderOperator(String token) {
        return token.equals("*") || token.equals("/");
    }

    private ScalarSymbol findScalar(String name) {
        for (ScalarSymbol symbol : scalars) {
            if (symbol.name.equals(name)) {
                return symbol;
            }
        }
        return null;
    }

    private ArraySymbol findArray(String name) {
        for (ArraySymbol symbol : arrays) {
            if (symbol.name.equals(name)) {
                return symbol;
            }
        }
        return null;
    }

    private float arrayValue(ArraySymbol arraySymbol, int index) {
        if (index < arraySymbol.values.length) {
            return arraySymbol.values[index];
        } else {
            System.out.println("Array index out of bounds");
            return 0;
        }
    }
    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
    		for (ArraySymbol as: arrays) {
    			System.out.println(as);
    		}
    }

}
