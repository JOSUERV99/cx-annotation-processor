package dabba.doo.annotationprocessor;

import com.squareup.javapoet.JavaFile;
import dabba.doo.annotationprocessor.core.writer.spring.layers.SpringRepositoryClassWriter;
import dabba.doo.annotationprocessor.core.writer.spring.layers.SpringServiceClassWriter;
import dabba.doo.annotationprocessor.db.Pojo;
import dabba.doo.annotationprocessor.db.paramresolver.ParameterMapCreator;
import dabba.doo.annotationprocessor.db.sqlwriter.SqlReadSentenceGenerator;
import dabba.doo.annotationprocessor.db.sqlwriter.SqlWriteSentenceGenerator;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        writeJavaFile("PojoRepository", "src/main/java/j2d/generated/repository", writeRepositoryClazz());
        writeJavaFile("PojoService", "src/main/java/j2d/generated/service", writeServiceClazz(Class.forName("j2d.generated.repository.PojoRepository")));
    }

    public static void demoSqWriter() {
        final Pojo instance = getSimplePojo();
        final SqlWriteSentenceGenerator sqlSentenceWriteWriter = new SqlWriteSentenceGenerator();
        System.out.println(sqlSentenceWriteWriter.writeInsertSentence(instance.getClass()));
        System.out.println(sqlSentenceWriteWriter.writeDeleteSentence(instance.getClass()));
    }

    public static void demoSqReader() {
        final Pojo instance = getSimplePojo();
        final SqlReadSentenceGenerator sqlSentenceReadWriter = new SqlReadSentenceGenerator();
//        System.out.println(sqlSentenceReadWriter.w(instance.getClass()));
//        System.out.println(sqlSentenceReadWriter.writeDeleteSentence(instance.getClass()));
    }

    public static void demoParameterCreator() {
        final Pojo instance = getSimplePojo();
        System.out.println(new ParameterMapCreator().buildParamsMap(instance).getValues());
    }

    public static String writeRepositoryClazz() {
        final SpringRepositoryClassWriter classWriter = new SpringRepositoryClassWriter();
        final JavaFile javaFile = classWriter.writeFile(Pojo.class);
        return javaFile.toString();
    }

    public static String writeServiceClazz(Class<?> clazz) {
        final SpringServiceClassWriter classWriter = new SpringServiceClassWriter();
        final JavaFile javaFile = classWriter.writeFile(Pojo.class, clazz);
        return javaFile.toString();
    }

    private static void writeJavaFile(final String filename, final String packageName, final String content) throws IOException {
        Files.createDirectories(Paths.get(packageName));
        final List<String> lines = Arrays.asList(content.split("\n"));
        final Path file = Paths.get(String.format("%s/%s.java", packageName, filename));
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static Pojo getSimplePojo() {
        final Pojo instance = new Pojo();
        instance.setId(1);
        instance.setMessageContent("Hello");
        instance.setCountNumberFromExternalService(10);
        instance.setSecondId(2);
        return instance;
    }
}
