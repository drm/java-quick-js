# java-quick-js

This provides a Java Native Interface (JNI) to
[QuickJS](https://bellard.org/quickjs/).

It's stable and I see no reason not to start using it in production, though
I haven't myself.

Be sure to stress test your implementation, as quickjs will break without
warning if memory issues occur.


## API compatibility

This implementation currently only supports the 'lambda'-approach of
javascripts, i.e., useful for server-side rendering but not for server-side
logic. Accessing files, server resources and other I/O are not tested nor
supported, for now.

It is probably not hard to implement, so let me know if you're interested in
either contributing or persuading me to implement it.

## Preliminary testing results

I started this project to research performance in comparison with V8.
Obviously, V8 is much more sophisticated and optimized, so for common
workloads, V8 is probably the better choice.

However, quickjs seems to outperform V8 by an order of magnitude, for *small*
one-off calls and low memory consumption. Also, the garbage collector is
deterministic and it's therefore much easier to hold the reign of this puppy
than to the monstrous power of V8.

I haven't really devised any definitive performance test, but based on some
real-world testing, V8 starts to outshine at higher memory consumption and JIT
optimisation by a landslide. Yes, dear frontenders, a 5MB bundle file of JS
code may be a bit much... Maybe the backend really isn't the problem here.

Nonetheless, if you don't expect high loads, and you're here for simplicity
rather than performance, QuickJS is much easier to build (takes about 25
seconds on my machine, rather than the 20+ minutes (!) for V8...).

Take a look at [mv8](https://github.com/drm/mv8) if you need V8 bindings in
Java. It's being used successfully in multiple high-traffic websites
notwithstanding the lack of documentation. Feel free to drop me a note if you
need help.

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

The design of qjs is such that you can cheaply create contexts and duplicate
them, so you can have a blueprint of the context, preloaded with you libraries,
and duplicate that template to actually execute your code in.

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

### More scenario's

Read the tests for more implementation examples.

# Questions, comments or remarks?

Feel free to ping me at 
[https://github.com/drm/java-quick-js](https://github.com/drm/java-quick-js) 
by creating an issue or starting a discussion.
