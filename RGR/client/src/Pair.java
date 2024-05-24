public class Pair<a,b> {
    a value1;
    b value2;
    public Pair(a value1, b value2) {
        this.value1 = value1; this.value2=value2;
    }
    public a getFirst() {
        return value1;
    }
    public b getSecond() {
        return value2;
    }
}
