/**
 * Figma MCP script for repo/figma-documentation-sync/docs/runbooks/trunk-sync.md.
 *
 * Execute the generated, untracked sync-trunk-design-model.mcp.js with the Figma MCP use_figma tool.
 * Replace DESIGN_MODEL in the generated JavaScript with the JSON artifact
 * published by TeamCity Figma Sync > Generate main design model.
 */
import { createFigmaDesignModelSync } from "./create-figma-design-model-sync";
import type { DesignModel, SyncFigmaDesignModelOptions } from "../domain/design-model";

export async function main(
  designModel: DesignModel | undefined,
  options?: SyncFigmaDesignModelOptions
) {
  return createFigmaDesignModelSync()(designModel, options);
}
