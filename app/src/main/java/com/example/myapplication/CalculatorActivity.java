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

public class CalculatorActivity extends Activity {

    TextView resultText, calculationText;
    String currentInput = "";
    String currentCalculation = "";
    boolean isAppBlocked = false;
    Handler handler = new Handler();
    long timerDuration = 60000;  // 1 minute timer
    String correctPassword = "";

    private MediaPlayer alertPlayer;
    private AudioManager audioManager;

    public void lockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Locks to portrait mode
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Uncomment this line to lock to landscape mode
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        lockOrientation();
        hideSystemUI();

        correctPassword = getIntent().getStringExtra("PASS");
        int receivedTime = getIntent().getIntExtra("TIME", 60);
        timerDuration = receivedTime * 60000L;

        resultText = findViewById(R.id.resultText);
        calculationText = findViewById(R.id.calculationText);

        // Number Buttons
        int[] btnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };
        String[] btnValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."};
        for (int i = 0; i < btnIds.length; i++) {
            setNumberButtonClickListener(findViewById(btnIds[i]), btnValues[i]);
        }

        // Operations Buttons
        setOpButton(R.id.btnAdd, "+");
        setOpButton(R.id.btnSubtract, "-");
        setOpButton(R.id.btnMultiply, "×");
        setOpButton(R.id.btnDivide, "÷");

        findViewById(R.id.btnEquals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.btnAC).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnSign).setOnClickListener(v -> toggleSign());
        findViewById(R.id.btnPercent).setOnClickListener(v -> applyPercent());

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        alertPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
        alertPlayer.setLooping(true);

        startTimer();
    }

    private void hideSystemUI() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    private void startTimer() {
        handler.postDelayed(this::blockApp, timerDuration);
    }

    private void blockApp() {
        isAppBlocked = true;
        setAllButtons(false);
        alertPlayer.start(); // Start ringing when the app is blocked
        showPasswordDialog();
    }

    private void unlockApp() {
        isAppBlocked = false;
        setAllButtons(true);
        alertPlayer.stop(); // Stop the ringing when the app is unlocked
        Toast.makeText(this, "App Unlocked!", Toast.LENGTH_SHORT).show();
    }

    private void setAllButtons(boolean enabled) {
        int[] allButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply,
                R.id.btnDivide, R.id.btnEquals, R.id.btnAC
        };
        for (int id : allButtonIds) {
            Button b = findViewById(id);
            b.setEnabled(enabled);
        }
    }

    private void setNumberButtonClickListener(Button button, String value) {
        button.setOnClickListener(v -> {
            if (!isAppBlocked) {
                currentInput += value;
                calculationText.setText(currentCalculation + currentInput);
            }
        });
    }

    private void setOpButton(int id, String op) {
        Button btn = findViewById(id);
        btn.setOnClickListener(v -> {
            if (!isAppBlocked) {
                currentCalculation += currentInput + " " + op + " ";
                currentInput = "";
                calculationText.setText(currentCalculation);
            }
        });
    }

    private void calculateResult() {
        if (!isAppBlocked) {
            try {
                String expression = currentCalculation + currentInput;
                expression = expression.replace("÷", "/").replace("×", "*");
                double result = eval(expression);
                resultText.setText(String.valueOf(result));
                currentCalculation = "";
                currentInput = String.valueOf(result);
            } catch (Exception e) {
                resultText.setText("Error");
            }
        }
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

    private void toggleSign() {
        if (currentInput.isEmpty()) return;

        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }

        calculationText.setText(currentCalculation + currentInput);
    }

    private void applyPercent() {
        if (currentInput.isEmpty()) return;

        try {
            double value = Double.parseDouble(currentInput);
            value = value / 100.0;
            currentInput = String.valueOf(value);
            calculationText.setText(currentCalculation + currentInput);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input for percent", Toast.LENGTH_SHORT).show();
        }
    }

    private double eval(final String str) {
        return new Object() {
            int pos = -1, c;

            void nextChar() {
                c = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (c == ' ') nextChar();
                if (c == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) c);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((c >= '0' && c <= '9') || c == '.') {
                    while ((c >= '0' && c <= '9') || c == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) c);
                }
                return x;
            }
        }.parse();
    }

    @Override
    public void onBackPressed() {
        if (!isAppBlocked) {
            showPasswordDialog();
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!isAppBlocked) {
            alertPlayer.start();  // Start ringing when user minimizes app or switches to another app
        }
    }

    @Override
    protected void onDestroy() {
        if (alertPlayer != null) {
            alertPlayer.release();
        }
        super.onDestroy();
    }
}
