# Figma Design Sync Tools

This package contains the portable TypeScript writer and the checkpointed MCP
runner tooling. It is built with a repository-owned `figma-config.ts` so Figma
node identities and visual targets remain outside the reusable writer.

Prepare a configured tool workspace with:

```powershell
npx figma-design-sync-build `
    --project-config=path\to\figma-config.ts `
    --output-dir=build\figma-design-sync-tools
```

The package remains `private` until a release is explicitly authorized. The
publication runbook describes the release gate and version alignment contract.
