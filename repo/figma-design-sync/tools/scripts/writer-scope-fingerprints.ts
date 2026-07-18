import { createHash } from "node:crypto";
import { readdir, readFile } from "node:fs/promises";
import { join, relative } from "node:path";
import {
  CATALOG_TARGET_NAMES,
  WRITER_TARGET_NAMES,
} from "@figma-design-sync/project-config";

export const WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION = 1;

const WRITER_TARGETS = WRITER_TARGET_NAMES;

const CATALOG_TARGETS = CATALOG_TARGET_NAMES;

export async function createWriterScopeFingerprints({
  sourceRoot,
  repositoryRoot,
  policyPath,
  scopes,
}) {
  const policy = JSON.parse(await readFile(policyPath, "utf8"));
  if (policy.schemaVersion !== 1) {
    throw new Error(
      `Unsupported Figma change-impact policy schema ${policy.schemaVersion}; expected 1.`
    );
  }

  const rules = policy.figmaVisualTargetRules || [];
  validateRuleTargets(rules);
  const excludedPatterns = policy.figmaTransportOnlyPaths || [];
  const sourceFiles = (await listFiles(sourceRoot)).filter((sourceFile) =>
    !matchesAny(normalizePath(relative(repositoryRoot, sourceFile)), excludedPatterns)
  );
  if (sourceFiles.length === 0) {
    throw new Error(`No Figma writer sources were found under ${sourceRoot}.`);
  }

  const sourcesByTarget = Object.fromEntries(WRITER_TARGETS.map((target) => [target, []]));
  for (const sourceFile of sourceFiles) {
    const repositoryPath = normalizePath(relative(repositoryRoot, sourceFile));
    const sourceHash = sha256(await readFile(sourceFile));
    const matchedTargets = new Set(
      rules
        .filter((rule) => matchesAny(repositoryPath, rule.paths || []))
        .flatMap((rule) => rule.targets || [])
    );
    const affectedTargets = matchedTargets.size > 0 ? [...matchedTargets] : WRITER_TARGETS;
    for (const target of affectedTargets) {
      sourcesByTarget[target].push({ path: repositoryPath, sourceHash });
    }
  }

  const targetFingerprints = Object.fromEntries(WRITER_TARGETS.map((target) => [
    target,
    sha256(stableJson({
      schemaVersion: WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION,
      target,
      sources: sourcesByTarget[target].sort((left, right) => left.path.localeCompare(right.path)),
    })),
  ]));

  const requestedScopes = [...new Set([...(scopes || []), "metadata"])];
  return Object.fromEntries(requestedScopes.map((scope) => [
    scope,
    targetFingerprints[targetForScope(scope)],
  ]));
}

function validateRuleTargets(rules) {
  const unknownTargets = [...new Set(rules.flatMap((rule) => rule.targets || []))]
    .filter((target) => !WRITER_TARGETS.includes(target));
  if (unknownTargets.length > 0) {
    throw new Error(`Unknown Figma writer target(s) in change-impact policy: ${unknownTargets.join(", ")}.`);
  }
}

function targetForScope(scope) {
  if (WRITER_TARGETS.includes(scope)) {
    return scope;
  }
  const catalogTarget = [...CATALOG_TARGETS]
    .sort((left, right) => right.length - left.length)
    .find((target) => scope.startsWith(`${target}.`));
  if (catalogTarget) {
    return catalogTarget;
  }
  throw new Error(`Unknown Figma writer execution scope '${scope}'.`);
}

async function listFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const nested = await Promise.all(entries.map((entry) => {
    const path = join(directory, entry.name);
    return entry.isDirectory() ? listFiles(path) : [path];
  }));
  return nested.flat().sort();
}

function matchesAny(path, patterns) {
  return patterns.some((pattern) => globRegex(pattern).test(path));
}

function globRegex(pattern) {
  const expression = normalizePath(pattern)
    .replace(/[.+^${}()|[\]\\]/g, "\\$&")
    .replace(/\*/g, ".*")
    .replace(/\?/g, ".");
  return new RegExp(`^${expression}$`, "i");
}

function normalizePath(path) {
  return path.trim().replace(/\\/g, "/");
}

function sha256(value) {
  return `sha256:${createHash("sha256").update(value).digest("hex")}`;
}

function stableJson(value) {
  if (Array.isArray(value)) {
    return `[${value.map(stableJson).join(",")}]`;
  }
  if (value && typeof value === "object") {
    return `{${Object.keys(value).sort().map((key) =>
      `${JSON.stringify(key)}:${stableJson(value[key])}`
    ).join(",")}}`;
  }
  return JSON.stringify(value);
}
