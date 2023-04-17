# java-quickjs-jni

This provides a Java Native Interface (JNI) to
[QuickJS](https://bellard.org/quickjs/).

## API compatibility

This implementation currently only supports the 'lambda'-approach of javascripts,
i.e., useful for server-side rendering but not for server-side logic. Accessing
files, server resources and other I/O are not tested nor supported, for now.

It is probably not hard to implement, so let me know if you're interested in 
either contributing or persuading me to implement it.

## Preliminary testing results

Overall, this library seems to outperform [mv8](https://github.com/drm/mv8) by
an order of magnitude, for one-off render calls, which is the primary concern 
of the mv8 implementation too. So, just to be able to render React components 
server-side, this library seems to be the way to go. If not for that, the ease
of use and simple code base really improves the developer experience anyway.
It is not hard to debug it with gdb, for example.

## Usage

### Basic evaluation
```java
import nl.melp.qjs.Qjs;

// ....

try (var rt = Qjs.createRuntime()) {
    try (var c = rt.createContext()) {
        System.out.println(c.eval("['Hello', 'World'].join(' ');"));
    }
} 
```

### Lightweight and efficient context boot-up

The design of qjs is such that you can cheaply create contexts and duplicate them, 
so you can have a blueprint of the context, preloaded with you libraries, and
duplicate that template to actually execute your code in.

To speed up loading the libraries, you can precompile them to bytecode.

```javascript
// helloworld.js
var fn = function (...args)({
    return args.join(' '); 
});
```

```java
import nl.melp.qjs.Qjs;

// ....

Qjs.compile("helloworld.js", "helloworld.js.bin");

try (var rt = Qjs.createRuntime()) {
	try (var template = rt.createContext()) {
        rt.evalBinaryPath(Path.of("helloworld.js.bin"));
		
        for (int i = 0; i < 10000; i++) {
            try (var c = template.duplicate()) {
                System.out.println(c.eval("fn(['Hello', 'World'])"));
            }
        }
    }
} 
```
