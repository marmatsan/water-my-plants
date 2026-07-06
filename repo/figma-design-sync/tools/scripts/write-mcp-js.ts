import { readFile, writeFile } from "node:fs/promises";

const sourcePath = new URL("./sync-trunk-design-model.mcp.js", import.meta.url);
const targetPath = new URL("../sync-trunk-design-model.mcp.js", import.meta.url);
const generatedHeader = "// Generated from sync-trunk-design-model.mcp.ts. Do not edit directly.\n";
const designModelPlaceholder = "const DESIGN_MODEL = undefined;\n";
const syncOptionsPlaceholder = "const SYNC_OPTIONS = undefined;\n";

const compiledSource = await readFile(sourcePath, "utf8");
const mcpSource = `${generatedHeader}${designModelPlaceholder}${syncOptionsPlaceholder}${compiledSource}\nreturn await FigmaTrunkSync.main(DESIGN_MODEL, SYNC_OPTIONS);\n`;

await writeFile(targetPath, mcpSource, "utf8");
