package org.example;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;
import java.lang.Object;


@RestController
public class Controller {
    String saveDir = "Files/";
    Logger logger = LoggerFactory.getLogger(App.class);

    @PostMapping("/list")
    public ResponseEntity<String> GetList() {
        String text = "";
        //JsonReader reader = Json.createReader(new StringReader("{\"LIST\" : [] }"));
        JsonArrayBuilder files = Json.createArrayBuilder();
        for (final var fileEntry : new File("Files/").listFiles()) {
            if (fileEntry.isFile()) {
                files.add(fileEntry.getName());
            }
        }
        JsonObjectBuilder reader = Json.createObjectBuilder().add("LIST", files);
        JsonObject jsonObject = reader.build();

        return new  ResponseEntity<>( prettyPrintJson(jsonObject), HttpStatus.ACCEPTED);
    }


    @PostMapping("/save")
    public ResponseEntity<String> CreateFile(@RequestParam(name = "name", required = true, defaultValue = "file.xml") String name, @RequestBody(required = true) String body) {
        try {
            var text = decompressGzipBase64ToString(body);
            var file = new File(saveDir + name);
            if(!file.exists()) {
                file.createNewFile();
            }
            var fw = new FileWriter(file.getAbsoluteFile());
            var bw = new BufferedWriter(fw);
            bw.write( text );
            bw.close();
            fw.close();
            //System.out.println("Saved " + text.length());
            logger.info("Saved " + name);
        } catch (IOException e) {
            logger.error("Error create file: \n\t" + e.getMessage());
            return new  ResponseEntity<>( "NOT OK", HttpStatus.BAD_REQUEST);
        }
        return new  ResponseEntity<>( "OK", HttpStatus.CREATED);
    }

    @PostMapping("/get")
    public ResponseEntity<String> GetFile(@RequestParam(name = "name", required = true, defaultValue = "file.xml") String name) {
        byte[] bytes;
        try {
            var file = new File(saveDir + name);
            bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
            if(!file.exists()) {
                throw new IOException("No file");
            }

            logger.info("Sended " + name);
            return new  ResponseEntity<>(compressStringToGzipBase64(new String(bytes)), HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error create file: \n\t" + e.getMessage());
            return new  ResponseEntity<>( "NOT OK", HttpStatus.BAD_REQUEST);
        }
    }


    // честно, лень ручками
    public  String prettyPrintJson(JsonObject jsonObject) {
        Map<String, Object> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);

        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();

        try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.writeObject(jsonObject);
        }

        String prettyJson = stringWriter.toString();
        return prettyJson;
    }
    public String decompressGzipBase64ToString(String compressedBase64) throws IOException {
        if (compressedBase64 == null || compressedBase64.length() == 0) {
            return null;
        }
        byte[] compressedBytes = Base64.getDecoder().decode(compressedBase64);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedBytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        gzipInputStream.close();
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
    }
    public String compressStringToGzipBase64(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
        gzipOutputStream.close();
        byte[] compressedBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(compressedBytes);
    }
}
