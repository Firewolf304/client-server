import netscape.javascript.JSObject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class Request {
    private String body = "";
    private URI url;
    private ArrayList<Pair> headers = new ArrayList<>(Arrays.asList(new Pair[]{
            new Pair<String, String>("User-Agent", "LittleShit/0.0")
    }));
    Request(URI uri, ArrayList<Pair> headers, String body) {
        this.headers = headers; this.url = uri;
         this.body = body;
    }
    Request(URI uri, String body) {
        this.headers = headers; this.url = uri;
        this.body = body;
    }
    Request(URI uri) {
        this.headers = headers; this.url = uri;
        this.body = body;
    }

    void putHeader(String value1, String value2) {
        headers.add(new Pair<String, String>(value1, value2));
    }
    void pushBody(String value) {
        this.body = value;
    }
    URI getURL() {return url;}
    String getBody() {return body;}
    ArrayList<Pair> getHeaders() {return headers;}
}
