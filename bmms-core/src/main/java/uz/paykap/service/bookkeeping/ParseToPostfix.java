package uz.paykap.service.bookkeeping;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseToPostfix {
    private static String OPERATOR = "^[+\\-*/^]$";
    private static String NUMBER = "^-?(?:(?:0|[1-9][0-9]*)(?:[\\.][0-9]+)?|[1-9][0-9]{1,2}(?:[\\.][0-9]{3})+)$";
    private static String BRACKET = "^(\\(|\\))$";

    Map<String, Integer> priorities;

    public ParseToPostfix() {
        priorities = new HashMap<>();

        addPriorities();
    }

    // Input: Input line in String format with or without whitespace
    // Output: List of tokens in INFIX notation
    // Description: Converts line (String) to list of tokens
    public ArrayList<String> parse(String input) {
        Pattern op = Pattern.compile(OPERATOR);
        Pattern num = Pattern.compile(NUMBER);
        Pattern bracket = Pattern.compile(BRACKET);
        Matcher m;
        Matcher m3;

        if (input == null || input.isEmpty()) {
            return new ArrayList<String>();
        }

        // result tokens
        List<String> tokens = new ArrayList<String>();
        List<String> secondTokens = new ArrayList<String>();

        // remove whitespace
        input = input.replaceAll("\\s+", "");

        String token = "";

        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);

            m = op.matcher(character + "");
            if (!m.find()) {
                token += character;
            } else {

                if (tokens.size() > 0) {

                }
                if (token.length() > 0) {
                    tokens.add(token);
                    token = "";
                }
                if (((tokens.size() == 0)
                    || (tokens.size() > 0 && (op.matcher(tokens.get(tokens.size() - 1)).find())
                    || (tokens.get(tokens.size() - 1).length() > 0
                    && bracket
                    .matcher(tokens.get(tokens.size() - 1)
                        .substring(tokens.get(tokens.size() - 1).length() - 1))
                    .find())))
                    && Character.toString(character).equals("-")) {
                    token += character;
                    continue;
                }
                tokens.add(character + "");

            }
        }
        tokens.add(token);

        // second iteration
        for (int i = 0; i < tokens.size(); i++) {
            String currentToken = tokens.get(i);
            m = op.matcher(currentToken);
            if (!m.find()) {
                String tempToken = currentToken.replaceAll("\\(", "~(~");
                tempToken = tempToken.replaceAll("\\)", "~)~");
                List<String> splitTokens = new ArrayList<String>(Arrays.asList(tempToken.split("~")));
                splitTokens.removeAll(Arrays.asList("", null));
                for (String splitToken : splitTokens) {
                    m = num.matcher(splitToken);

                    m3 = bracket.matcher(splitToken);

                    if (m.find() || m3.find()) {
                        secondTokens.add(splitToken);
                    } else {
                        return new ArrayList<String>();
                    }
                }
            } else {
                secondTokens.add(currentToken);
            }
        }
        if (secondTokens.size() >= 1)
            return (ArrayList<String>) secondTokens;
        else
            return new ArrayList<String>();
    }

    // Input: List of tokens in INFIX notation
    // Output: List of tokens in POSTFIX notation
    // Description: Converts tokens to postfix notation
    public List<String> generatePostfixNotation(ArrayList<String> tokens) {
        List<String> postFix = new ArrayList<String>();
        Stack<String> operators = new Stack<String>();
        String poped = "";

        for (String token : tokens) {
            int prio = 0;
            if (priorities.containsKey(token)) {
                prio = priorities.get(token);
            }

            if (isNumeric(token)) {
                postFix.add(getDoubleValue(token) + "");
            } else if (prio == 0) {
                operators.push(token);
            } else if (token.equals(")")) {
                poped = operators.pop();
                while (!poped.contains("(")) {
                    postFix.add(poped);
                    poped = operators.pop();
                }
                int prioPeek = 0;
                if (operators.size() > 0) {
                    prioPeek = priorities.get(operators.get(operators.size() - 1));
                }
                if (operators.size() > 0 && prioPeek == 0 && !operators.get(operators.size() - 1).equals("(")) {
                    postFix.add(operators.pop());
                }
            } else {

                if (operators.size() > 0 && checkPriority(operators.get(operators.size() - 1), token)) {
                    for (String string : postFix) {

                    }
                    while (checkPriority(operators.get(operators.size() - 1), token)) {
                        poped = operators.pop();
                        postFix.add(poped);

                        if (operators.size() == 0) {
                            break;
                        }
                    }

                    operators.push(token);
                } else {
                    operators.push(token);
                }
            }
        }

        while (operators.size() > 0) {
            postFix.add(operators.pop());
        }

        return postFix;
    }

    private double getDoubleValue(String s) {
        double result;
        try {
            result = Double.parseDouble(s);
            return result;

        } catch (Exception e) {
            return 0;
        }

    }

    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean checkPriority(String op1, String op2) {
        return priorities.get(op1) >= priorities.get(op2);
    }

    private void addPriorities() {
        priorities.put(")", -1);
        priorities.put("(", 0);
        priorities.put("+", 1);
        priorities.put("-", 1);
        priorities.put("*", 2);
        priorities.put("/", 2);
        priorities.put("^", 3);
    }

    public String listToString(List<String> list) {
        String s = "";
        for (int i = 0; i < list.size(); i++) {

            s += list.get(i) + ((i == list.size() - 1) ? "" : " ");
        }
        return s;
    }

}
