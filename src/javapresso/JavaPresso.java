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
    public static final String JAVA_PRESSO_VERSION = "1.0.1";
        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) {
            JavaPresso app = new JavaPresso();
            app.run(args);
        }

        private void run(String[] args) {
            info();
            try {
                if (args.length < 2) {
                    help();
                } else {
                    if (args.length < 3) {
                        System.out.println("Info: you omitted the version argument, using 'undefined' instead");
                        packClasses(args[0], args[1], "undefined");
                    }
                    else {
                        packClasses(args[0], args[1], args[2]);
                    }
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
            System.out.println("Usage: java -jar JavaPresso.jar input_folder_path namespace_name compressed_file_version");
            System.out.println("Example: 'java -jar JavaPresso.jar C:\\JCMathLib\\src\\opencrypto\\jcmathlib\\ jcmathlib 0.4.2'");
        }
        
        /**
         * Prints info.
         */
        private void info() {
            System.out.println("\n-----------------------------------------------------------------------   ");
            System.out.println("JavaPresso " + JAVA_PRESSO_VERSION + " - compress multiple source files of library into a single include.");
            System.out.println("Petr Svenda 2016-2021 (https://github.com/petrs/).");
            help();
            System.out.println("Please check if you use the latest version at\n  https://github.com/petrs/JavaPresso/releases/latest.");

            System.out.println("-----------------------------------------------------------------------\n");
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

        private void packClasses(String basePath, String namespaceName, String version) throws FileNotFoundException, IOException {
            String filesPath = basePath + File.separator;
            File dir = new File(filesPath);
            String[] filesArray = dir.list();
            
            System.out.println("\r\n");
            System.out.println(String.format("input folder   = '%s'", filesPath));
            System.out.println(String.format("namespace name = '%s'", namespaceName));
            System.out.println(String.format("version        = '%s'", version));
            System.out.println("\r\n");

            if ((filesArray != null) && (dir.isDirectory() == true)) {

                ArrayList<JavaFileInfo> filesContent = new ArrayList<>(filesArray.length);

                for (int i = 0; i < filesArray.length; i++) {
                    filesContent.add(i, loadJavaFile(filesPath + filesArray[i]));
                }

                //
                // Header
                //
                String fileName = String.format("%s\\%s.java", basePath, namespaceName);
                FileOutputStream file = new FileOutputStream(fileName);
                String header = "// Merged file class by JavaPresso (https://github.com/petrs/JavaPresso) \r\n";
                header += "// TODO: Change 'your_package' to your real package name as necessary\r\n";
                header += String.format("// TODO: Add 'import your_package.%s.*;' to access all classes as usual\r\n\r\n", namespaceName);
                header += "package your_package;\r\n";
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

                header += String.format("\r\npublic class %s {\r\n", namespaceName);
                header += String.format("    public static String version = \"%s\"; \r\n\r\n", version);

        
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
                
                System.out.println(String.format("Successfully compressed into '%s'", fileName));
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
                        fileInfo.fileContent.add(strLine.replaceAll("public class ", "public static class "));
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
