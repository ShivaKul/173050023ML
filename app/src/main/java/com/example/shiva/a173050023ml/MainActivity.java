package com.example.shiva.a173050023ml;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import umich.cse.yctung.androidlibsvm.LibSVM;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_TRAINING_FILE_REQUEST_CODE= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTrainingButtonClicked(View view)
    {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICK_TRAINING_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_TRAINING_FILE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                TextView textView = findViewById(R.id.training_file_textview);
                textView.setText((data.getDataString()));
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())));
                    //Toast.makeText(getApplicationContext(), br.readLine() + "Hello", Toast.LENGTH_LONG).show();
                    textView.setText(br.readLine() + "Hello");
                } catch (java.io.IOException e) {
                    Log.v("ML ", "IOWrong");
                    e.printStackTrace();
                }


            }
            else{
                Toast.makeText(getApplicationContext(), "Please select the training file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
