package io;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.StringUtils;

/**
 *
 * @author Stijn
 */
public class FileExtension extends FileFilter {
    public static FileExtension PNG = new FileExtension("Portable Network Graphics", "png");
    public static FileExtension JPG = new FileExtension("JPEG image", "jpg", "jpeg");
    public static FileExtension PDF = new FileExtension("Portable Document Format", "pdf");
    public static FileExtension MXP = new FileExtension("Merode File", "mxp");
    public static FileExtension SVG = new FileExtension("Scalable Vector Graphics", "svg");
    public static FileExtension CSV = new FileExtension("Commas-Seperated Values", "csv");
    public static FileExtension XLS = new FileExtension("Microsoft Excel File", "xls");
    public static FileExtension RTF = new FileExtension("Rich Text Format", "rtf");
    public static FileExtension TXT = new FileExtension("Plain Text File", "txt");
    public static FileExtension HTM = new FileExtension("HyperText Markup Language", "html", "htm");
    public static FileExtension BPMN = new FileExtension("BPMN2.0 XML Files", "bpmn", "bpmn2", "xml");
    
    private FileNameExtensionFilter filter;
    private String defaultExtension;

    public FileExtension(String description, String... extensions) {
        if (extensions == null)
            throw new IllegalArgumentException("A FileExtension object should have at least one extensions");
        description += " (*." + StringUtils.join(";*.", extensions) + ")";
        filter = new FileNameExtensionFilter(description, extensions);
        defaultExtension = extensions[0];
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    @Override
    public boolean accept(File f) {
        return filter.accept(f);
    }

    @Override
    public String getDescription() {
        return filter.getDescription();
    }

    public File getWellFormedFile(File f) {
        if (filter.accept(f))
            return f;
        return new File(f.getAbsolutePath() + "." + defaultExtension);
    }
}
