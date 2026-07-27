# Module figmaDocumentationSync-teamcity-adapter

Optional TeamCity adapter for the portable Figma documentation sync pipeline.

This module translates TeamCity-generated configuration and command-line
operations into portable domain and data contracts. Reusable domain, data, and
plugin modules must not depend on this adapter.

# Package com.marmatsan.figmaDocumentationSync.teamcityAdapter

TeamCity clients and configuration providers isolate TeamCity file formats,
command execution, and authentication-facing operations from the portable
Figma documentation sync implementation.
