package dev.jbang.launch;

import java.util.List;

public class UrlConverterTest {
    public static void main(String[] args) {
        test("jbang://run/hello.java");
        test("jbang:///run/hello.java");
    }

    private static void test(String url) {
        List<String> expected = List.of("jbang", "run", "hello.java");
        List<String> actual = UrlConverter.urlToCommand(url);
        if (!actual.equals(expected)) {
            throw new AssertionError("Expected " + expected + " but was " + actual + " for url: " + url);
        }
    }
}
