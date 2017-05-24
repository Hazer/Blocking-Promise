# Blocking-Promise
Async Callbacks to Sync Future helper

[ ![Download](https://api.bintray.com/packages/hazer/maven/blocking-promise/images/download.svg) ](https://bintray.com/hazer/maven/blocking-promise/_latestVersion)

## Basic Usage (Kotlin)
```Kotlin
val promise = makePromise<String> { complete ->
  val message: String = "First"

  thread(start = true) {
    Thread.sleep(3000) // Do async operation
    complete.success(message)
  }
}

println(promise.get()) // Block current thread waiting for async operation.
```

## Basic Usage (Java)
```Java
Future<String> promise = BlockingPromises.makeFrom(complete -> {
  final String message = "lasmlaskf";
  new Thread() {
    @Override
    public void run() {
      Thread.sleep(3000); // Do async operation
      complete.success(message);
    }
  }.start();
});

String message = promise.get(); // Block current thread waiting for async operation.
System.out.println(message);
```

