# JBang Protocol Handler

This application includes support for the `jbang://` protocol, allowing it to handle URLs in the format `jbang://<command>/<script-reference>`.

## Protocol Format

The jbang protocol follows this format:
```
jbang://<command>/<script-reference>
```

Examples:
- `jbang://run/hello.java`
- `jbang://edit/example@latest`
- `jbang://run/github:jbangdev/jbang-action@main`

## How It Works

1. When a `jbang://` URL is clicked or accessed, the operating system routes it to this application
2. The application parses the URL to extract the command and script reference
3. The parsed information is then processed (currently just displayed, but can be extended to call the actual jbang implementation)

## Platform Support

### macOS
- Protocol registration is handled through the `Info.plist` file in the application bundle
- Automatically registered when the `.pkg` installer is run via `src/jpackage/osx/Info.plist` 

### Linux
- Uses a `.desktop` file for desktop integration
- The `MimeType=x-scheme-handler/jbang;` entry registers the protocol handler
- May require manual registration depending on the distribution

### Windows
- Includes a batch script (`register-jbang-protocol.bat`) for registry registration
- Must be run with administrator privileges
- Adds registry entries to handle `jbang://` URLs

## Testing

You can test the protocol handler by running:
```bash
java -jar target/jbang-launch-1.0.0-SNAPSHOT.jar "jbang://run/hello.java"
```

Expected output:
```
JBang Protocol Handler
Command: run
Script Reference: hello.java
Full URL: jbang://run/hello.java
```

## Integration with JBang

This is currently a proof-of-concept implementation. To fully integrate with JBang:

1. Replace the `handleJbangProtocol` method with actual JBang API calls
2. Add proper error handling and validation
3. Implement the actual command execution logic
4. Add support for JBang's configuration and caching

## Building and Distribution

The protocol handler files are automatically included in the native packages when building with JReleaser:

```bash
mvn clean package
jreleaser assemble
```

This will create platform-specific packages with protocol handler registration included. 