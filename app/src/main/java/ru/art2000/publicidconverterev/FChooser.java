package ru.art2000.publicidconverterev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.Manifest;

import ru.art2000.widgets.NiceButton;
import ru.art2000.widgets.YesNoPopUp;

public class FChooser extends Activity {

    MenuItem upper;
    TextView pathTV;
    ListView contentLV;
    ViewGroup emptyView;
    String filter;
    FAdapter fAdapter;
    String folder = "folder";
    String file = "file";
    String defaultPath = "/storage";
    String startFolder;
    File startFile;
//    FileFilter ff = pathname ->
//            false;
//    FilenameFilter ff2 = ;
    String selectFolder = "Select %s folder?";
    String selectFile = "Select %s file?";
    File selectedFile;
    YesNoPopUp selectPopUp;
    String type;
    int mSelected = -1;
    File[] mmStorageList = {new File("/sdcard"), getSdcardPath()};
    File curPath = null;
    boolean doubleBackToExitPressedOnce = false;

    public File[] sortedFileList(File[] fs){
        int k = 0;
        for (File f : fs) {
            if (f.isDirectory() || FilenameUtils.getNoDotExtension(f.getName()).equals(filter))
                k++;
        }

        File[] tmp;
        int c = 0;
        tmp = new File[k];
        for (File f : fs) {
            if (f.isDirectory() || FilenameUtils.getNoDotExtension(f.getName()).equals(filter)) {
                tmp[c] = f;
                c++;
            }
        }

        ArrayList<File> folderList = new ArrayList<>();
        ArrayList<File> fileList = new ArrayList<>();
        for (File f: tmp){
            if (f.isDirectory())
                folderList.add(f);
            else
                fileList.add(f);
        }
        Collections.sort(folderList, (o1, o2) ->{
            int minDifference = 100;
            int maxDifference = 1000;
            String name1 = o1.getName();
            String name2 = o2.getName();
            Log.d("folder o1", name1);
            Log.d("folder o2", name2);
            Log.d("result", String.valueOf(name1.compareToIgnoreCase(name2)));
            int res = name1.compareToIgnoreCase(name2);
            if ((res > minDifference && res < maxDifference) ||
                    (res < -minDifference && res >-maxDifference))
                return -res;
            return res;
        });
        Collections.sort(fileList, (o1, o2) -> {
            int minDifference = 100;
            int maxDifference = 1000;
            String name1 = o1.getName();
            String name2 = o2.getName();
            Log.d("folder o1", name1);
            Log.d("folder o2", name2);
            Log.d("result", String.valueOf(name1.compareToIgnoreCase(name2)));
            int res = name1.compareToIgnoreCase(name2);
            if ((res > minDifference && res < maxDifference) ||
                    (res < -minDifference && res >-maxDifference))
                return -res;
            return res;
        });

        tmp = new File[folderList.size() + fileList.size()];
        int i = 0;
        for (File f: folderList){
            tmp[i] = f;
            i++;
        }
        for (File f: fileList){
            tmp[i] = f;
            i++;
        }
        if (i == 0){
            contentLV.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            contentLV.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        return tmp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fchooser_menu, menu);
        upper = menu.getItem(0);
        upper.setVisible(upVisibility());
        return super.onCreateOptionsMenu(menu);
    }

    public boolean rootOfInternalOrExternalStorage() {
        return curPath.getName().equals("sdcard") || curPath.getName().equals(getSdcardPath().getName());
    }

    public String getFName(){
        ListPreference
        String path = pathTV.getText().toString();
        if (mSelected == -1) {
            return path.substring(path.lastIndexOf("/"));
        } else {
            if (path.endsWith(defaultPath) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return "/" + mmStorageList[mSelected].getName();
            else
                if (type.equals(folder))
                    return "/" + sortedFileList(curPath.listFiles())[mSelected].getName();
                else
                    return sortedFileList(curPath.listFiles())[mSelected].getName();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case R.id.upper:
                mSelected = -1;
                if (rootOfInternalOrExternalStorage()){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        curPath = curPath.getParentFile();
                        fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));
                        pathTV.setText(curPath.getAbsolutePath());
                    } else {
                        pathTV.setText(defaultPath);
                        fAdapter = new FAdapter(this, mmStorageList);
                    }
                    selectPopUp.hide();
                } else {
                    curPath = curPath.getParentFile();
                    fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));
                    pathTV.setText(curPath.getAbsolutePath());
                }


                contentLV.setAdapter(fAdapter);
                fAdapter.notifyDataSetChanged();
                selectPopUp.setPopUpText(String.format(selectFolder, getFName()));

                upper.setVisible(upVisibility());
                if (type.equals(file))
                    selectPopUp.hide();
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public boolean upVisibility(){
        return !(pathTV.getText().equals(defaultPath) || curPath.getParentFile() == null
                || curPath.getParentFile().listFiles() == null);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap back one more time to return on previous screen",
                Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() ->
                doubleBackToExitPressedOnce = false, 2000);
    }

    public File getSdcardPath() {
        for (File file: new File(defaultPath).listFiles()){
            if (file.listFiles() != null)
                return file;
        }
        return null;
    }

    public File[] getStorageFileList(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return sortedFileList(new File(defaultPath).listFiles());
        else
            return mmStorageList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 666) {
                selectPopUp.setType(selectPopUp.yesOnly);
                curPath = selectedFile;
                mSelected = -1;
                pathTV.setText(curPath.getAbsolutePath());
                fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));
                contentLV.setAdapter(fAdapter);
                selectPopUp.setPopUpText(String.format(selectFolder, getFName()));
                upper.setVisible(upVisibility());
                if (type.equals(file))
                    selectPopUp.hide();
                else
                    selectPopUp.show();
            }
            if (requestCode == 777) {
                Intent intent = new Intent();
                if (type.equals(folder) && mSelected == -1)
                    intent.putExtra("path", curPath.getAbsolutePath());
                else
                    intent.putExtra("path", selectedFile.getAbsolutePath());
                setResult(666, intent);
                finish();
            }
        } else
            Toast.makeText(this, "Cannot open folder - external storage permission was denied!",
                Toast.LENGTH_LONG).show();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fchooser);
        pathTV = findViewById(R.id.cur_path);
        contentLV = findViewById(R.id.path_content);
        emptyView = findViewById(R.id.empty_view);
        selectPopUp = findViewById(R.id.select_popup);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            curPath = new File(defaultPath);
        else
            mmStorageList = sortedFileList(mmStorageList);
        type = getIntent().getStringExtra("type");
        filter = getIntent().getStringExtra("filter");
        startFolder = getIntent().getStringExtra("start_folder");
        startFile = new File(startFolder);
        if (type.equals(folder)) {
            selectPopUp.setType(selectPopUp.yesOnly);
            ((ImageView) emptyView.getChildAt(0)).setImageResource(R.drawable.ic_manager_folder);
            ((TextView) emptyView.getChildAt(1)).setText("No folders found in directory");
        } else {
            ((ImageView) emptyView.getChildAt(0)).setImageResource(R.drawable.ic_manager_file);
            ((TextView) emptyView.getChildAt(1)).setText("No files found in directory");
        }
        selectPopUp.setPopUpClickListener(new YesNoPopUp.PopUpClickListener() {

            @Override
            public void onConfirmButtonClick() {
                if (shouldRequestPerm())
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
                if (!shouldRequestPerm() ||
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent();
                    if (type.equals(folder) && mSelected == -1)
                        intent.putExtra("path", curPath.getAbsolutePath());
                    else
                        intent.putExtra("path", selectedFile.getAbsolutePath());
                    setResult(666, intent);
                    finish();
                    }
            }

            @Override
            public void onCancelButtonClick() {
                mSelected = -1;
                if (type.equals(folder) && !pathTV.getText().equals(defaultPath)) {
                    selectPopUp.setPopUpText(String.format(selectFolder, getFName()));
                    selectPopUp.setType(selectPopUp.yesOnly);
                } else
                    selectPopUp.hide();
                fAdapter.notifyDataSetChanged();
            }

        });
        if (startFolder.equals(defaultPath)) {
            pathTV.setText(defaultPath);
            fAdapter = new FAdapter(this, getStorageFileList());
            selectPopUp.hide();
        } else {
            pathTV.setText(startFolder);
            curPath = startFile;
            fAdapter = new FAdapter(this, sortedFileList(startFile.listFiles()));
        }
//        if (curPath == null)
//            fAdapter = new FAdapter(this, mmList);
//        else
//            fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));

        switch (type){
            case "file":
                getActionBar().setTitle("Select file");
                break;
            default:
                getActionBar().setTitle("Select folder");
                selectPopUp.setPopUpText(String.format(selectFolder, getFName()));
                break;
        }

        contentLV.setAdapter(fAdapter);
        contentLV.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->  {
            final File[] shownFiles;
            if (curPath == null || (pathTV.getText().equals(defaultPath) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M))
                shownFiles = mmStorageList;
            else
                shownFiles = sortedFileList(curPath.listFiles());
            selectedFile = shownFiles[position];
            contentLV.setSelection(position);
            if (selectedFile.isDirectory()) {
                if (selectedFile.listFiles() != null) {
                    openFolder(selectedFile);
                } else
                    Toast.makeText(this, "Not openable folder!", Toast.LENGTH_SHORT).show();
            } else {
                selectPopUp.setType(selectPopUp.yes_no);
                selectPopUp.show();
                mSelected = position;
                selectPopUp.setPopUpText(String.format(selectFile, getFName()));
                fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));
                contentLV.setAdapter(fAdapter);
            }
        });
        contentLV.setOnItemLongClickListener((parent, view, position, id) -> {
            final File[] shownFiles;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && pathTV.getText().toString().equals(defaultPath))
                shownFiles = mmStorageList;
            else
                shownFiles = sortedFileList(curPath.listFiles());
            selectedFile = shownFiles[position];
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            selectPopUp.setType(selectPopUp.yes_no);
            v.vibrate(100);
            fAdapter = new FAdapter(this, shownFiles);
            if (type.equals(file)) {
                if (!selectedFile.isDirectory()) {
                    mSelected = position;
                    selectedFile = shownFiles[position];
                    selectPopUp.setPopUpText(String.format(selectFile, getFName()));
                    selectPopUp.show();
                } else
                    openFolder(selectedFile);
            }
            if (type.equals(folder)) {
                mSelected = position;
                selectedFile = shownFiles[position];
                selectPopUp.setPopUpText(String.format(selectFolder, getFName()));
                if (pathTV.getText().toString().equals(defaultPath))
                    selectPopUp.show();
            }
            contentLV.setAdapter(fAdapter);
            return true;
        });

    }

    public void openFolder(File folder){
        if (shouldRequestPerm()) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);
        }
        if (!shouldRequestPerm() ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectPopUp.setType(selectPopUp.yesOnly);
            curPath = folder;
            mSelected = -1;
            pathTV.setText(curPath.getAbsolutePath());
            fAdapter = new FAdapter(this, sortedFileList(curPath.listFiles()));
            contentLV.setAdapter(fAdapter);
            selectPopUp.setPopUpText(String.format(selectFolder, getFName()));
            upper.setVisible(upVisibility());
            if (type.equals(file))
                selectPopUp.hide();
            else
                selectPopUp.show();
        }
    }

    public boolean shouldRequestPerm(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                selectedFile.getAbsolutePath().contains(getSdcardPath().getName());
    }


    class FAdapter extends BaseAdapter{

        File[] files;
        Context mContext;

        FAdapter(Context ctx, File[] content){
            mContext = ctx;
            files = content;
        }

        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public File getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.file_manager_item, parent, false);
            if (position == mSelected)
                view.setBackgroundColor(getResources().getColor(R.color.grey));
            else
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//                view.setBackground(android.R.attr.selectableItemBackground);
            TextView fileSize = view.findViewById(R.id.size);
                ((TextView) view.findViewById(R.id.item_title)).setText(getItem(position).getName());
                if (getItem(position).isDirectory()) {
                    fileSize.setVisibility(View.GONE);
                    ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.ic_manager_folder);
                } else {
                    fileSize.setVisibility(View.VISIBLE);
                    ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.ic_manager_file);
                    fileSize.setText(getFileSize(getItem(position)));
                }
            return view;
        }


        String getFileSize(File cFile){
            String[] suf = {" B", " KB", " MB", " GB"};
            int i = 0;
            float len = (int) cFile.length();
            while (len > 1024){
                i++;
                len = len/1024;
            }
            NumberFormat nf = new DecimalFormat("#.##");
            return (nf.format(len) + suf[i]);
        }


//        AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                mSelected = position;
//                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                v.vibrate(100);
//                return true;
//            }
//        };


    }

}
