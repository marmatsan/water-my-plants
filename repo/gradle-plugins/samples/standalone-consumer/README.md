# Standalone Gradle Plugin Consumer

This fixture resolves every repository convention-plugin marker from the staged
Maven repository. It applies the unit-test convention against a consumer-owned
`libs` catalog and deliberately contains no `includeBuild` declaration.
