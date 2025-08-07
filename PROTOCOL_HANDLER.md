# JBang Protocol Handler

This application includes support for the `jbang://` protocol, allowing it to handle URLs in the format `jbang://<command>/<arg1>/<arg2>` etc.

## Protocol Format

The jbang protocol follows this format:
```
jbang://<command>/<arg1>/<flag1>/<arg2>/<flag2>/...
```

Where each arg or flag gets to be their own path segment with special characters URL encoded.

Examples:
| Command                                 | URL Format                                                      |
|------------------------------------------|-----------------------------------------------------------------|
| `jbang run hello.java`                   | `jbang://run/hello.java`                                        |
| `jbang run --fresh example@latest`       | `jbang://run/--fresh/example@latest`                            |
| `jbang run github:jbangdev/jbang-action@main` | `jbang:///run/github%3Ajbangdev%2Fjbang-action%40main`     |

The intent is to be url to use this url format to run java apps from anywhere with minimal setup.

## How It Works

1. When a `jbang://` URL is clicked or accessed, the operating system routes it to `jbang-launch`
2. `jbang-launch` parses the URL to extract the full jbang command
3. The parsed information is then processed and for security reasons shown to the user
4. User can then aceept or decline to run the command.

## Platform Support

Each OS supports URL handlers differently, below they are summarized with links to how it is accomplished.

### General

We use `jpackage` to create a portable installer that ensures things get installed properly. `jpackage` itself does
not have all the defaults to handle URL protocol but can be tweaked per platform. Below lists them.

### Linux
- Uses a `.desktop` file for desktop integration
- The `MimeType=x-scheme-handler/jbang` entry registers the protocol handler
- The `Exec` includes a magic expansion variable `%u` that only has a value if opened via URL protocol
- May require manual registration depending on the distribution

### Windows
- In [`main.wxs`](src/jpackage/windows/main.wxs) we have a copy of `jpackage` default install setup + registry key changes
- Look for `ProtocolHandlers` to see the details.

### macOS
- Protocol registration is handled through the [`Info.plist`](src/jpackage/osx/Info.plist) file in the application bundle.
  Majority is copy of `jpackage` defaults, look for `BundleURL` to see the important extra parts.
- Opposite to Linux and Windows where the url is passed as command line arguments, on OSX protocol handlers are sent as events after the app has started.
    See `installURIListener()` how that is done. 

