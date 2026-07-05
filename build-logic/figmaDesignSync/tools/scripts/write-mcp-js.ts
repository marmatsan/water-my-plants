import { readFile, writeFile } from "node:fs/promises";

const sourcePath = new URL("./sync-trunk-design-model.mcp.js", import.meta.url);
const targetPath = new URL("../sync-trunk-design-model.mcp.js", import.meta.url);
const generatedHeader = "// Generated from sync-trunk-design-model.mcp.ts. Do not edit directly.\n";
const designModelPlaceholder = "const DESIGN_MODEL = undefined;\n";

const compiledSource = await readFile(sourcePath, "utf8");
const mcpSource = `${generatedHeader}${designModelPlaceholder}${compiledSource}\nreturn await FigmaTrunkSync.main(DESIGN_MODEL);\n`;

await writeFile(targetPath, mcpSource, "utf8");
