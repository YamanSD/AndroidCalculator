package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiFunction;


public class MainActivity extends AppCompatActivity {
    /**
     * Current expression on display
     */
    private String currentExp = "";

    /**
     * Display instance for the calculator
     */
    private EditText display;

    /**
     * Set of mathematical operators
     */
    private static final HashMap<String, BiFunction<Double, Double, Double>> operators =
            new HashMap<String, BiFunction<Double, Double, Double>>() {{
        put("+", Double::sum);
    }};

    /**
     * Maps button IDs to string operators.
     */
    private static final HashMap<Integer, String> btnToOperator = new HashMap<Integer, String>() {{
       put(R.id.plusBtn, "+");
    }};

    /**
     * Buttons in this set do not clear the screen on evaluation.
     */
    private static final HashSet<Integer> nonClearingBtnSet = new HashSet<Integer>() {{
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);
    }

    /**
     * @param token a number or a mathematical function (sin, cos, etc...)
     *         with a numerical input.
     * @return if the token is a number, the number is parsed.
     *         Otherwise, the execution is evaluated.
     */
    protected double evaluateToken(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("No implemented functions");
        }
    }

    /**
     * @param l left operand
     * @param operator operator string
     * @param r right operand
     * @return the result of the operator on the operands
     */
    protected double evaluateExpression(double l, String operator, double r) {
        return Objects.requireNonNull(operators.get(operator)).apply(l, r);
    }

    /**
     * @param token any parsed token.
     * @return true if the token is an operator.
     */
    protected boolean isOperator(String token) {
        return operators.containsKey(token);
    }

    /**
     * @param token char
     * @return true if the char token is an operator.
     */
    protected boolean isOperator(char token) {
        return isOperator("" + token);
    }

    /**
     * @param exp mathematical expression string.
     * @return a list of tokens (numbers and operators) present in the expression.
     */
    protected ArrayList<String> parseExpression(String exp) {
        /* list of parsed tokens */
        ArrayList<String> result = new ArrayList<>();

        /* holds the current character */
        char c;

        /* token being parsed */
        StringBuilder current = new StringBuilder();

        /* parse the expression */
        for (int i = 0; i < exp.length(); i++) {
            c = exp.charAt(i);

            if (Character.isWhitespace(c)) {
                if (current.length() != 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                } else if (Character.isDigit(c)) {
                    result.add("" + c);
                }
            } else if (Character.isDigit(c) || c == '.') {
                current.append(c);
            } else if (isOperator(c)) {
                if (current.length() != 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                    result.add("" + c);
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
            }
        }

        /* add last token */
        if (current.length() != 0) {
            result.add(current.toString());
        }

        return result;
    }

    /**
     * @param tokens list of parsed tokens.
     * @return the evaluation of the tokens.
     */
    protected double evaluateTokens(ArrayList<String> tokens) {
        /* initial value */
        double value = 0;

        /* latestOperator */
        String latestOperator = "";

        for (String token: tokens) {
            if (isOperator(token)) {
                if (latestOperator.length() != 0) {
                    throw new IllegalArgumentException("Double operators");
                } else {
                    latestOperator = token;
                }
            } else {
                if (latestOperator.length() != 0) {
                    value = evaluateExpression(value, latestOperator, evaluateToken(token));
                    latestOperator = "";
                } else {
                    value = evaluateToken(token);
                }
            }
        }

        return value;
    }

    /**
     * This form of evaluation is a simple evaluation from left to right, without
     * accounting for precedence of operators.
     * Mobile phone calculators usually do not have precedence.
     * In order to implement precedence, create a new parser that separates based on precedence,
     * & then feed the expressions into the already existing architecture.
     *
     * @param exp mathematical expression
     * @return its evaluation
     */
    protected Double evaluateExpression(String exp) {
        return evaluateTokens(parseExpression(exp));
    }

    /**
     * @param v pressed button.
     * @return the operator associated with the button.
     */
    protected String getOperator(View v) {
        return btnToOperator.get(v.getId());
    }

    /**
     * @param v pressed button.
     * @return true if the button requires a clean screen
     */
    protected boolean doClear(View v) {
        return !nonClearingBtnSet.contains(v.getId());
    }

    /**
     * Clears the display.
     */
    protected void clearDisplay() {
        display.setText("");
    }

    /**
     * @param value to be displayed on the display.
     */
    protected void setDisplay(String value) {
        display.setText(value);
    }

    /**
     * @return the displayed text on screen.
     */
    protected String onDisplay() {
        return display.getText().toString();
    }

    /**
     * Processes calculator input based on state of the expression.
     *
     * @param view pressed button
     */
    public void executeExpression(View view) {
        /* text on display */
        final String onDisplay = onDisplay();

        /* no input is provided */
        if (onDisplay.length() == 0) {
            return;
        }

        // Process input
        if (currentExp.length() == 0) {
            // No input thus far
            currentExp = onDisplay + getOperator(view); // Add the value on display
            clearDisplay();
        } else if (isOperator(currentExp.charAt(currentExp.length() - 1))) {
            // current expression has an operator
            currentExp = evaluateExpression(currentExp + onDisplay).toString();
            setDisplay(currentExp);
        } else {
            // Add the operator to the end of the current expression
            currentExp += getOperator(view);

            if (doClear(view)) {
                clearDisplay();
            } else {
                setDisplay(currentExp);
            }
        }
    }
}