# Unit Testing

This autonomous included build owns portable Kotlin test APIs. It intentionally
contains no Gradle convention plugins and has no filesystem dependency on any
sibling included build.

- `unit-test-dsl/` publishes `com.marmatsan.repo:unit-test-dsl`.
- `samples/standalone-consumer/` proves Maven consumption without `includeBuild`.
- `versions.properties` owns the build and publication versions used here.

Run its complete contract with:

```powershell
.\gradlew.bat -p repo/unit-testing check verifyStagedPublication
```
