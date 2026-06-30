# Importing PlantUML SVGs Into Figma

PlantUML `.puml` files are the source of truth. Rendered SVGs are publication artifacts used to place the reviewed diagram in Figma.

## Import Path

Figma `upload_assets` does not support SVG. Import SVG diagrams with `use_figma` and `figma.createNodeFromSvg()`.

PlantUML SVG output can be valid for browsers but still fail in `createNodeFromSvg()`. Before importing, sanitize the SVG:

- Remove PlantUML comments and XML metadata.
- Remove non-essential attributes such as `contentStyleType`, `data-diagram-type`, `zoomAndPan`, `version`, and `xmlns:xlink`.
- Keep text layout hints such as `lengthAdjust` and `textLength`; PlantUML uses them to keep text aligned with its calculated containers, and removing them can make Figma render text outside the boxes.
- Convert inline `style="stroke:...;stroke-width:..."` values to explicit `stroke` and `stroke-width` attributes.
- Remove root SVG `style="width:...;height:..."`; keep `width`, `height`, and `viewBox`.

Use:

```powershell
build-logic\figmaDesignSync\docs\uml\tools\sanitize-svg-for-figma.ps1 `
  -InputPath build-logic\figmaDesignSync\docs\uml\diagrams\figma-design-sync-figma-design-model-feature.svg `
  -OutputPath tmp\figma-design-model-feature.figma.svg
```

Then pass the sanitized SVG string to:

```javascript
const node = figma.createNodeFromSvg(svg)
```

Place the imported SVG in a Figma diagram section named after the full `.puml`
file name, for example `figma-design-model-feature.puml`. Module diagrams may
be grouped under a parent module section such as `figmaDesignSync`, but each UML
diagram should keep its own section.

After `createNodeFromSvg()` imports the SVG, flatten the Figma wrapper structure
used for the import:

- Move the imported `Group` node directly into the diagram section.
- Remove intermediate wrapper frames such as `*.svg reference` or `*.svg`.
- Center the `Group` horizontally inside the diagram section.

Use this style for diagram sections:

- No fill.
- Stroke bound to `md/sys/color/outline`.
- Stroke align `INSIDE`.
- Stroke weight `2`.
- Locked after publication.

## Text Fidelity

Figma may import PlantUML `<text>` elements as native text nodes and recalculate their width with Figma font metrics. This can make labels appear slightly wider than PlantUML's preview, even when the SVG keeps `textLength` and `lengthAdjust`.

After importing an SVG with native text, inspect the result in Figma. If text is not centered or overflows its PlantUML container, run a small `use_figma` correction that recenters every imported text node inside the rectangular vector block that contains it.

For diagrams that need exact visual fidelity and do not need selectable text in Figma, convert text to paths before import with Inkscape:

```powershell
inkscape tmp\diagram.sanitized.svg `
  --export-type=svg `
  --export-filename=tmp\diagram.outlined.svg `
  --export-text-to-path `
  --export-plain-svg
```

## Notes

- `createNodeFromSvg()` failures are atomic in `use_figma`; failed imports do not leave partial nodes behind.
- Avoid relying on `fetch` inside `use_figma`; the plugin runtime may not expose it.
- For large SVGs, prefer a controlled string or a generated import helper over hand-written base64.
- Lock the Figma diagram section after placing the generated SVG.
