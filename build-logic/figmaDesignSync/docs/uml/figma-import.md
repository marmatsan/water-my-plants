# Importing PlantUML SVGs Into Figma

PlantUML `.puml` files are the source of truth. Rendered SVGs are publication artifacts used to place the reviewed diagram in Figma.

## Tooling

This module currently uses these tools to render and prepare PlantUML diagrams
for Figma:

| Tool           | Required          | Purpose                                                                                                                | Verification                                                                                          |
|----------------|-------------------|------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| Java           | Yes               | Runs the PlantUML `.jar` when the Chocolatey command is not enough or when a pinned jar is used.                       | `java -version`                                                                                       |
| PlantUML       | Yes               | Renders `.puml` files into SVG publication artifacts.                                                                  | `plantuml -version` or `java -jar C:\ProgramData\chocolatey\lib\plantuml\tools\plantuml.jar -version` |
| Graphviz `dot` | Yes               | Provides layout calculation used by PlantUML for class, component, and other graph-based diagrams.                     | `dot -V`                                                                                              |
| PowerShell     | Yes               | Runs repository helper scripts such as `docs/uml/tools/sanitize-svg-for-figma.ps1`.                                    | `$PSVersionTable.PSVersion`                                                                           |
| Inkscape       | Optional fallback | Converts SVG text to vector paths when Figma recalculates SVG text widths and the diagram needs exact visual fidelity. | `& 'C:\Program Files\Inkscape\bin\inkscape.com' --version`                                            |

Figma MCP tooling is needed for publication, but it is not part of SVG
generation. The SVG generation path is `.puml -> PlantUML/Graphviz -> temporary
SVG -> sanitizer -> Figma import SVG`.

## Import Path

Figma `upload_assets` does not support SVG. Import SVG diagrams with `use_figma` and `figma.createNodeFromSvg()`.

Do not publish PlantUML diagrams as PNG image fills. A PNG can be useful as a
temporary diagnostic fallback when SVG import is failing, but it is not the
accepted Figma publication format for UML documentation. The final Figma node
must come from `figma.createNodeFromSvg(svg)` so Figma contains imported SVG
geometry and text nodes instead of a raster image.

Render temporary SVGs under `tmp/uml/<module>/` instead of placing generated
artifacts next to the `.puml` source. For this module, use:

```text
tmp/uml/figmaDesignSync/
```

The `tmp` directory is ignored by Git. Delete rendered SVGs after they have
been successfully published to Figma.

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
plantuml -tsvg `
  -o "$PWD\tmp\uml\figmaDesignSync" `
  build-logic\figmaDesignSync\docs\uml\diagrams\figma-design-model-feature.puml

build-logic\figmaDesignSync\docs\uml\tools\sanitize-svg-for-figma.ps1 `
  -InputPath tmp\uml\figmaDesignSync\figma-design-model-feature.svg `
  -OutputPath tmp\uml\figmaDesignSync\figma-design-model-feature.figma.svg
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

Generate the base64 chunks from the sanitized SVG file instead of copying or
editing them by hand. Manual chunk editing can corrupt the payload and produce
import failures that look like SVG compatibility problems.

Place the imported SVG in a Figma diagram section named after the full `.puml`
file name, for example `figma-design-model-feature.puml`. Module diagrams may
be grouped under a parent module section such as `figmaDesignSync`, but each UML
diagram should keep its own section.

After `createNodeFromSvg()` imports the SVG, flatten the Figma wrapper structure
used for the import:

- Move the imported `Group` node directly into the diagram section.
- Remove intermediate wrapper frames such as `*.svg reference` or `*.svg`.
- Center the `Group` horizontally inside the diagram section.

Verify the imported section structurally after publication:

- The section should contain an imported `Group`/`FRAME` with many `VECTOR` and
  `TEXT` descendants.
- No descendant should have an `IMAGE` fill. If `imageFillCount` is greater
  than `0`, the diagram was published as a raster image and must be replaced
  with a `createNodeFromSvg()` import.
- The diagram section must stay locked after the SVG group has been placed.

Use this style for diagram sections:

- No fill.
- Stroke bound to `md/sys/color/outline`.
- Stroke align `INSIDE`.
- Stroke weight `2`.
- Locked after publication.

## Text Fidelity

Figma may import PlantUML `<text>` elements as native text nodes and recalculate their width with Figma font metrics. This can make labels appear slightly wider than PlantUML's preview, even when the SVG keeps `textLength` and `lengthAdjust`.

After importing an SVG with native text, inspect the result in Figma. If text is not centered or overflows its PlantUML container, run a small `use_figma` correction that recenters every imported text node inside the rectangular vector block that contains it.

When Figma recalculates text wider than PlantUML, fix the imported Figma nodes
before changing the `.puml` source:

- Find the nearest rectangular UML vector containing the text center.
- Compare the Figma text width with the rectangle width minus the PlantUML
  horizontal margin.
- Reduce only overflowing text nodes by the smallest required ratio.
- Recenter the adjusted text inside its UML rectangle.
- Validate that the section still has `imageFillCount = 0` and no remaining
  text overflow.

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
inkscape tmp\uml\figmaDesignSync\diagram.sanitized.svg `
  --export-type=svg `
  --export-filename=tmp\uml\figmaDesignSync\diagram.outlined.svg `
  --export-text-to-path `
  --export-plain-svg
```

## Notes

- `createNodeFromSvg()` failures are atomic in `use_figma`; failed imports do not leave partial nodes behind.
- Avoid relying on `fetch` inside `use_figma`; the plugin runtime may not expose it.
- Avoid relying on `clientStorage` for chunk staging; this MCP runtime may reject `clientStorage.setAsync()`.
- For large SVGs, prefer generated base64 chunks over one hand-written base64 string.
- Lock the Figma diagram section after placing the generated SVG.
