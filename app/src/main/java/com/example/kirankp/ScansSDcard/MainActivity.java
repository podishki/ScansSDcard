package com.example.kirankp.ScansSDcard;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;
    private static final String KIRAN = "KIRAN";
    private Map<String, Integer> fileExtension = new HashMap<String, Integer>();
    private Map<Integer, String> fileExtensionSorted = new TreeMap<Integer, String>(new SortReverse());
    private Map<Integer, String> fileNameSizeHashSorted = new TreeMap<Integer, String>(new SortReverse());
    private ProgressDialog mProgressDialog = null;
    Button startbtn, stopbtn;
    FileScanTask task;
    private TextView fileName, fileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new ProgressDialog(this);
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(MainActivity.this, "I know you said no, but I'm asking again", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
            return;
        }
        fileName = (TextView) findViewById(R.id.filenametv);
        fileSize = (TextView) findViewById(R.id.filesizetv);
        startbtn = (Button) findViewById(R.id.startbtn);
        stopbtn = (Button) findViewById(R.id.stopbtn);
        task = new FileScanTask();
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setMessage("Scanning files in SD Card");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog = mProgressDialog.show(
                        MainActivity.this,
                        "Dialog",
                        "Scanning files in SD Card",
                        true,
                        true,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (task != null && task.isCancelled() == false)
                                    task.cancel(true);

                            }
                        }
                );
                if (task == null || task.isCancelled() == true) {
                    task = new FileScanTask();
                }
                task.execute("Param 1", "Param 2", "Param 3");
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(KIRAN, "Cancel");
                if (task != null) {
                    task.cancel(true);
                }
            }
        });
    }

    /**
     * List all files from a directory and its subdirectories
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission was granted!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission was denied!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class FileScanTask extends AsyncTask<String, String, Map> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Map doInBackground(String... params) {
            if (!isCancelled()) {
                String filepath = Environment.getExternalStorageDirectory().toString();
                listFilesAndFilesSubDirectories(filepath);
            }
            return fileNameSizeHashSorted;
        }

        public void listFilesAndFilesSubDirectories(String directoryName) {

            File directory = new File(directoryName);
            //get all the files from a directory
            File[] fList = directory.listFiles();
            for ( File file : fList ) {
                if (file.isFile()) {
                    //Log.d(KIRAN, "listFilesAndFilesSubDirectories: "+ fileExt(file));
                    int count = fileExtension.containsKey(fileExt(file)) ? fileExtension.get(fileExt(file)) : 0;
                    fileExtension.put(fileExt(file), count + 1);

                    fileNameSizeHashSorted.put((int) file.length(), file.getName());
                } else if (file.isDirectory()) {
                    listFilesAndFilesSubDirectories(file.getAbsolutePath());
                }
            }
        }

        private String fileExt(File file) {
            String extension = null;
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = file.getName().substring(i + 1);
            }
            return extension;
        }

        @Override
        protected void onPostExecute(Map result) {
            mProgressDialog.dismiss();
            mProgressDialog.cancel();
            int i = 0;
            int j = 0;
            fileName.append("\nFile Name\n\n");
            fileSize.append("\nFile Size\n\n");

            for ( Map.Entry<Integer, String> entry1 : fileNameSizeHashSorted.entrySet() ) {
                fileName.append(entry1.getValue() + "\n");
                fileSize.append(entry1.getKey() + "\n");
                i += 1;
                if (i > 10)
                    break;
            }
            fileName.append("\nFile Extension\n\n");
            fileSize.append("\nFrequency\n\n");

            for ( Map.Entry<String, Integer> entry2 : fileExtension.entrySet() ) {

                fileExtensionSorted.put(entry2.getValue(), entry2.getKey());
            }
            for ( Map.Entry<Integer, String> entry3 : fileExtensionSorted.entrySet() ) {

                fileName.append(entry3.getValue() + "\n");
                fileSize.append(entry3.getKey() + "\n");

                j += 1;
                if (j > 5)
                    break;
            }
            task = null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Log.d(KIRAN, "onProgress:[" + values[0] + "]");
        }

    }
}

class SortReverse implements Comparator<Integer> {
    @Override
    public int compare(Integer str2, Integer str1) {
        return str1.compareTo(str2);
    }
}
