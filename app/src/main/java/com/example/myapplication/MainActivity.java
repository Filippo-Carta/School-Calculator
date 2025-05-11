package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startButton.setOnClickListener(v -> {
            String mode = binding.modeSpinner.getSelectedItem().toString();
            String timeStr = binding.timeEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            if (timeStr.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter time and password", Toast.LENGTH_SHORT).show();
                return;
            }

            int minutes = Integer.parseInt(timeStr);
            Intent intent;


            switch (mode.toLowerCase()) {
                case "basic":
                    intent = new Intent(MainActivity.this, CalculatorActivity.class);
                    break;
                case "scientific":
                    intent = new Intent(MainActivity.this, SciCalcActivity.class);
                    break;
                default:
                    intent = new Intent(MainActivity.this, CalculatorActivity.class);
                    break;
            }

            intent.putExtra("MODE", mode);
            intent.putExtra("TIME", minutes);
            intent.putExtra("PASS", password);
            startActivity(intent);
        });

        startLockTask();
    }

    public native String stringFromJNI();

    static {
        System.loadLibrary("myapplication");
    }
}
