package io;

import com.jtechlabs.ui.widget.directorychooser.JDirectoryChooser;
import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Stijn
 */
public class FileChooser {
    private FileChooser() { }
    
    private static File currentDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
    
    public static void setCurrentDirectory(File dir) {
        currentDirectory = dir;
    }
    public static File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * This convience method prepares a JFileChooser with the given Component
     * as parent. The FileExtensions object will be used as the filefilters.
     *
     * This method will always return either a valid file, or null if the user
     * cancels the dialog box. The file returned is garanteed to be writable
     * and to have a valid extension from the ones provided to this method. Any
     * error will be dealt with internally.
     */
    public static File save(Component parent, FileExtension... filters) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(currentDirectory);
        if (filters != null) {
            chooser.setAcceptAllFileFilterUsed(false);
            for (FileExtension filter : filters)
                chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filters[0]);
        }
        while (true) {
            int result = chooser.showSaveDialog(parent);
            if (result == JFileChooser.CANCEL_OPTION)
                return null;
            currentDirectory = chooser.getCurrentDirectory();
            File file = chooser.getSelectedFile();
            if (result == JFileChooser.ERROR_OPTION)
                JOptionPane.showMessageDialog(parent,
                    "An unknown error occured while trying to open " + (file==null?" the file":file.getName()) + " ."
                    + "\nPlease choose a different file.",
                    "Error while saving", JOptionPane.ERROR_MESSAGE);
            else {
                file = ((FileExtension) chooser.getFileFilter()).getWellFormedFile(file);
                if (!file.exists())
                    return file;
                int overwrite = JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite " +
                    file.getName() + "?", "Are you sure?" , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (overwrite == JOptionPane.CANCEL_OPTION)
                    return null;
                if (overwrite == JOptionPane.YES_OPTION) {
                    if (file.canWrite())
                        return file;
                    JOptionPane.showMessageDialog(parent,
                        "Could not open " + (file==null?"file":file.getName()) + " for writing."
                        + "\nPlease choose a different file.",
                        "Error while saving", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * This convience method prepares a JFileChooser with the given Component
     * as parent. The FileExtensions object will be used as the filefilters.
     *
     * This method will always return either a valid file, or null if the user
     * cancels the dialog box. The file returned is garanteed to exist and to
     * have a valid extension from the ones provided to this method. Any error
     * will be dealt with internally.
     */
    public static File open(Component parent, FileExtension... filters) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(currentDirectory);
        if (filters != null) {
            chooser.setAcceptAllFileFilterUsed(false);
            for (FileExtension filter : filters)
                chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filters[0]);
        }
        while (true) {
            int result = chooser.showOpenDialog(parent);
            if (result == JFileChooser.CANCEL_OPTION)
                return null;
            currentDirectory = chooser.getCurrentDirectory();
            File file = chooser.getSelectedFile();
            if (result == JFileChooser.ERROR_OPTION)
                JOptionPane.showMessageDialog(parent,
                    "An unknown error occured while trying to open " + (file==null?" the file":file.getName()) + " ."
                    + "\nPlease choose a different file.",
                    "Error while saving", JOptionPane.ERROR_MESSAGE);
            else {
                file = ((FileExtension) chooser.getFileFilter()).getWellFormedFile(file);
                if (file.canRead())
                    return file;
                JOptionPane.showMessageDialog(parent,
                    "Could not open " + (file==null?"file":file.getName()) + " for reading."
                    + "\nPlease choose a different file.",
                    "Error while saving", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * This convience method prepares a JFileChooser with the given Component
     * as parent. The FileExtensions object will be used as the filefilters.
     *
     * This method will always return either a valid file array, or null if the
     * user cancels the dialog box. The files returned is garanteed to exist and
     * to have a valid extension from the ones provided to this method. Any
     * error will be dealt with internally.
     */
    public static File[] openFiles(Component parent, FileExtension... filters) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setCurrentDirectory(currentDirectory);
        if (filters != null) {
            chooser.setAcceptAllFileFilterUsed(false);
            for (FileExtension filter : filters)
                chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filters[0]);
        }
        while (true) {
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.CANCEL_OPTION)
                return null;
            File[] files = chooser.getSelectedFiles();
            if (result == JFileChooser.ERROR_OPTION)
                JOptionPane.showMessageDialog(null,
                    "An unknown error occured while trying to open " +
                    ((files==null||files.length==0)?"the file":files[0] +
                    (files.length==1?".":" and "+(files.length-1)+" other files."))
                    + "\nPlease choose a different file.",
                    "Error while saving", JOptionPane.ERROR_MESSAGE);
            else {
                boolean failure = false;
                for (int i = 0; i < files.length; i++) {
                    files[i] = ((FileExtension) chooser.getFileFilter()).getWellFormedFile(files[i]);
                    if (!files[i].canRead()) {
                        JOptionPane.showMessageDialog(null,
                            "An unknwon error occurred while trying to open " +
                            (files[i]==null?"a file":files[i].getName())
                            + ".\nPlease choose a different file.",
                            "Error while saving", JOptionPane.ERROR_MESSAGE);
                        failure = true;
                        break;
                    }
                }
                if (!failure)
                    return files;
            }
        }
    }

    public static File selectDirectory(Component parent) {
        File file = JDirectoryChooser.showDialog(parent, currentDirectory, "Select directory", "Please select a directory");
        if (file != null)
            currentDirectory = file;
        return file;
    }
}
