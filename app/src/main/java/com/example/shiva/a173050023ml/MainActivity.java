package com.example.shiva.a173050023ml;

import android.content.Intent;
import android.net.Uri;
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
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import umich.cse.yctung.androidlibsvm.LibSVM;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_TRAINING_FILE_REQUEST_CODE= 0;
    private static final int PICK_TESTING_FILE_REQUEST_CODE= 1;

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

    public void onTestingButtonClicked(View view)
    {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICK_TESTING_FILE_REQUEST_CODE);
    }


    public String convertAccelToString(ArrayList<AccelReading> readings, int i, int len)
    {
        String label;
        if(readings.get(i).label.equals("walking"))
            label = "+1";
        else
            label = "-1";
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        double var_x = 0, mean_x = 0, var_y = 0, mean_y = 0, var_z = 0, mean_z = 0;
        int n = readings.size(), j = 0;
        while(j < len && j + i < n)
        {
            mean_x += readings.get(i + j).x;
            mean_y += readings.get(i + j).y;
            mean_z += readings.get(i + j).z;
            j++;
        }
        mean_x /= j;
        mean_y /= j;
        mean_z /= j;
        j = 0;
        while(j < len && j + i < n)
        {
            var_x += Math.pow((readings.get(i + j).x - mean_x), 2);
            var_y += Math.pow((readings.get(i + j).y - mean_y), 2);
            var_z += Math.pow((readings.get(i + j).z - mean_z), 2);
            j++;
        }
        var_x /= j;
        var_y /= j;
        var_z /= j;
        sb.append(" 1:" + (var_x + var_y + var_z));
        return sb.toString();
    }

    public String generateTrainingFile(Uri uri, int windowSize)
    {
        String filePath = null;
        File folder = new File(getApplicationContext().getFilesDir() + File.separator + "TrainingFiles");
        if (!folder.exists())
        {
            if (!folder.mkdir())
            {
                return null;
            }
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            filePath = folder.toString() + File.separator + ts + ".libSVM";
            FileWriter writer = new FileWriter(filePath);
            String line;
            br.readLine();
            br.readLine();
            ArrayList<AccelReading> readings = new ArrayList<>();
            //Log.v("ArrayList", "ArrayList created");
            while ((line = br.readLine()) != null)
            {
                //Log.v("AddingLines", "Current line is " + line);
                readings.add(new AccelReading(line));
            }
            //Log.v("Size of file", "Size of trainingCSV is " + readings.size());
            int n = readings.size();
            ArrayList<String> strList = new ArrayList<>();
            for(int i = 0; i < n; i += windowSize)
            {
                //Log.v("Value of i", "i is now " + i);
                strList.add(convertAccelToString(readings, i, windowSize) + "\n");
            }
            Collections.shuffle(strList);
            n = strList.size();
            for(int i = 0; i < n; i++)
                writer.write(strList.get(i));
            writer.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_TRAINING_FILE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                TextView textView = findViewById(R.id.training_file_textview);
                textView.setText((data.getDataString()));
                //Log.v("Point1", "Now going to call generateTrainingFile");
                String filePath = generateTrainingFile(data.getData(), 10);
                //Log.v("Point2", "Now returning from generateTrainingFile");
                //Log.v("This is the fp", filePath);
                LibSVM svm = LibSVM.getInstance();
                String folderPath = "sdcard/ActML" + File.separator;
                svm.scale(filePath, folderPath + "scaled_file");
                svm.train("-t 2 " + folderPath + "scaled_file " + folderPath + "model");}
            else{
                Toast.makeText(getApplicationContext(), "Please select the training file", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == PICK_TESTING_FILE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                TextView textView = findViewById(R.id.testing_file_textview);
                textView.setText(R.string.app_name);
                LibSVM svm = LibSVM.getInstance();
                String folderPath = "sdcard/ActML" + File.separator;
                svm.predict(folderPath + "testingFile.libSVM " + folderPath + "model " + folderPath + "result");
            }
            else{
                Toast.makeText(getApplicationContext(), "Please select the testing file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
