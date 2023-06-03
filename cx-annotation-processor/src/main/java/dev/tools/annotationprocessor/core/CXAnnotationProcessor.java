package dev.tools.annotationprocessor.core;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import dev.tools.annotationprocessor.core.annotations.CXSpringRestCrudApi;
import dev.tools.annotationprocessor.core.reflection.NameGenerationTool;
import dev.tools.annotationprocessor.core.writer.spring.SpringRestApiWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Main class for the CXAnnotationProcessor
 *
 * <p>An annotation processor to generate java class files to handle CXEntities, aiming for
 * persistence and code generation
 *
 * @author josue.rojas
 */
@SupportedAnnotationTypes("dev.tools.annotationprocessor.core.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class CXAnnotationProcessor extends AbstractProcessor {

  private SpringRestApiWriter springRestApiWriter;
  private Filer filer;
  private Messager messager;

  /**
   * Initialization method for annotation processing
   *
   * @param processingEnv current processing environment
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    springRestApiWriter = new SpringRestApiWriter();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  /**
   * Process source code elements and get annotations to generate code
   *
   * @param annotations allowed annotations
   * @param roundEnv source code environment
   * @return
   */
  @Override
  public boolean process(
      final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    annotations.forEach(
        annotation -> {
          Collection<JavaFile> javaFiles;
          try {
            javaFiles = applyAnnotations(annotation, roundEnv);
            javaFiles.stream()
                .forEach(
                    jf -> {
                      try {
                        jf.writeTo(filer);
                      } catch (IOException e) {
                        e.printStackTrace();
                      }
                    });
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    return true;
  }

  /**
   * Apply annotations based on CX entity persistence logic
   *
   * @param annotation target annotation
   * @param roundEnv source code environment
   * @return Collection<JavaFile> generated by javapoet features, to be generated
   */
  private Collection<JavaFile> applyAnnotations(
      final TypeElement annotation, final RoundEnvironment roundEnv) {
    final List<JavaFile> javaFileList = new ArrayList<>();
    for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (CXSpringRestCrudApi.class.getName().equals(annotation.asType().toString())) {
        final String packageName = NameGenerationTool.getPackageName(element);
        final Collection<JavaFile> javaFiles =
            springRestApiWriter.write((TypeElement) element, packageName);
        javaFileList.addAll(javaFiles);
      }
    }
    return javaFileList;
  }
}