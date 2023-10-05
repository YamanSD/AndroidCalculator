package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;


public class MainActivity extends AppCompatActivity {
    /**
     * Current expression on display
     */
    private String currentExp = "";

    /**
     * Display instance for the calculator, used to display input & output.
     */
    private EditText display;

    /**
     * Mini-display instance for the calculator, used to display entire exp.
     */
    private EditText miniDisplay;

    /**
     * true indicates that the user pressed the equals btn
     */
    private boolean hasEqualed = false;

    /**
     * Current selected button
     */
    private int selectedBtn = -1;

    /**
     * Set of mathematical operators
     */
    private static final HashMap<String, BiFunction<Double, Double, Double>> operators =
            new HashMap<String, BiFunction<Double, Double, Double>>() {{
        put("+", Double::sum);
        put("-", (n0, n1) -> n0 - n1);
    }};

    /**
     * Maps button IDs to string operators.
     */
    private static final HashMap<Integer, String> btnToOperator = new HashMap<Integer, String>() {{
       put(R.id.plusBtn, "+");
       put(R.id.minusBtn, "-");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);
        miniDisplay = findViewById(R.id.miniDisplay);
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
                    throw new ArithmeticException();
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

            // If invalid value
            if (value == Double.POSITIVE_INFINITY
                    || value == Double.NEGATIVE_INFINITY
                    || Double.isNaN(value)) {
                throw new ArithmeticException();
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
        return getOperator(v.getId());
    }

    /**
     * @param id of the pressed button.
     * @return the operator associated with the button.
     */
    protected String getOperator(int id) {
        return ' ' + btnToOperator.get(id) + ' ';
    }

    /**
     * Clears the display.
     */
    protected void clearDisplay() {
        display.setText("");
    }

    /**
     * Clears the mini-display.
     */
    protected void clearMiniDisplay() {
        miniDisplay.setText("");
    }

    /**
     * @param value to be displayed on the display.
     */
    protected void setDisplay(String value) {
        if (value.endsWith(".0")) {
            final int dotIndex = value.indexOf('.');
            display.setText(value.substring(0, dotIndex));
        } else {
            display.setText(value);
        }
    }

    /**
     * @param value to be displayed on the mini display.
     */
    protected void setMiniDisplay(String value) {
        if (value.endsWith(".0")) {
            final int dotIndex = value.indexOf('.');
            miniDisplay.setText(value.substring(0, dotIndex));
        } else {
            miniDisplay.setText(value);
        }
    }

    /**
     * @return the displayed text on screen.
     */
    protected String onDisplay() {
        final Editable temp = display.getText();

        return temp == null ? "" : temp.toString();
    }

    /**
     * @param v button
     * @return true if v is the equals button.
     */
    protected boolean isEquals(View v) {
        return v.getId() == R.id.equalBtn;
    }

    /**
     * @return true if the user has not selected an operation.
     */
    protected boolean hasSelectedOp() {
        return selectedBtn != -1;
    }

    /**
     * Resets the selected btn.
     */
    protected void resetBtn() {
        setSelectedBtn(-1);
    }

    /**
     * @param newBtn ID
     */
    protected void setSelectedBtn(int newBtn) {
        if (hasSelectedOp()) {
            Button prev = findViewById(selectedBtn);
            prev.setTextColor(Color.parseColor("#ffffff"));
            prev.setBackgroundColor(Color.parseColor("#800080"));
        }

        selectedBtn = newBtn;

        if (hasSelectedOp()) {
            Button current = findViewById(selectedBtn);
            current.setBackgroundColor(Color.parseColor("#ffffff"));
            current.setTextColor(Color.parseColor("#800080"));
        }
    }

    /**
     * Processes calculator input based on state of the expression.
     *
     * @param view pressed button
     */
    public void handleOperation(View view) {
        /* first input */
        if (currentExp.isEmpty()) {
            currentExp = onDisplay(); // Get from user
            setMiniDisplay(currentExp); // Updated mini-screen
            clearDisplay();

            if (!isEquals(view)) { // Initial btn click
                setSelectedBtn(view.getId());
            }
        } else if (isEquals(view)) { // User pressed equals btn
            if (!onDisplay().isEmpty() && hasSelectedOp()) {
                // If the display is not empty & the user has selected an operator.
                currentExp += getOperator(selectedBtn) + onDisplay();
            }

            setMiniDisplay(currentExp); // update mini-display with current expression

            try {
                setDisplay(evaluateExpression(currentExp).toString()); // calculate expression
                currentExp = onDisplay(); // current expression is the formatted value
            } catch (ArithmeticException e) { // Failed evaluation
                setDisplay("Error");
                currentExp = "";
                return;
            }

            resetBtn();
            hasEqualed = true;
        } else { // User pressed any other action btn
            // Initial btn click, this prevents lack of operator when the user presses the equals
            // Btn initially.
            if (!hasSelectedOp()) {
                setSelectedBtn(view.getId());
            }

            // Clear previous result
            if (hasEqualed) {
                clearDisplay();
            }

            if (!onDisplay().isEmpty()) {
                // An operation has been selected with a value
                currentExp += getOperator(selectedBtn) + onDisplay();
            }

            // Update mini-display
            setMiniDisplay(currentExp);
            clearDisplay();

            // set back to false, any equalities are done
            hasEqualed = false;

            setSelectedBtn(view.getId()); // set selected operator
        }
    }
}
