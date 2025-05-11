package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import android.content.pm.ActivityInfo;

public class SciCalcActivity extends Activity {

    TextView resultText, calculationText;
    String currentInput = "";
    String currentCalculation = "";
    double memory = 0;
    double lastAnswer = 0;
    boolean isAppBlocked = false;
    Handler handler = new Handler();
    long timerDuration = 60000;
    String correctPassword = "";
    private MediaPlayer alertPlayer;
    private AudioManager audioManager;

    public void lockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scientificcalc);
        lockOrientation();
        hideSystemUI();

        correctPassword = getIntent().getStringExtra("PASS");
        int receivedTime = getIntent().getIntExtra("TIME", 60);
        timerDuration = receivedTime * 60000L;

        resultText = findViewById(R.id.resultText);
        calculationText = findViewById(R.id.calculationText);

        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot};
        String[] numberVals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."};
        for (int i = 0; i < numberIds.length; i++) {
            setNumberButtonClickListener(findViewById(numberIds[i]), numberVals[i]);
        }

        setOpButton(R.id.btnAdd, "+");
        setOpButton(R.id.btnSubtract, "-");
        setOpButton(R.id.btnMultiply, "*");
        setOpButton(R.id.btnDivide, "/");
        setOpButton(R.id.btnOpenParen, "(");
        setOpButton(R.id.btnCloseParen, ")");

        findViewById(R.id.btnEquals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.btnAC).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnSign).setOnClickListener(v -> toggleSign());
        findViewById(R.id.btnPercent).setOnClickListener(v -> applyPercent());
        findViewById(R.id.btnBackspace).setOnClickListener(v -> backspace());

        findViewById(R.id.btnPi).setOnClickListener(v -> appendToInput(String.valueOf(Math.PI)));
        findViewById(R.id.btnAns).setOnClickListener(v -> appendToInput(String.valueOf(lastAnswer)));
        findViewById(R.id.btnSquare).setOnClickListener(v -> applyUnary(Math::pow, 2));
        findViewById(R.id.btnCube).setOnClickListener(v -> applyUnary(Math::pow, 3));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> applyUnary(Math::sqrt));
        findViewById(R.id.btnCbrt).setOnClickListener(v -> applyUnary(Math::cbrt));
        findViewById(R.id.btnReciprocal).setOnClickListener(v -> applyUnary(x -> 1 / x));
        findViewById(R.id.btnFactorial).setOnClickListener(v -> applyFactorial());
        findViewById(R.id.btnPower).setOnClickListener(v -> appendToInput("^"));
        findViewById(R.id.btnRootY).setOnClickListener(v -> appendToInput("yroot"));
        findViewById(R.id.btnExp).setOnClickListener(v -> appendToInput("E"));
        findViewById(R.id.btnMPlus).setOnClickListener(v -> {
            try {
                memory += Double.parseDouble(currentInput);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, "Invalid input for M+", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnMMinus).setOnClickListener(v -> {
            try {
                memory -= Double.parseDouble(currentInput);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, "Invalid input for M−", Toast.LENGTH_SHORT).show();
            }
        });
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        alertPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
        alertPlayer.setLooping(true);
        startTimer();
    }

    private void hideSystemUI() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void startTimer() {
        handler.postDelayed(this::blockApp, timerDuration);
    }

    private void blockApp() {
        isAppBlocked = true;
        setAllButtons(false);
        if (alertPlayer != null) {
            alertPlayer.start();
        }
        showPasswordDialog();
    }


    private void unlockApp() {
        isAppBlocked = false;
        setAllButtons(true);
        Toast.makeText(this, "App Unlocked!", Toast.LENGTH_SHORT).show();
    }

    private void setAllButtons(boolean enabled) {
        int[] allButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot,
                R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide, R.id.btnEquals, R.id.btnAC
        };
        for (int id : allButtonIds) {
            Button b = findViewById(id);
            b.setEnabled(enabled);
        }
    }

    private void setNumberButtonClickListener(Button button, String value) {
        button.setOnClickListener(v -> appendToInput(value));
    }

    private void setOpButton(int id, String op) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> appendToInput(op));
    }

    private void appendToInput(String value) {
        if (!isAppBlocked) {
            currentInput += value;
            calculationText.setText(currentCalculation + currentInput);
        }
    }

    private void toggleSign() {
        if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
        else currentInput = "-" + currentInput;
        calculationText.setText(currentCalculation + currentInput);
    }

    private void applyPercent() {
        try {
            double value = Double.parseDouble(currentInput);
            currentInput = String.valueOf(value / 100.0);
            calculationText.setText(currentCalculation + currentInput);
        } catch (NumberFormatException ignored) {}
    }

    private void backspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            calculationText.setText(currentCalculation + currentInput);
        }
    }

    private void applyUnary(java.util.function.DoubleUnaryOperator op) {
        try {
            double value = Double.parseDouble(currentInput);
            currentInput = String.valueOf(op.applyAsDouble(value));
            calculationText.setText(currentCalculation + currentInput);
        } catch (NumberFormatException ignored) {}
    }

    private void applyUnary(java.util.function.DoubleBinaryOperator op, double exponent) {
        try {
            double value = Double.parseDouble(currentInput);
            currentInput = String.valueOf(op.applyAsDouble(value, exponent));
            calculationText.setText(currentCalculation + currentInput);
        } catch (NumberFormatException ignored) {}
    }

    private void applyFactorial() {
        try {
            int n = Integer.parseInt(currentInput);
            long result = 1;
            for (int i = 2; i <= n; i++) result *= i;
            currentInput = String.valueOf(result);
            calculationText.setText(currentCalculation + currentInput);
        } catch (NumberFormatException ignored) {}
    }

    private void calculateResult() {
        try {
            String expression = currentCalculation + currentInput;
            expression = expression.replace("\u00D7", "*").replace("\u00F7", "/").replace("^", "**");
            double result = evaluateExpression(expression);
            resultText.setText(String.valueOf(result));
            lastAnswer = result;
            currentCalculation = "";
            currentInput = String.valueOf(result);
        } catch (Exception e) {
            resultText.setText("Error");
        }
    }

    private double evaluateExpression(String expression) {
        return new ExpressionEvaluator().evaluate(expression);
    }

    private void clearAll() {
        currentInput = "";
        currentCalculation = "";
        resultText.setText("0");
        calculationText.setText("");
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App Locked");
        builder.setMessage("Enter password to unlock:");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Unlock", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        Button unlock = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        unlock.setOnClickListener(v -> {
            if (input.getText().toString().equals(correctPassword)) {
                unlockApp();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                input.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isAppBlocked) {
            showPasswordDialog();
        }
        showPasswordDialog();
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!isAppBlocked) {
            alertPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (alertPlayer != null) {
            alertPlayer.release();
        }
        super.onDestroy();
    }

    public class ExpressionEvaluator {
        public double evaluate(String expression) {

            expression = expression.replaceAll("\\s", "");
            return evalExpression(expression);
        }
        private String insertImplicitMultiplication(String expr) {

            expr = expr.replaceAll("(\\d)\\(", "$1*(");

            expr = expr.replaceAll("\\)(\\d)", ")*$1");
            expr = expr.replaceAll("\\)\\(", ")*(");
            return expr;
        }

        private double evalExpression(String expression) {
            expression = insertImplicitMultiplication(expression);

            expression = expression.replaceAll("\\s", "");


            while (expression.contains("(")) {

                int start = expression.lastIndexOf("(");
                int end = expression.indexOf(")", start);
                String subExpression = expression.substring(start + 1, end);

                double subResult = evalExpression(subExpression);


                expression = expression.substring(0, start) + subResult + expression.substring(end + 1);
            }


            if (expression.contains("+")) {
                String[] operands = expression.split("\\+");
                return evalExpression(operands[0]) + evalExpression(operands[1]);
            } else if (expression.contains("-")) {
                String[] operands = expression.split("(?<=\\d)-");
                if (operands.length > 1) {
                    return evalExpression(operands[0]) - evalExpression(operands[1]);
                }
            }


            if (expression.contains("*")) {
                String[] operands = expression.split("\\*");
                return evalExpression(operands[0]) * evalExpression(operands[1]);
            } else if (expression.contains("/")) {
                String[] operands = expression.split("/");
                return evalExpression(operands[0]) / evalExpression(operands[1]);
            } else if (expression.contains("^")) {
                String[] operands = expression.split("\\^");
                return Math.pow(evalExpression(operands[0]), evalExpression(operands[1]));
            }


            return Double.parseDouble(expression);
        }
    }
}
