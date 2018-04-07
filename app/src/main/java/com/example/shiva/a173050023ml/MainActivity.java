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
import java.util.List;

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

    public String convertLineToLibSVM(String csvLine)
    {
        String vals[] = csvLine.split(",");
        String label;
        if(vals[vals.length - 1].equals("walking"))
            label = "+1";
        else
            label = "-1";
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        for(int i = 3; i < vals.length - 1; i++)
            sb.append(" " + (i - 2) + ":" + vals[i]);
        return sb.toString();
    }

    public String generateTrainingFile(Uri uri)
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
            while ((line = br.readLine()) != null)
            {
                readings.add(new AccelReading(line));
            }
            int windowSize = 50;
            int n = readings.size();
            int i = 0;
            double avg_x = 0, avg_y = 0 , avg_z = 0;
            while( i < n)
            {
                if(i % (windowSize - 1) == 0 && i > 0)
                {
                    avg_x = avg_x/windowSize;
                    avg_y = avg_y/windowSize;
                    avg_z = avg_z/windowSize;
                    double var_x = 0, var_y = 0, var_z = 0;
                    for(int j = i - windowSize + 1; j <= i; j++)
                    {
                        var_x = Math.pow((readings.get(i).x - avg_x), 2);
                        var_y = Math.pow((readings.get(i).y - avg_y), 2);
                        var_z = Math.pow((readings.get(i).z - avg_z), 2);
                    }
                    var_x /= windowSize;
                    var_y /= windowSize;
                    var_z /= windowSize;
                    StringBuilder sb = new StringBuilder();
                    if(readings.get(i).label.equals("walking"))
                        sb.append("+1 ");
                    else
                        sb.append("-1 ");
                    sb.append("1:" + avg_x + " 2:" + avg_y + " 3:" + avg_z + " 4:" + var_x + " 5:" + var_y + " 6:" + var_z);
                    writer.write(sb.toString());
                    writer.write("\n");
                    avg_x = avg_y = avg_z = 0.0;
                }
                avg_x += readings.get(i).x;
                avg_y += readings.get(i).y;
                avg_z += readings.get(i).z;
                i += windowSize - 1;
            }
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
                //String filePath = generateTrainingFile(data.getData());
                //Log.v("This is the fp", filePath);
                LibSVM svm = LibSVM.getInstance();
                String folderPath = "sdcard/ActML" + File.separator;
                //svm.scale(filePath, folderPath + "scaled_file");
                //svm.train("-t 2 "/* svm kernel */ + folderPath + "scaled_file " + folderPath + "model");
                svm.predict(folderPath + "testingFile.libSVM " + folderPath + "model " + folderPath + "result");
            }
            else{
                Toast.makeText(getApplicationContext(), "Please select the training file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
