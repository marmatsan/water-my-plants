# Importing PlantUML SVGs Into Figma

PlantUML `.puml` files are the source of truth. Rendered SVGs are publication artifacts used to place the reviewed diagram in Figma.

## Import Path

Figma `upload_assets` does not support SVG. Import SVG diagrams with `use_figma` and `figma.createNodeFromSvg()`.

PlantUML SVG output can be valid for browsers but still fail in `createNodeFromSvg()`. Before importing, sanitize the SVG:

- Remove PlantUML comments and XML metadata.
- Remove non-essential attributes such as `contentStyleType`, `data-diagram-type`, `zoomAndPan`, `version`, and `xmlns:xlink`.
- Keep text layout hints such as `lengthAdjust` and `textLength` for the first import attempt; PlantUML uses them to keep text aligned with its calculated containers.
- If `createNodeFromSvg()` rejects a complex diagram, create a stricter temporary SVG by removing non-essential attributes such as `class`, `data-*`, and `preserveAspectRatio`, then test that stricter SVG before changing the `.puml`.
- Do not remove `lengthAdjust`, `textLength`, or `stroke-dasharray` as a first-line fix. Sequence diagrams rely on those attributes to keep participant labels centered and to distinguish lifelines and return messages from normal calls. If they must be removed for compatibility, visually correct the imported Figma nodes before treating the diagram as published.
- Convert inline `style="..."` declarations to explicit SVG attributes.
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

For medium or large SVGs, avoid passing one large base64 string directly inside
`use_figma`. Split the base64 into small chunks, join the chunks in the plugin,
then decode:

```javascript
const chunks = [
  "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmci...",
  "..."
]

const svg = atob(chunks.join(""))
const node = figma.createNodeFromSvg(svg)
```

Before making the real edit, run a non-destructive import probe:

```javascript
const node = figma.createNodeFromSvg(svg)
const result = { type: node.type, width: node.width, height: node.height }
node.remove()
return result
```

This separates transport failures from SVG compatibility failures. In this MCP
environment, `clientStorage.setAsync()` is not supported and `fetch` may not be
available inside the plugin runtime, so chunked inline transport is the most
reliable path for PlantUML SVGs that are too large for a single string.

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

For sequence diagrams, also inspect dashed semantics after import:

- Participant lifelines should remain dashed.
- Return messages should remain dashed.
- Normal call messages should remain solid.

If a compatibility import flattened dash styling, restore `dashPattern` on the imported Figma line/vector nodes instead of changing the `.puml` source.

If a vertical lifeline is missing after import, do not recreate it from scratch
with a new Figma `LINE` node as the first fix. Zero-width vertical lines may not
render reliably through the MCP path, and rotated replacement lines can change
the section bounds or appear above activation rectangles. Clone an existing
imported lifeline group instead, move the clone to the missing participant, and
copy the original line's exact visual properties:

- `strokes`
- `strokeWeight`
- `dashPattern`
- `opacity`

Imported PlantUML vectors can use fractional stroke weights. In the
`model-generation-flow.puml` import, the original lifelines used
`strokeWeight = 0.34229177236557007`, while manually created/cloned Figma lines
defaulted to a visually thicker `0.5`. Always copy the value from an existing
imported lifeline instead of guessing a rounded weight.

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
- Avoid relying on `clientStorage` for chunk staging; this MCP runtime may reject `clientStorage.setAsync()`.
- For large SVGs, prefer generated base64 chunks over one hand-written base64 string.
- Lock the Figma diagram section after placing the generated SVG.
