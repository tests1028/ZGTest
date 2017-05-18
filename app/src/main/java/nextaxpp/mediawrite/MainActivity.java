package nextaxpp.mediawrite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;

import nextapp.mediafile.MediaFile;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
/** Horrible test app for MediaFile. */
public class MainActivity extends Activity {

    private static final String TAG = "MediaWrite";
    private static final String TEST_PATH = "/storage/usbotg/MediaWriteTest";
    
    private  File baseDir = null ;
    
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private LinearLayout logLayout;
    private ScrollView logScroll;
    
    private String getFileState(File file) {
        if (!file.exists()) {
            return("[DOES NOT EXIST] " + file.getAbsolutePath());            
        }

        StringBuilder out = new StringBuilder();
        
        if (file.isDirectory()) {
            out.append("[DIR] ");
        } else {
            out.append("[FILE] ");
        }
        out.append(file.getAbsolutePath());
        out.append(' ');
        
        if (file.isDirectory()) {
            String[] items = file.list();
            out.append(" Items: ");
            out.append(items == null ? 0 : items.length);
        }
        
        long lastModified = file.lastModified();
        if (lastModified != 0) {
            out.append(" Last Modified: ");
            out.append(dateFormat.format(lastModified));
            out.append(' ');
            out.append(timeFormat.format(lastModified));
        }
        
        if (!file.isDirectory()) {
            out.append(" Size: " + file.length());
        }
        
        return out.toString();
    }
    
    private void log(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        logLayout.addView(text);
        logScroll.post(new Runnable() {
            
            @Override
            public void run() {
                logScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            baseDir = new File(TEST_PATH);
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        dateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);
        
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        
        logScroll = new ScrollView(this);
        logScroll.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        contentLayout.addView(logScroll);
        
        logLayout = new LinearLayout(this);
        logLayout.setOrientation(LinearLayout.VERTICAL);
        logScroll.addView(logLayout);
        
        LinearLayout controlsLayout = new LinearLayout(this);
        contentLayout.addView(controlsLayout);

        Button createButton = new Button(this);
        createButton.setText("Create");
        createButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                runCreateTests();
            }
        });
        controlsLayout.addView(createButton);
        
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                runDeleteTests();
            }
        });
        controlsLayout.addView(deleteButton);
        
        setContentView(contentLayout);
        
        log("Welcome to the nextapp.mediafile test app."); 
        
        testInit();
    }
    
    private void runCreateTests() {
        try {
            log("---------------------------------");
            log("Create Test");
            log("---------------------------------");
            try {
                if (!baseDir.exists() || !baseDir.isDirectory()) {
                    log("Base directory does not exist: aborting");
                    return;
                }
                testCreateFile("text.txt", "text.txt");
                testMkDir("Stuff");
                testCreateFile("Stuff/text.txt", "text.txt");
//                testCreateFile("Stuff/FX.png", "FX.png");
            } catch (IOException ex) {
                ex.printStackTrace();
                log("Create test failed." + ex);
                Log.w(TAG, "Failed to create directory.", ex);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void runDeleteTests() {
        log("---------------------------------");
        log("Delete Test"); 
        log("---------------------------------");
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            log("Base directory does not exist: aborting");
            return;
        }
        
        try {
            // Delete File "Hello.txt"
            testDeleteFile("Hello.txt");
            testDeleteFile("Stuff/Hi.txt");
            testDeleteFile("Stuff/FX.png");
            testDeleteFile("Stuff");
        } catch (IOException ex) {
            log("Create test failed." + ex);
            Log.w(TAG, "Failed to create directory.", ex);
        }
    }
    
    private void testCreateFile(String path, String assetPath) 
    throws IOException {
        // Create File "Hello.txt"
        File file = new File(baseDir, path);
        log("Writing trivial text file: " + file.getAbsolutePath());
        log("* Prewrite state: " + getFileState(file));
        MediaFile mf = new MediaFile(getContentResolver(), file);
        InputStream in = getAssets().open(assetPath);
        OutputStream out = mf.write();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        log("* Postwrite state: " + getFileState(file));
    }
    
    private void testDeleteFile(String path)
    throws IOException {
        try {
        File file = new File(baseDir, path);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "not exist", Toast.LENGTH_SHORT).show();
            file.createNewFile();
        }
        log("Deleting trivial text file: " + file.getAbsolutePath());
        log("* Prewrite state: " + getFileState(file));
        MediaFile mf = new MediaFile(getContentResolver(), file);
        boolean result = mf.delete();
        log("* delete() result: " + result);
        log("* Postwrite state: " + getFileState(file));
    }catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void testInit() {
        ContentResolver cr = getContentResolver();
        if (baseDir.exists()) {
            if (baseDir.isDirectory()) {
                log("Base directory already exists: " + baseDir.getAbsolutePath());
            } else {
                log("Base directory exists, but is a file: " + baseDir.getAbsolutePath());
                log("All operations will fail.");
            }
            return;
        }
        MediaFile mf = new MediaFile(cr, baseDir);
        try {
            boolean created = mf.mkdir();
            if (created) {
                log("Successfully created base directory: " + baseDir.getAbsolutePath());
            } else {
                log("Failed to create base directory: " + baseDir.getAbsolutePath());
            }
        } catch (IOException ex) {
            log("Failed to create directory." + ex);
            Log.w(TAG, "Failed to create directory.", ex);
        }
    }
    
    private void testMkDir(String path) 
    throws IOException {
        File file = new File(baseDir, path);
        log("MkDir: " + file.getAbsolutePath());
        log("* Prewrite state: " + getFileState(file));
        MediaFile mf = new MediaFile(getContentResolver(), file);
        boolean mkdir = mf.mkdir();
        log("* mkdir() result: " + mkdir);
        log("* Postwrite state: " + getFileState(file));
    }
}
