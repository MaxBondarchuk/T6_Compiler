package Compiler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileChecker {
    String file_name;

    public FileChecker(String file_name) {
        this.file_name = file_name;
    }

    public boolean check_for_utf_16() throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(file_name), "r");

        if (file_name.length() != 0) {
            short encoding = file.readShort();
            file.close();
            return encoding == -257;
        } else {
            file.close();
            return false;
        }
    }
}
