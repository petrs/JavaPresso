 /*
 * Copyright (c) 2016-2017 Petr Svenda <petr@svenda.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package javapresso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class JavaPresso {
        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) {
            JavaPresso app = new JavaPresso();
            app.run(args);
        }

        private void run(String[] args) {
            try {
                if (args.length < 2) {
                    help();
                } else {
                    packClasses(args[0], args[1]);
                }
            } catch (Exception ex) {
                System.out.printf(ex.getLocalizedMessage());
            } finally {
            }
        }

        /**
         * Prints help.
         */
        private void help() {
            System.out.println("Usage: java -jar JavaPresso.jar input_folder_path namespace_name");
        }

        class JavaFileInfo {

            String packageLine;
            ArrayList<String> importLines;
            ArrayList<String> fileContent;

            JavaFileInfo() {
                importLines = new ArrayList<>();
                fileContent = new ArrayList<>();
            }
        }

        private void packClasses(String basePath, String className) throws FileNotFoundException, IOException {
            String filesPath = basePath + File.separator;
            File dir = new File(filesPath);
            String[] filesArray = dir.list();

            if ((filesArray != null) && (dir.isDirectory() == true)) {

                ArrayList<JavaFileInfo> filesContent = new ArrayList<>(filesArray.length);

                for (int i = 0; i < filesArray.length; i++) {
                    filesContent.add(i, loadJavaFile(filesPath + filesArray[i]));
                }

                //
                // Header
                //
                int startOffset = filesContent.get(0).packageLine.indexOf("package ") + "package ".length();
                int endOffset = filesContent.get(0).packageLine.lastIndexOf(";");
                String packageName = filesContent.get(0).packageLine.substring(startOffset, endOffset);
                String fileName = String.format("%s\\%s.java", basePath, className);
                FileOutputStream file = new FileOutputStream(fileName);
                String header = "// Merged file class by JavaCodePacker \r\n";
                header += String.format("// Add 'import %s.%s;' to access all classes as usual.\r\n", packageName, className);
                header += filesContent.get(0).packageLine;
                header += "\r\n";
                // Add import lines without duplicities
                Set set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
                for (JavaFileInfo fileInfo : filesContent) {
                    set.addAll(fileInfo.importLines);
                }
                ArrayList<String> importLines = new ArrayList(set);
                for (String importLine : importLines) {
                    header += importLine;
                }

                header += String.format("\r\npublic class %s {\r\n\r\n", className);

                file.write(header.getBytes());
                file.flush();

                // 
                // Separate classes
                //
                String indent = "    ";
                for (JavaFileInfo fileInfo : filesContent) {
                    for (String line : fileInfo.fileContent) {
                        file.write(indent.getBytes());
                        file.write(line.getBytes());
                    }
                    file.flush();
                }

                //
                // Footer
                //
                String footer = "}\r\n";
                file.write(footer.getBytes());
                file.flush();
                file.close();
            } else {
                System.out.println("directory '" + filesPath + "' is empty");
            }
        }

        private JavaFileInfo loadJavaFile(String filePath) {
            JavaFileInfo fileInfo = new JavaFileInfo();
            try {
                //create BufferedReader to read csv file
                BufferedReader br = new BufferedReader(new FileReader(filePath));
                String strLine;

                //read file line by line
                while ((strLine = br.readLine()) != null) {
                    strLine += "\r\n";
                    String trimedStrLine = strLine.trim();
                    if (trimedStrLine.startsWith("package ")) {
                        fileInfo.packageLine = strLine;
                    } else if (trimedStrLine.startsWith("import ")) {
                        fileInfo.importLines.add(strLine);
                    } else if (trimedStrLine.startsWith("public class ")) {
                        fileInfo.fileContent.add(strLine.replaceAll("public class ", "static class "));
                    } else {
                        fileInfo.fileContent.add(strLine);
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception while reading csv file: " + e);
            }
            return fileInfo;
        }
    }
