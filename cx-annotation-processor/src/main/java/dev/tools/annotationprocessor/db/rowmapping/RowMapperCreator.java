package dev.tools.annotationprocessor.db.rowmapping;

import com.squareup.javapoet.*;
import dev.tools.annotationprocessor.core.reflection.ClassReflectionTool;
import dev.tools.annotationprocessor.core.reflection.NameGenerationTool;
import dev.tools.annotationprocessor.core.writer.JavaClassFile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.springframework.jdbc.core.RowMapper;

/**
 * Row mapper creator
 *
 * @author josue.rojas
 */
public class RowMapperCreator {
  /**
   * Build row mapper class (for select queries)
   *
   * @param clazz type element from annotation processing
   * @param className class name as string
   * @return TypeSpec for mapper method
   */
  private TypeSpec buildRowMapperClass(final TypeElement clazz, final String className) {
    final ParameterizedTypeName parameterizedTypeName =
        ParameterizedTypeName.get(
            ClassName.get(RowMapper.class), NameGenerationTool.getTypeNameFromTypeElement(clazz));
    return TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(parameterizedTypeName)
        .addMethod(
            MethodSpec.methodBuilder("mapRow")
                .addParameter(ResultSet.class, "rs", Modifier.FINAL)
                .addParameter(int.class, "rowCount")
                .addAnnotation(Override.class)
                .addStatement(generateMappingForClassName(clazz, "rs", "instance"))
                .addException(SQLException.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(NameGenerationTool.getTypeNameFromTypeElement(clazz))
                .build())
        .build();
  }

  /**
   * Build row mapper java class file
   *
   * @param clazz type element from annotation processing
   * @param packageName package name
   * @return JavaClassFile for mapper generated class
   */
  public JavaClassFile buildRowMapperJavaClassFile(
      final TypeElement clazz, final String packageName) {
    final String className = clazz.getSimpleName().toString() + "RowMapper";
    final String filePackageName = packageName + ".rowmapper";
    final JavaFile javaFile =
        JavaFile.builder(filePackageName, buildRowMapperClass(clazz, className)).build();
    return new JavaClassFile()
        .setJavaFile(javaFile)
        .setClassName(className)
        .setPackageName(filePackageName)
        .setFileName(String.format("%s.%s.java", filePackageName, className))
        .setName((String.format("%s.%s", filePackageName, className)));
  }

  private String generateMappingForClassName(
      final TypeElement element, final String rsKey, final String instanceName) {

    final StringBuilder stringBuilder = new StringBuilder();
    final String className = element.toString();

    // add instance creation
    stringBuilder
        .append("final ")
        .append(className)
        .append(" ")
        .append(instanceName)
        .append(" = ")
        .append("new ")
        .append(className)
        .append("();\n");

    // get attributes
    final List<? extends Element> fields = ClassReflectionTool.getDeclaredFields(element);

    final Map<String, TypeName> typeMap =
        fields.stream()
            .collect(
                Collectors.toMap(e -> e.getSimpleName().toString(), e -> TypeName.get(e.asType())));

    final String mappingBody =
        fields.stream()
            .map(
                field -> {
                  final String fieldName = field.getSimpleName().toString();
                  String setLine = "";
                  final String setCallString =
                      String.format(
                          "set%s",
                          ("" + fieldName.charAt(0)).toUpperCase() + fieldName.substring(1));
                  final TypeName type = typeMap.get(fieldName);
                  String setTypeFromRs = "";
                  if (type.equals(TypeName.get(Integer.class))
                      || type.equals(TypeName.get(int.class))) {
                    setTypeFromRs = "getInt(";
                  } else if (type.equals(TypeName.get(Boolean.class))
                      || type.equals(TypeName.get(boolean.class))) {
                    setTypeFromRs = "getBoolean(";
                  } else if (type.equals(TypeName.get(String.class))) {
                    setTypeFromRs = "getString(";
                  } else if (type.equals(TypeName.get(Long.class))
                      || type.equals(TypeName.get(long.class))) {
                    setTypeFromRs = "getLong(";
                  } else if (type.equals(TypeName.get(Float.class))
                      || type.equals(TypeName.get(float.class))) {
                    setTypeFromRs = "getFloat(";
                  }
                  setLine +=
                      instanceName
                          + "."
                          + setCallString
                          + "("
                          + rsKey
                          + "."
                          + setTypeFromRs
                          + "\""
                          + fieldName
                          + "\"));";
                  return setLine;
                })
            .collect(Collectors.joining("\n"));
    stringBuilder.append(mappingBody).append("\n");
    stringBuilder.append("return " + instanceName + "");
    return stringBuilder.toString();
  }
}
