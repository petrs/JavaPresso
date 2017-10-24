# JavaPresso
[![Latest release](https://img.shields.io/github/release/petrs/JavaPresso.svg)](https://github.com/petrs/JavaPresso/releases/latest)

**Ever wanted to get rid of many files with many Java classes and copy just a single file into your project instead?** 

Not a typical desire for the standard 
Java developer, but quite helpful if dealing with more awkward toolchain like the conversion of applets for the JavaCard platform. 

This simple tool takes multiple Java source files, extracts contained classes and inserts them as static subclasses into a single file. 
Makes inclusion of a target library (with multiple files) simpler by wrapping all classes into a single namespace and file. 

## Usage
```
java -jar JavaPresso.jar input_folder_path namespace_name 
```
Takes all `*.java` files from `input_folder_path` and inserts them as static subclasses into single `namespace_name.java` file. 
The implementation of classes is otherwise unchanged.

If you don't want to include all classes from an original package, simply delete corresponding files before running JavaPresso.


## Example

Assume existence of clasess `A`, `B` and `C` with the corresponding files `A.java`, `B.java` and `C.java` located inside `/src/mylibrary/` folder.
Running command `java -jar JavaPresso.jar /src/mylibrary presso` will create single file `presso.java` with the following content:
```
package  mylibrary;
import ... // all imports from A,B and C collated together without duplicities

public class presso {
    // ... original head section of class A (license, authors...)
    static class A {
       // ... original implementation of class A
    }
    // ...
    static class B {
       // ... original implementation of class B
    }
    // ...
    static class C {
       // ... original implementation of class C
    }
}
```

A single file `presso.java` can be now copied into your project directory. All classes are made available for use by typing `import mylibrary.presso.*;`

## Why?

The common way how to keep project's src folder clean and well-arranged is to separate different functionality into multiple subfolders with the different package names. 
But such a structure makes it difficult to compile and convert all classes into single JavaCard executable (cap file). As a result, 
all on-card files with classes are put under the single package name and thus also into the same folder, making it quickly chaotic. The JavaPresso tool converts whole library you like to include into a single file and also simplifies its import in applet's code.  

## License
Code is licensed under permissive MIT license.


