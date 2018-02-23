package app;

import java.io.*;
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
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	return 0;
    }

    private static void printVariables(ArrayList<Variable> vars) {
        for (Variable ss: vars) {
            System.out.println(ss);
        }
    }

    /**
     * Utility method, prints the symbols in the arrays list
     */
    private static void printArrays(ArrayList<Array> arrays) {
        for (Array as: arrays) {
            System.out.println(as);
        }
    }
}
