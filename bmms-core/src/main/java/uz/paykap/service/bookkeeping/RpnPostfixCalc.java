package uz.paykap.service.bookkeeping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

@Service
public class RpnPostfixCalc {

    // Commands
    private static final String QUIT = "quit";
    private static final String VAR = "var";
    private static final String CLEAR = "clear";

    private static ArrayList<String> variableNames = new ArrayList<String>();
    private static ArrayList<Double> variableValues = new ArrayList<Double>();
    private static Stack<Double> operationStack = new Stack<Double>();
    private final static Logger log = LoggerFactory.getLogger(RpnPostfixCalc.class);

    public double calculate(String s) {
        ArrayList<String> input = new ArrayList<String>();
        Collections.addAll(input, s.trim().split(" "));
        input.removeAll(Arrays.asList(null, ""));
        if (input.size() == 1) {
            double d = getValue(s);
            return d;
//            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }

        boolean hasVariableAssignment = input.contains("=");
        String var = "";
        int startIndex = 0;
        if (hasVariableAssignment) {
            var = input.get(0);
            startIndex = 2;
        } else {
            startIndex = 0;
        }
        for (int i = startIndex; i < input.size(); i++) {
            String n = input.get(i);
            if (isOperator(n)) {
                if (operationStack.size() > 1) {
                    operationStack.push(doOperation(n));
                } else {
                    log.warn("\tOperation not valid!");
                    throw new CodedException(ProblemEnum.PARSE_ERROR);
                }
            } else {
                double d = getValue(n);
                if (!Double.isNaN(d)) {
                    operationStack.push(d);
                } else {
                    operationStack.clear();
                    throw new CodedException(ProblemEnum.PARSE_ERROR);
                }
            }
        }
        double result = operationStack.pop();
        if (operationStack.size() > 0) {
            log.warn("\tOperation not valid!");
            operationStack.clear();
            throw new CodedException(ProblemEnum.PARSE_ERROR);
        }

        if (hasVariableAssignment) {
            replaceAddValue(var, result);
        }
        return  result;
    }

    private double doOperation(String s) {
        char op = s.charAt(0);
        double a = operationStack.pop();
        double b = operationStack.pop();
        switch (op) {
            case '+':
                return b + a;
            case '-':
                return b - a;
            case '*':
                return b * a;
            case '/':
                return b / a;
            default:
                return Double.NaN;
        }
    }

    public double getValue(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            try {
                return variableValues.get(variableNames.indexOf(s));
            } catch (Exception e) {
                return Double.NaN;
            }
        }
    }

    private void replaceAddValue(String var, double value) {

        if (variableNames.contains(var)) {
            int index = variableNames.indexOf(var);
            variableValues.set(index, value);
        } else {
            variableNames.add(var);
            variableValues.add(value);
        }
    }

    public boolean isOperator(String s) {
        char c = s.charAt(0);
        return c == '+' || c == '-' || c == '/' || c == '*';
    }
}
