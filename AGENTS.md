## Android UI tokens / theme
- Design system resources (colors, typography, shapes) live in:
  core/core_ui/src/main/kotlin/com/marmatsan/core_ui/theme
- When implementing UI, import and use those tokens.
- Do not hardcode colors in features; reference core_ui/theme.
- If you need a new token, add it in core_ui/theme and thread it through the design system.
- Compose previews: when a composable has variants, previews must use a PreviewParameterProvider to render those variants.
- Composable functions: all parameters must be named and each parameter must be on its own line. This includes compose-related functions (e.g., Modifier usage).
- Files generated must not end with an empty line.
