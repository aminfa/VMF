/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 * Copyright 2017-2019 Goethe Center for Scientific Computing, University Frankfurt. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 */
package eu.mihosoft.vmf;

import eu.mihosoft.vmf.core.CodeGenerator;
import eu.mihosoft.vmf.core.TypeUtil;
import eu.mihosoft.vmf.core.io.FileResourceSet;
import eu.mihosoft.vmf.core.io.ResourceSet;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class performs the code generation of a given VMF model.
 * 
 * Created by miho on 03.01.2017.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VMF {
    
    private VMF() {
        //
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output directory for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param interfaces model interfaces (all interfaces must be in the same package,
     *                   package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(File outputDir, Class<?>... interfaces) throws IOException {
        new CodeGenerator().generate(new FileResourceSet(outputDir),interfaces);
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param resourceSet target resource set
     * @param interfaces model interfaces (all interfaces must be in the same package,
     *                   package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(ResourceSet resourceSet, Class<?>... interfaces) throws IOException {
        new CodeGenerator().generate(resourceSet, interfaces);
    }

    /**
     * Generates code for the specified model definition. The context classloader of the current thread is used to
     * locate the model interfaces.
     *
     * @param outputDir output directory for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param packageName package that contains the model interfaces
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(File outputDir, String packageName) throws IOException {
        Collection<Class<?>> interfaces = listClassesInPackage(
                Thread.currentThread().getContextClassLoader(), packageName);
        generate(outputDir,interfaces.toArray(new Class[interfaces.size()]));
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output resource set for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param classLoader the classloader that shall be used to locate the model interfaces in the specified package
     * @param packageName package that contains the model interfaces
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(ResourceSet outputDir, ClassLoader classLoader, String packageName) throws IOException {
        Collection<Class<?>> interfaces = listClassesInPackage(classLoader, packageName);
        generate(outputDir,interfaces.toArray(new Class[interfaces.size()]));
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output directory for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param classLoader the classloader that shall be used to locate the model interfaces in the specified package
     * @param packageName package that contains the model interfaces
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(File outputDir, ClassLoader classLoader, String packageName) throws IOException {
        Collection<Class<?>> interfaces = listClassesInPackage(classLoader, packageName);
        generate(outputDir,interfaces.toArray(new Class[interfaces.size()]));
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output resource set for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param classLoader the classloader that shall be used to locate the dependencies of the model interfaces
     * @param sourceFiles source files that contain the interface definitions
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(ResourceSet outputDir, ClassLoader classLoader, File... sourceFiles) throws IOException {

        InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();

        if(classLoader!=null) {
            compiler.useParentClassLoader(classLoader);
        }

        for(File f : sourceFiles) {
            String code = new String(Files.readAllBytes(f.toPath()), Charset.forName("UTF-8"));
            try {
                compiler.addSource(f.getName(), code);
            } catch (Exception ex) {
                throw new IOException("Cannot add source file '"+f+"'", ex);
            }
        }

        Map<String,Class<?>> modelClasses;

        try {
            modelClasses = compiler.compileAll();
        } catch (Exception ex) {
            throw new IOException("Cannot compile model definitions.", ex);
        }

        generate(outputDir,modelClasses.values().toArray(new Class[modelClasses.values().size()]));
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output resource set for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param sourceFiles source files that contain the interface definitions
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(ResourceSet outputDir, File... sourceFiles) throws IOException {
        generate(outputDir,Thread.currentThread().getContextClassLoader(),sourceFiles);
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output directory for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param classLoader the classloader that shall be used to locate the dependencies of the model interfaces
     * @param sourceFiles source files that contain the interface definitions
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(File outputDir, ClassLoader classLoader, File... sourceFiles) throws IOException {
        generate(new FileResourceSet(outputDir),classLoader,sourceFiles);
    }

    /**
     * Generates code for the specified model definition.
     *
     * @param outputDir output directory for the generated code, e.g., '<em>main/java/src-gen</em>'
     * @param sourceFiles source files that contain the interface definitions
     *                    (all interfaces must be in the same package, package must end with '.vmfmodel')
     * @throws IOException if the code generation fails due to I/O related problems
     * @throws IllegalArgumentException if the specified model is empty
     */
    public static void generate(File outputDir, File... sourceFiles) throws IOException {
        generate(new FileResourceSet(outputDir), sourceFiles);
    }

    /**
     * Lists all classes in the specified package.
     * @param classLoader classloader used for searching the package content
     * @param packageName package name
     * @return all classes in the specified package
     *
     * @throws IllegalArgumentException if the specified model is empty
     */
    private static Collection<Class<?>> listClassesInPackage(ClassLoader classLoader, String packageName) {
        List<String> clsNames = new FastClasspathScanner(packageName).addClassLoader(classLoader)
                .scan()
                .getNamesOfAllClasses();

        return clsNames.stream().map(clsName->loadClass(classLoader, clsName)).
                filter(cls->cls!=null).filter(cls->{
                    // Annotations from other packages are allowed since we use them inside the model.
                    // Thus, we remove annotations originating from other packages from this list to prevent
                    // error messages.
                    //
                    // Other classes originating from other packages are not removed since we generate an error message
                    // in this case to prevent illegal code generation.
                    return Objects.equals(TypeUtil.getPackageName(cls),packageName)||!cls.isAnnotation();
                }).
                collect(Collectors.toList());
    }

    /**
     * Loads a class from a specified classloader.
     * @param classLoader classloader that shall be used for classloading
     * @param clsName the name of the class to load
     * @return the reuested class or {@code null} if the requested class cannot be found
     *
     * @throws IllegalArgumentException if the specified model is empty
     */
    private static Class<?> loadClass(ClassLoader classLoader,String clsName) {
        try {
            return classLoader.loadClass(clsName);
        } catch (ClassNotFoundException e) {
            //
        }

        return null;
    }

}
