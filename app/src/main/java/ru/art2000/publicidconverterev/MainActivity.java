package ru.art2000.publicidconverterev;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends Activity {

    MenuItem sourceItem;
    MenuItem portItem;

    ProgressDialog progressDialog;

    SharedPreferences prefs;
    EditText sourcePublicET;
    EditText portPublicET;
    EditText sourceSmaliFileET;
    EditText sourceSmaliFolderET;

    Button openFile;
    Button openFolder;
    ImageButton openLog;

    CheckBox includeSubfolders;

    RadioButton fileRB;
    RadioButton folderRB;
    RadioGroup radioGroup;

    Boolean overwrite;
    Boolean overwriteMis;
    Boolean skipModified;
    Boolean skipMissingModified;
    Boolean sourceFramework;
    Boolean portFramework;

    String smaliType = "file";

    private static final int SOURCE_XML = 0;
    private static final int PORT_XML = 1;
    private static final int SOURCE_SMALI = 2;
    private static final int SOURCE_SMALI_FOLDER = 3;

    private File sourcePublicXml;
    private File portPublicXml;
    private File sourceSmaliFile;
    private File sourceSmaliFolder;

    final String xmlFilter = "xml";
    final String smaliFilter = "smali";

    private static final String DEFAULT_SEARCH_STRING = "0x7f";
    private static final String FRAMEWORK_SEARCH_STRING = "0x1";
    private static final String SOURCE_PUBLIC_XML_FILENAME = "sourcepublic";
    private static final String PORT_PUBLIC_XML_FILENAME = "portpublic";
    private static final String SOURCE_SMALI_FILENAME = "sourcesmali";
    private static final String SOURCE_SMALI_FOLDERNAME = "sourcesmalifolder";

    private BufferedWriter logWriter = null;
    private String somethingMissing = "Btw smthing is MISSING... ";
    private String firstLine = "# Modified by Public ID Converter Ev.";
    private String missingFirstLine = "# Modified by Public ID Converter Ev. Contains MISSING";
    private boolean folder = false;
    private boolean folderMis = false;
    private int frameworkIdLength = 9;
    private int usualIdLength = 10;
    private int currentLength = usualIdLength;
    private ArrayList<String> originalIds = new ArrayList<>();
    private ArrayList<String> nameType = new ArrayList<>();
    private int converted = 0;
    private boolean found = false;
    private File findingIDsLog;
    private File folderConversionLog;
    private File androidFolder;
    private int filesInFolder = 0;
    private int filesWithIDs = 0;
    private boolean conversion = false;
    private LinearLayout fileLayout;
    private LinearLayout folderLayout;
    private boolean processing = false;


    @Override
    public void onBackPressed() {
        if(!processing)
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.d("tmp left margin", String.valueOf(((ViewGroup.MarginLayoutParams)
                findViewById(R.id.tmp).getLayoutParams()).leftMargin));

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isCheckable()) {
                Boolean checked;
                switch (item.getItemId()) {
                    case R.id.overwrite:
                        checked = overwrite;
                        break;
                    case R.id.overwriteMis:
                        checked = overwriteMis;
                        break;
                    case R.id.skipModified:
                        checked = skipModified;
                        break;
                    case R.id.skipMissingModified:
                        checked = skipMissingModified;
                        break;
                    case R.id.sourceFramework:
                        checked = sourceFramework;
                        sourceItem = item;
                        break;
                    case R.id.portFramework:
                        checked = portFramework;
                        portItem = item;
                        break;
                    default:
                        checked = false;
                        break;
                }

                item.setChecked(checked);

            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.isCheckable())
            item.setChecked(!item.isChecked());
        switch (item.getItemId()) {
            case R.id.overwrite:
                overwrite = item.isChecked();
                prefs.edit().putBoolean("overwrite", overwrite).apply();
                break;
            case R.id.overwriteMis:
                overwriteMis = item.isChecked();
                prefs.edit().putBoolean("overwriteMis", overwriteMis).apply();
                break;
            case R.id.skipModified:
                skipModified = item.isChecked();
                prefs.edit().putBoolean("skipModified", skipModified).apply();
                break;
            case R.id.skipMissingModified:
                skipMissingModified = item.isChecked();
                prefs.edit().putBoolean("skipMissingModified", skipMissingModified).apply();
                break;
            case R.id.sourceFramework:
                sourceFramework = item.isChecked();
                prefs.edit().putBoolean("sourceFramework", sourceFramework).apply();
                break;
            case R.id.portFramework:
                portFramework = item.isChecked();
                prefs.edit().putBoolean("portFramework", portFramework).apply();
                break;
            case R.id.swap:
                String tmp = sourcePublicET.getText().toString();
                sourcePublicET.setText(portPublicET.getText());
                portPublicET.setText(tmp);
                File tmpFile = sourcePublicXml;
                sourcePublicXml = portPublicXml;
                portPublicXml = tmpFile;
                sourceItem.setChecked(FilenameUtils.isFrameworkPublic(sourcePublicXml.getAbsolutePath()));
                portItem.setChecked(FilenameUtils.isFrameworkPublic(portPublicXml.getAbsolutePath()));
                setFilePreference(SOURCE_PUBLIC_XML_FILENAME, sourcePublicXml);
                setFilePreference(PORT_PUBLIC_XML_FILENAME, portPublicXml);
                break;

        }
        return super.onOptionsItemSelected(item);
    }







    private void cmdConvert() {
        if (!checkFiles())
            return;
        workWithFilesBeforeThread();
        new Thread(() -> {
            switch (smaliType) {
                case "file":
                    folder = false;
                    progressDialog.setMax(1);
                    if (findIds(sourceSmaliFile))
                        convertFile(sourceSmaliFile);
                    break;
                case "folder":
                    folder = true;
                    calcFiles(sourceSmaliFolder);
                    if (filesInFolder == 0) {
                        updateStatusBar("No smali files found in directory " + sourceSmaliFolder.getAbsoluteFile());
                        String txt = "No smali files found in directory\n" + sourceSmaliFolder.getAbsoluteFile();
                        runOnUiThread(() -> {
                            checkFilesExceptionToast(txt);
                            progressDialog.dismiss();
                        });
                        return;
                    }
                    progressDialog.setMax(filesInFolder);
                    convertFolder();
                    if (converted != 0) {
                        String last = " files successfully converted!";
                        if (folderMis)
                            last += " " + somethingMissing;
                        updateStatusBar(converted + last);
                        converted = 0;
                    } else if (filesInFolder != 0)
                        updateStatusBar("No files converted!");

                    break;
            }
            workWithFilesAfterThread();
        }).start();

    }

    private void updateStatusBar(String string) {
        if (progressDialog != null) {
            runOnUiThread(() ->
                    progressDialog.setMessage(string));
        }
        if (folder && conversion)
            try {
                logWriter.write(string);
                logWriter.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void workWithFilesBeforeThread(){
        openLog.setVisibility(View.GONE);
        setupDialog();
        folderMis = false;
        processing = true;
        if (conversion){
            updateStatusBar("Converting has started...");
            if (sourceItem.isChecked())
                currentLength = frameworkIdLength;
            else
                currentLength = usualIdLength;
        } else
            updateStatusBar("Finding IDs...");
        if (!androidFolder.exists())
            androidFolder.mkdirs();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void workWithFilesAfterThread(){
        runOnUiThread(()-> {
            if (progressDialog.getProgress() != progressDialog.getMax())
                progressDialog.setProgress(progressDialog.getMax());
        });
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        runOnUiThread(()->{
            if ((conversion && smaliType.equals("folder")) || found) {
                try {
                    openLog.setVisibility(View.VISIBLE);
                    openLog.setOnClickListener(v ->{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if (conversion)
                            intent.setDataAndType(Uri.fromFile(folderConversionLog), "text/plain");
                        else
                            intent.setDataAndType(Uri.fromFile(findingIDsLog), "text/plain");
                        startActivity(intent);
                    });
                    try {
                        logWriter.close();
                    } catch (Exception e) {
                        System.out.println("Cannot close log file writer");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            filesWithIDs = 0;
            filesInFolder = 0;
            processing = false;
            progressDialog.dismiss();

        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onFindIds() {
        if (!checkFiles())
            return;
        workWithFilesBeforeThread();
        new Thread(() -> {
            try {
                if (!findingIDsLog.exists())
                    findingIDsLog.createNewFile();
                logWriter = new BufferedWriter(new FileWriter(findingIDsLog, false));
                logWriter.write("This file is located at: ");
                logWriter.newLine();
                logWriter.write(findingIDsLog.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Cannot write log file");
            }
            switch (smaliType) {
                case "file":
                    progressDialog.setMax(1);
                    if (findIds(sourceSmaliFile))
                        cmdFind(sourceSmaliFile);
                    break;
                case "folder":
                    found = false;
                    calcFiles(sourceSmaliFolder);
                    if (filesInFolder == 0) {
                        updateStatusBar("No smali files found in directory " + sourceSmaliFolder.getAbsoluteFile());
                        String txt = "No smali files found in directory\n" + sourceSmaliFolder.getAbsoluteFile();
                        runOnUiThread(() -> {
                            checkFilesExceptionToast(txt);
                            progressDialog.dismiss();
                        });
                        return;
                    }
                    progressDialog.setMax(filesInFolder);
                    findIdsInSmaliFolder(sourceSmaliFolder);
                    if (filesWithIDs != 0)
                        updateStatusBar(filesWithIDs + " files with IDs were found in folder " + sourceSmaliFolder.getAbsoluteFile());
                    break;
            }
            workWithFilesAfterThread();
        }).start();

    }

    private void cmdFind(File cFile) {
        if (!FilenameUtils.getExtension(cFile.getName()).equalsIgnoreCase(".smali")) {
            updateStatusBar(cFile.getName() + ": Not a smali file. Skipping...");
            return;
        }
        if (nameType != null && !nameType.isEmpty()) {
            found = true;
            try {
                logWriter.newLine();
                logWriter.newLine();
                logWriter.write(cFile.getAbsoluteFile().toString());
                logWriter.newLine();
                logWriter.write("Found the following " + nameType.size() + " IDs:\n");
                for (int i = 0; i < nameType.size(); i++) {
                    logWriter.write(nameType.get(i) + " id=\"" + originalIds.get(i) + "\"");
                    logWriter.newLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean findIds(File cFile) {

        originalIds.clear();
        nameType.clear();

        runOnUiThread(()->
            progressDialog.setTitle(cFile.getAbsolutePath()));

        Scanner scanner = null;
        //Read source smali
        String sbar = cFile.getName() + ": Reading file...";
        try {
            updateStatusBar(sbar);
            scanner = new Scanner(cFile);
            while (scanner.hasNextLine()) {
                final String lineFromFile = scanner.nextLine();
                if ((skipModified && lineFromFile.equals(firstLine))
                        || (skipMissingModified && lineFromFile.equals(missingFirstLine))) {
                    updateStatusBar(cFile.getName() + " is already modified! Skipping...");
                    progressDialog.incrementProgressBy(1);
                    scanner.close();
                    return false;
                }
                if (lineFromFile.contains(getSearchString())) {
                    int index = lineFromFile.indexOf(getSearchString());
                    String publicId;
                    try {
                        if (sourceItem.isChecked())
                            publicId = "0x0" + lineFromFile.substring(index + 2, index + frameworkIdLength);
                        else
                            publicId = lineFromFile.substring(index, index + usualIdLength);
                    } catch (Exception e) {
                        System.out.println("String shorter then ID. Skipping...");
                        continue;
                    }
                    if (publicId.length() == usualIdLength) {
                        originalIds.add(publicId);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (originalIds.isEmpty()) {
            String noIds = cFile.getName() + ": No ids found in smali file";
            updateStatusBar(noIds);
            if (conversion && smaliType.equals("folder")) {
                try {
                    logWriter.newLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(()->
                progressDialog.incrementProgressBy(1));
            return false;
        }

        String name = "haha";
        //Search source public.xml
        try {
            updateStatusBar("Searching source public.xml for ids...");
            for (String id : originalIds) {
                boolean contains = false;
                int length = (int) sourcePublicXml.length();
                byte[] bytes = new byte[length];
                FileInputStream in = new FileInputStream(sourcePublicXml);
                in.read(bytes);
                String contents = new String(bytes);
                String[] lines = contents.split("\n");
                for (String line: lines){
                    if (line.contains(id)) {
                        name = line.substring(line.indexOf("type="), line.indexOf("id=") - 1);
                        contains = true;
                        break;
                    }
                }
                if (!contains)
                    name = "Not found in source public.xml";
                nameType.add(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (nameType.isEmpty()) {
            updateStatusBar("Could not find any ids in the source public.xml");
            return false;
        } else {
            updateStatusBar("Found " + nameType.size() + " IDs");
        }
        for (int i = 0; i < originalIds.size(); i++) {
            if (sourceItem.isChecked()) {
                String cur = originalIds.get(i);
                originalIds.remove(i);
                if (cur.length() == 10)
                    originalIds.add(i, "0x" + cur.substring(3, cur.length()));
                else
                    originalIds.add(i, cur);
            }
        }
        return true;
    }

    private void filesFromSmaliFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                if (includeSubfolders.isChecked())
                    filesFromSmaliFolder(fileEntry);
            } else {
                if (findIds(fileEntry))
                    convertFile(fileEntry);
            }
        }
        if (filesInFolder == 0){
            findIds(folder);
        }
    }

    private void calcFiles(File folder) {
        if (folder == null)
            return;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                if (includeSubfolders.isChecked())
                    calcFiles(fileEntry);
            } else if (FilenameUtils.getExtension(fileEntry.getName()).equalsIgnoreCase(".smali")) {
                filesInFolder++;
            }
        }
    }

    private void findIdsInSmaliFolder(File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                if (includeSubfolders.isChecked())
                    findIdsInSmaliFolder(fileEntry);
            } else {
                if (findIds(fileEntry)) {
                    filesWithIDs++;
                    cmdFind(fileEntry);
                }
            }
        }
        if (filesInFolder == 0){
            findIds(folder);
        }
    }

    private void convertFolder() {
        try {

            logWriter = new BufferedWriter(new FileWriter(folderConversionLog, false));
            logWriter.write("This file is located at: ");
            logWriter.newLine();
            logWriter.write(folderConversionLog.getAbsolutePath());
            logWriter.newLine();
            logWriter.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        filesFromSmaliFolder(sourceSmaliFolder);
        filesInFolder = 0;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertFile(File cFile) {
        if (!FilenameUtils.getExtension(cFile.getName()).equalsIgnoreCase(".smali")) {
            updateStatusBar(cFile.getName() + ": Not a smali file. Skipping...");
            return;
        }

        ArrayList<String> newIds = new ArrayList<>();
        Scanner scanner;
        //Search port public.xml
        boolean empty = true;
        boolean missing = false;
        try {
            updateStatusBar("Search port public.xml for matching ids...");
            int length = (int) portPublicXml.length();
            byte[] bytes = new byte[length];
            try (FileInputStream in = new FileInputStream(portPublicXml)) {
                in.read(bytes);
            }
            String contents = new String(bytes);
            String[] lines = contents.split("\n");

            for (String aNameType : nameType) {
                boolean contains = false;
                for (String line : lines) {
                    if (line.contains(aNameType)) {
                        String id = line.substring(line.indexOf("id=") + 4,
                                line.lastIndexOf(" ") - 1) + "    # " + aNameType;
                        if (portItem.isChecked())
                            id = "0x" + id.substring(3, id.length());
                        newIds.add(id);
                        contains = true;
                        empty = false;
                    }
                }
                if (!contains) {
                    missing = true;
                    newIds.add("    # MISSING: " + aNameType);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (empty) {
            updateStatusBar("No matching ids found in port public.xml");
            if (smaliType.equals("folder")) {
                try {
                    logWriter.newLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(()->
                progressDialog.incrementProgressBy(1));

            return;
        }

        converted++;

        StringBuilder sb = new StringBuilder();
        int line = 0;
        File newFile;
        String name_modifier = "";
        try {
            updateStatusBar("Replacing ids in smali file...");
            scanner = new Scanner(cFile);

            boolean modifiedFile = false;
            while (scanner.hasNextLine()) {
                String lineFromFile = scanner.nextLine();
                if (lineFromFile.equals(firstLine) || lineFromFile.equals(missingFirstLine))
                    modifiedFile = true;
                if (line == 0) {
                    if (missing)
                        sb.append(missingFirstLine);
                    else
                        sb.append(firstLine);
                    sb.append("\n");
                }
                line++;
                for (int i = 0; i < originalIds.size(); i++) {
                    if (lineFromFile.contains(originalIds.get(i))) {
                        if (newIds.get(i).contains("MISSING")) {
                            lineFromFile = lineFromFile.substring(0, lineFromFile.indexOf(originalIds.get(i)) + currentLength) + newIds.get(i);
                        } else {
                            lineFromFile = lineFromFile.substring(0, lineFromFile.indexOf(originalIds.get(i))) + newIds.get(i);
                        }
                        break;
                    }
                }
                String newLine = lineFromFile + "\n";
                if (line == 1) {
                    if (!modifiedFile) {
                        sb.append(newLine);
                    }
                } else
                    sb.append(newLine);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String append = cFile.getName() + ": Done! ";
        try {
            BufferedWriter writer;
            FileWriter fstream;
            if (missing) {
                if (overwriteMis) {
                    append += "File successfully OVERRIDEN. ";
                } else {
                    name_modifier = "-MODIFIED";
                    append += "MODIFIED file created. ";
                }
                append += somethingMissing;
            } else {
                if (overwrite) {
                    append += "File successfully OVERRIDEN";
                } else {
                    name_modifier = "-MODIFIED";
                    append += "MODIFIED file created";
                }
            }
            String filename = FilenameUtils.removeExtension(cFile.getName()) + name_modifier +
                    FilenameUtils.getExtension(cFile.getName());
            newFile = new File(FilenameUtils.getFullPath(cFile.getAbsolutePath()) + filename);
            fstream = new FileWriter(newFile, false); //true tells to append data.
            writer = new BufferedWriter(fstream);
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateStatusBar(append);
        if (missing)
            folderMis = true;
        if (folder || !conversion)
            try {
                logWriter.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        runOnUiThread(()->
            progressDialog.incrementProgressBy(1));
    }

    private String getSearchString() {
        if (sourceItem.isChecked())
            return FRAMEWORK_SEARCH_STRING;
        else
            return DEFAULT_SEARCH_STRING;
    }

    private void checkFilesExceptionToast(String text){
        Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
    }

    private boolean checkFiles() {
        if (sourcePublicXml == null || portPublicXml == null) {
            updateStatusBar("Please load all files!");
            checkFilesExceptionToast("Please load all files!");
            return false;
        }
        if (!sourcePublicXml.exists()) {
            updateStatusBar("Could not open source public.xml");
            checkFilesExceptionToast("Could not open source public.xml");
            return false;
        }
        if (!portPublicXml.exists()) {
            updateStatusBar("Could not open port public.xml");
            checkFilesExceptionToast("Could not open port public.xml");
            return false;
        }
        if (sourcePublicXml.isDirectory() || portPublicXml.isDirectory()) {
            updateStatusBar("Make sure you selected a file and not a directory");
            checkFilesExceptionToast("Make sure you selected a file and not a directory");
            return false;
        }

        if (smaliType.equals("folder")){
            if (sourceSmaliFolder  == null || !sourceSmaliFolder.exists()){
                updateStatusBar("Could not open smali folder");
                checkFilesExceptionToast("Could not open smali folder");
                return false;
            }
        } else {
            if (sourceSmaliFile == null || sourceSmaliFile.isDirectory() || !sourceSmaliFile.exists()) {
                updateStatusBar("Error opening source smali file");
                checkFilesExceptionToast("Error opening source smali file");
                return false;
            }
        }
        return true;
    }

    private File getFilePreference(String filename) {
        String filePath = prefs.getString(filename, null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    private void setFilePreference(String filename, File file) {
        if (file != null)
            prefs.edit().putString(filename, file.getAbsolutePath()).apply();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        overwrite = prefs.getBoolean("overwrite", false);
        overwriteMis = prefs.getBoolean("overwriteMis", false);
        skipModified = prefs.getBoolean("skipModified", true);
        skipMissingModified = prefs.getBoolean("skipMissingModified", false);
        sourceFramework = prefs.getBoolean("sourceFramework", false);
        portFramework = prefs.getBoolean("portFramework", false);


        Log.d("6 dp", String.valueOf(new ru.art2000.widgets.Utils().dip2px(getBaseContext(), 6)));

        openFile = findViewById(R.id.source_smali_file_open);
        openFolder = findViewById(R.id.source_smali_folder_open);
        openLog = findViewById(R.id.log_opener);
        fileLayout = findViewById(R.id.file_layout);
        folderLayout = findViewById(R.id.folder_layout);
        includeSubfolders = findViewById(R.id.subfolders);
        sourcePublicET = findViewById(R.id.source_public_path);
        portPublicET = findViewById(R.id.port_public_path);
        sourceSmaliFileET = findViewById(R.id.source_smali_file_path);
        sourceSmaliFolderET = findViewById(R.id.source_smali_folder_path);
        fileRB = findViewById(R.id.fileRB);
        folderRB = findViewById(R.id.folderRB);
        radioGroup = findViewById(R.id.group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            LinearLayout.LayoutParams folderParams = (LinearLayout.LayoutParams) folderLayout.getLayoutParams();
            LinearLayout.LayoutParams fileParams = (LinearLayout.LayoutParams) fileLayout.getLayoutParams();
            switch (checkedId){
                case R.id.fileRB:
                    smaliType = "file";

                    fileParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    folderParams.height = 0;

                    prefs.edit().putBoolean("folder", false).apply();
                    break;
                case R.id.folderRB:
                    smaliType = "folder";

                    folderParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    fileParams.height = 0;

                    prefs.edit().putBoolean("folder", true).apply();
                    break;
            }

            fileLayout.setLayoutParams(fileParams);
            folderLayout.setLayoutParams(folderParams);

        });
        includeSubfolders.setOnCheckedChangeListener((view, checked) ->
            prefs.edit().putBoolean("subfolders", checked).apply());
        includeSubfolders.setChecked(prefs.getBoolean("subfolders", true));
        if (prefs.getBoolean("folder", false))
            folderRB.setChecked(true);
        else
            fileRB.setChecked(true);
        String folderName = Environment.getExternalStorageDirectory().getAbsoluteFile()
                + "/Android/data/" + getPackageName();
        androidFolder = new File(folderName);
        folderConversionLog = new File(folderName + "/folder_conversion.log");
        findingIDsLog = new File(folderName + "/ids_found.log");

        getFilesFromPreference();

        findViewById(R.id.findIdsBtn).setOnClickListener(v -> {
            conversion = false;
            onFindIds();
        });

        findViewById(R.id.convertBtn).setOnClickListener(v -> {
            conversion = true;
            cmdConvert();
        });
        findViewById(R.id.source_public_open).setOnClickListener(v ->
                openFileChooser(SOURCE_XML, xmlFilter, sourcePublicXml));
        findViewById(R.id.port_public_open).setOnClickListener(v ->
                openFileChooser(PORT_XML, xmlFilter, portPublicXml));
        openFile.setOnClickListener(v ->
                openFileChooser(SOURCE_SMALI, smaliFilter, sourceSmaliFile));
        openFolder.setOnClickListener(v ->
                openFolderChooser(SOURCE_SMALI_FOLDER, sourceSmaliFolder));
    }

    private void getFilesFromPreference() {
        sourcePublicXml = getFilePreference(SOURCE_PUBLIC_XML_FILENAME);
        portPublicXml = getFilePreference(PORT_PUBLIC_XML_FILENAME);
        sourceSmaliFile = getFilePreference(SOURCE_SMALI_FILENAME);
        sourceSmaliFolder = getFilePreference(SOURCE_SMALI_FOLDERNAME);
        if (sourcePublicXml != null) {
            sourcePublicET.setText(sourcePublicXml.getAbsolutePath());
        }
        if (portPublicXml != null) {
            portPublicET.setText(portPublicXml.getAbsolutePath());
        }
        if (sourceSmaliFile != null) {
            sourceSmaliFileET.setText(sourceSmaliFile.getAbsolutePath());
        }
        if (sourceSmaliFolder != null) {
            sourceSmaliFolderET.setText(sourceSmaliFolder.getAbsolutePath());
        }
    }

    private void openFileChooser(int request, String filter, File curFile){
        Intent intent;
        String startFolder;
        if (curFile == null || curFile.getParent().equals("/"))
            startFolder = "/storage";
        else
            startFolder = curFile.getParent();
        Log.d("start folder", startFolder);
        intent = new Intent(this, FChooser.class);
        intent.putExtra("type", "file");
        intent.putExtra("filter", filter);
        intent.putExtra("start_folder", startFolder);
        startActivityForResult(intent, request);
    }

    private void openFolderChooser(int request, File curFolder){
        Intent intent;
        String startFolder;
        if (curFolder == null || curFolder.getParent().equals("/"))
            startFolder = "/storage";
        else
            startFolder = curFolder.getParent();
        intent = new Intent(this, FChooser.class);
        intent.putExtra("type", "folder");
        intent.putExtra("start_folder", startFolder);
        startActivityForResult(intent, request);
    }

    private void setupDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Dummy message");
        progressDialog.setTitle("Dummy title");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
            if (data != null) {
                File selectedFile;
                try {
                    selectedFile = new File(data.getStringExtra("path"));
                    switch (requestCode) {
                        case SOURCE_XML:
                            sourcePublicXml = selectedFile;
                            sourceItem.setChecked(FilenameUtils.isFrameworkPublic(sourcePublicXml.getAbsolutePath()));
                            setFilePreference(SOURCE_PUBLIC_XML_FILENAME, selectedFile);
                            sourcePublicET.setText(selectedFile.getAbsolutePath());
                            break;
                        case PORT_XML:
                            portPublicXml = selectedFile;
                            portItem.setChecked(FilenameUtils.isFrameworkPublic(portPublicXml.getAbsolutePath()));
                            setFilePreference(PORT_PUBLIC_XML_FILENAME, selectedFile);
                            portPublicET.setText(selectedFile.getAbsolutePath());
                            break;
                        case SOURCE_SMALI:
                            sourceSmaliFile = selectedFile;
                            setFilePreference(SOURCE_SMALI_FILENAME, selectedFile);
                            sourceSmaliFileET.setText(selectedFile.getAbsolutePath());
                            break;
                        case SOURCE_SMALI_FOLDER:
                            sourceSmaliFolder = selectedFile;
                            setFilePreference(SOURCE_SMALI_FOLDERNAME, selectedFile);
                            sourceSmaliFolderET.setText(selectedFile.getAbsolutePath());
                            break;
                    }
                } catch (Exception e){
                    Log.d("File error", "Cannot find a file");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}
