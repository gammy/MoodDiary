// Dialog interface by Kirill Mikhailov, based on an example by schwiz:
// https://stackoverflow.com/a/13986063/417115
package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class ShitDialog {

    private static final String PARENT_DIR = "..";
    private final String TAG = getClass().getName();
    private String[] fileList;
    private File currentPath;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }
    private ListenerList<FileSelectedListener> fileListenerList;
    private ListenerList<DirectorySelectedListener> dirListenerList;
    private final Activity activity;
    private boolean selectDirectoryOption;
    private String fileEndsWith;

    /**
     * @param activity
     * @param initialPath
     */
     public ShitDialog(Activity activity, File initialPath) {
        this(activity, initialPath, null);
     }

     public ShitDialog(Activity activity, File initialPath, String fileEndsWith) {
        this.activity = activity;
        setFileEndsWith(fileEndsWith);

         if (! initialPath.exists()) {
            initialPath = Environment.getExternalStorageDirectory();
        }

        loadFileList(initialPath);
        dirListenerList = new ListenerList<ShitDialog.DirectorySelectedListener>();
        fileListenerList = new ListenerList<ShitDialog.FileSelectedListener>();
     }

    /**
     * @return file dialog
     */
    public Dialog createFileDialog() {
        Log.d(TAG, "Enter createFileDialog");
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Import SQL from:\n" + currentPath.getPath());

        if (selectDirectoryOption) {
            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "setItems click: positive: which = " + Integer.toString(which));
                    Log.d(TAG, currentPath.getPath());
                    fireDirectorySelectedEvent(currentPath);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "setItems click: negative: which = " + Integer.toString(which));
                    Log.d(TAG, currentPath.getPath());
                    activity.finish();
                }
            });
        }

        builder.setItems(fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "setItems click: which = " + Integer.toString(which));
                String fileChosen = fileList[which];
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    loadFileList(chosenFile);
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                } else {
                    fireFileSelectedEvent(chosenFile);
                }
            }
        });

        dialog = builder.show();
        return dialog;
    }

    public void addFileListener(FileSelectedListener listener) {
        fileListenerList.add(listener);
    }

    public void removeFileListener(FileSelectedListener listener) {
        fileListenerList.remove(listener);
    }

    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.selectDirectoryOption = selectDirectoryOption;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    public void removeDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.remove(listener);
    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        createFileDialog().show();
    }

    private void fireFileSelectedEvent(final File file) {
        Log.d(TAG, "enter FireFileSelectedEvent");
        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private void fireDirectorySelectedEvent(final File directory) {
        Log.d(TAG, "enter FireDirectorySelectedEvent");
        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        Log.d(TAG, "Enter loadFileList");

        this.currentPath = path;
        List<String> r = new ArrayList<>();
        if (path.exists()) {
            if (path.getParentFile() != null) {
                r.add(PARENT_DIR);
            }
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    Log.d("?", "Enter inner accept()");
                    File sel = new File(dir, filename);
                    if (! sel.canRead()) {
                        return false;
                    }
                    if (selectDirectoryOption) {
                        return sel.isDirectory();
                    }
                    else {
                        boolean endsWith =
                                fileEndsWith != null
                                        ? filename.toLowerCase().endsWith(fileEndsWith)
                                        : true;
                        return endsWith || sel.isDirectory();
                    }
                }
            };
            String[] fileList1 = path.list(filter);
            for (String file : fileList1) {
                r.add(file);
            }
        }
        fileList = (String[]) r.toArray(new String[]{});
    }

    private File getChosenFile(String fileChosen) {
        Log.d(TAG, "Enter getChosenFile");

        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        }
        else {
            return new File(currentPath, fileChosen);
        }
    }

    private void setFileEndsWith(String fileEndsWith) {
        Log.d(TAG, "Enter setFileEndsWith");
        this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
    }
 }

class ListenerList<L> {
private List<L> listenerList = new ArrayList<L>();

public interface FireHandler<L> {
    void fireEvent(L listener);
}

public void add(L listener) {
    listenerList.add(listener);
}

public void fireEvent(FireHandler<L> fireHandler) {
    Log.d("?", "Enter fireEvent");
    List<L> copy = new ArrayList<L>(listenerList);
    for (L l : copy) {
        fireHandler.fireEvent(l);
    }
}

public void remove(L listener) {
    Log.d("?", "Enter remove");
    listenerList.remove(listener);
}

public List<L> getListenerList() {
    Log.d("?", "Enter getListenerList");
    return listenerList;
}
}