package uk.gov.ida.verifymatchingservicetesttool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

public class FileUtils {

    private final String scenarioDirectory;

    public FileUtils(String scenarioDirectory) {
        this.scenarioDirectory = scenarioDirectory;
    }

    public String readFromResources(String fileName) throws IOException {
        InputStream inputStream = this.getClass()
            .getClassLoader()
            .getResourceAsStream(Paths.get(scenarioDirectory, fileName).toString());

        return readFrom(inputStream);
    }

    public static String read(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return readFrom(inputStream);
        }
    }

    private static String readFrom(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            return bufferedReader
                .lines()
                .collect(joining(lineSeparator()));
        }
    }
}
