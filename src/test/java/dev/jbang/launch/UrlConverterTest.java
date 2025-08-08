package dev.jbang.launch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UrlConverter Tests")
class UrlConverterTest {

    @Test
    @DisplayName("commandToUrl should convert simple command line to URL")
    void commandToUrl_SimpleCommand() {
        // Given
        String[] args = {"jbang", "run", "Hello.java"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run/Hello.java");
    }

    @Test
    @DisplayName("commandToUrl should handle arguments with spaces")
    void commandToUrl_ArgumentsWithSpaces() {
        // Given
        String[] args = {"jbang", "run", "Hello World.java"};

        // When
        URI result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toString()).isEqualTo("jbang:///run/Hello%20World.java");
    }

    @Test
    @DisplayName("commandToUrl should handle special characters")
    void commandToUrl_SpecialCharacters() {
        // Given
        String[] args = {"jbang", "run", "file@with#special$chars.java"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then - RFC 3986 only encodes reserved characters: # is reserved, @ and $ are not
        assertThat(result.toString()).isEqualTo("jbang:///run/file@with%23special$chars.java");
    }

    @Test
    @DisplayName("commandToUrl should handle unicode characters")
    void commandToUrl_UnicodeCharacters() {
        // Given
        String[] args = {"jbang", "run", "café.java"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then - RFC 3986 allows unicode characters in paths
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run/caf%C3%A9.java");
    }

    @Test
    @DisplayName("commandToUrl should handle multiple arguments")
    void commandToUrl_MultipleArguments() {
        // Given
        String[] args = {"jbang", "run", "Hello.java", "--verbose", "--debug"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run/Hello.java/--verbose/--debug");
    }

    @Test
    @DisplayName("commandToUrl should handle empty arguments")
    void commandToUrl_EmptyArguments() {
        // Given
        String[] args = {"jbang", "run", "", "test.java"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run//test.java");
    }

    @Test
    @DisplayName("commandToUrl should handle multiple empty arguments")
    void commandToUrl_MultipleEmptyArguments() {
        // Given
        String[] args = {"jbang", "run", "", "", "test.java"};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run///test.java");
    }

    @Test
    @DisplayName("commandToUrl should handle trailing empty argument")
    void commandToUrl_TrailingEmptyArgument() {
        // Given
        String[] args = {"jbang", "run", "test.java", ""};

        // When
        var result = UrlConverter.commandToUrl(args);

        // Then
        assertThat(result.toASCIIString()).isEqualTo("jbang:///run/test.java/");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("commandToUrl should throw exception for null or empty args")
    void commandToUrl_ThrowsExceptionForNullOrEmpty(String[] args) {
        assertThatThrownBy(() -> UrlConverter.commandToUrl(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Command line arguments cannot be null or empty");
    }

    @Test
    @DisplayName("commandToUrl should throw exception for single argument")
    void commandToUrl_ThrowsExceptionForSingleArgument() {
        // Given
        String[] args = {"jbang"};

        // When/Then
        assertThatThrownBy(() -> UrlConverter.commandToUrl(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Command line must have at least 2 arguments (jbang <command>)");
    }

    @Test
    @DisplayName("urlToCommand should convert simple URL to command line")
    void urlToCommand_SimpleUrl() {
        // Given
        String url = "jbang:///run/Hello.java";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "Hello.java");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with encoded spaces")
    void urlToCommand_UrlWithEncodedSpaces() {
        // Given
        String url = "jbang:///run/Hello%20World.java";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "Hello World.java");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with special characters")
    void urlToCommand_UrlWithSpecialCharacters() {
        // Given
        String url = "jbang:///run/file%40with%23special%24chars.java";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "file@with#special$chars.java");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with unicode characters")
    void urlToCommand_UrlWithUnicodeCharacters() {
        // Given
        String url = "jbang:///run/caf%C3%A9.java";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "café.java");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with multiple segments")
    void urlToCommand_UrlWithMultipleSegments() {
        // Given
        String url = "jbang:///run/Hello.java/--verbose/--debug";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "Hello.java", "--verbose", "--debug");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with empty segments")
    void urlToCommand_UrlWithEmptySegments() {
        // Given
        String url = "jbang:///run//test.java";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "", "test.java");
    }

    @Test
    @DisplayName("urlToCommand should handle URL with trailing slash")
    void urlToCommand_UrlWithTrailingSlash() {
        // Given
        String url = "jbang:///run/Hello.java/";

        // When
        List<String> result = UrlConverter.urlToCommand(url);

        // Then
        assertThat(result).containsExactly("jbang", "run", "Hello.java", "");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("urlToCommand should throw exception for null or empty URL")
    void urlToCommand_ThrowsExceptionForNullOrEmpty(String url) {
        assertThatThrownBy(() -> UrlConverter.urlToCommand(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL cannot be null or empty");
    }

    @Test
    @DisplayName("urlToCommand should throw exception for invalid URL")
    void urlToCommand_ThrowsExceptionForInvalidUrl() {
        // Given
        String url = "invalid://url:with:colons";

        // When/Then
        assertThatThrownBy(() -> UrlConverter.urlToCommand(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing path in invalid://url:with:colons");
    }

    @Test
    @DisplayName("urlToCommand should throw exception for URL without path")
    void urlToCommand_ThrowsExceptionForUrlWithoutPath() {
        // Given
        String url = "jbang://";

        // When/Then
        assertThatThrownBy(() -> UrlConverter.urlToCommand(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid URL: jbang://");
    }

    @Test
    @DisplayName("urlToCommandString should convert URL to quoted command string")
    void urlToCommandString_SimpleUrl() {
        // Given
        String url = "jbang:///run/Hello.java";

        // When
        String result = UrlConverter.urlToCommandString(url);

        // Then
        assertThat(result).isEqualTo("jbang run Hello.java");
    }

    @Test
    @DisplayName("urlToCommandString should quote arguments with spaces")
    void urlToCommandString_ArgumentsWithSpaces() {
        // Given
        String url = "jbang:///run/Hello%20World.java";

        // When
        String result = UrlConverter.urlToCommandString(url);

        // Then
        assertThat(result).isEqualTo("jbang run \"Hello World.java\"");
    }

    @Test
    @DisplayName("urlToCommandString should quote arguments with quotes")
    void urlToCommandString_ArgumentsWithQuotes() {
        // Given
        String url = "jbang:///run/file%22with%22quotes.java";

        // When
        String result = UrlConverter.urlToCommandString(url);

        // Then
        assertThat(result).isEqualTo("jbang run \"file\\\"with\\\"quotes.java\"");
    }

    @Test
    @DisplayName("urlToCommandString should handle multiple arguments with mixed quoting")
    void urlToCommandString_MultipleArgumentsWithMixedQuoting() {
        // Given
        String url = "jbang:///run/Hello.java/--verbose/--name%3DJohn%20Doe";

        // When
        String result = UrlConverter.urlToCommandString(url);

        // Then
        assertThat(result).isEqualTo("jbang run Hello.java --verbose \"--name=John Doe\"");
    }

    @ParameterizedTest
    @CsvSource({
        "jbang:///run/Hello.java, jbang run Hello.java",
        "jbang:///run/Hello%20World.java, jbang run \"Hello World.java\"",
        "jbang:///run/file%40test.java, jbang run file@test.java",
        "jbang:///run/--verbose/--debug, jbang run --verbose --debug"
    })
    @DisplayName("urlToCommandString should handle various URL patterns")
    void urlToCommandString_VariousPatterns(String url, String expected) {
        // When
        String result = UrlConverter.urlToCommandString(url);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Round-trip conversion should preserve original command")
    void roundTripConversion_PreservesOriginalCommand() {
        // Given
        String[] originalArgs = {"jbang", "run", "Hello World.java", "--verbose", "--debug"};

        // When
        var url = UrlConverter.commandToUrl(originalArgs);
        List<String> convertedArgs = UrlConverter.urlToCommand(url.toString());

        // Then
        assertThat(convertedArgs).containsExactly(originalArgs);
    }

    @Test
    @DisplayName("Round-trip conversion should preserve special characters")
    void roundTripConversion_PreservesSpecialCharacters() {
        // Given
        String[] originalArgs = {"jbang", "run", "file@with#special$chars.java", "--name=café"};

        // When
        var url = UrlConverter.commandToUrl(originalArgs);
        List<String> convertedArgs = UrlConverter.urlToCommand(url.toString());

        // Then
        assertThat(convertedArgs).containsExactly(originalArgs);
    }

    @Test
    @DisplayName("Round-trip conversion should preserve empty arguments")
    void roundTripConversion_PreservesEmptyArguments() {
        // Given
        String[] originalArgs = {"jbang", "run", "", "test.java"};

        // When
        var url = UrlConverter.commandToUrl(originalArgs);
        List<String> convertedArgs = UrlConverter.urlToCommand(url.toString());

        // Then
        assertThat(convertedArgs).containsExactly(originalArgs);
    }
} 