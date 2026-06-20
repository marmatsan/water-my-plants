export function libraryArtifactNames(entries) {
  return libraryArtifacts(entries).map((artifact) => artifact.name);
}

export function libraryArtifacts(entries) {
  return entries.flatMap((entry) => {
    if (entry.type === "artifact") {
      const requiredByModules = sortedUnique(entry.requiredByModules || []);
      return [{ name: entry.artifact, requiredByModules }];
    }
    if (entry.type === "bundle") {
      return (entry.artifacts || []).map((artifact) => ({ name: artifact, requiredByModules: [] }));
    }
    throw new Error(`Unsupported library catalog entry type '${entry.type}'.`);
  });
}

export function libraryBundles(entries) {
  return entries
    .filter((entry) => entry.type === "bundle")
    .map((entry) => ({
      alias: entry.alias,
      requiredByModules: sortedUnique(entry.requiredByModules || []),
    }));
}

export function libraryArtifactVersions(entries) {
  return entries.flatMap((entry) => {
    const version = entry.version;
    if (!version?.visible || !version.value) return [];
    if (entry.type === "artifact") return [version.value];
    if (entry.type === "bundle") return (entry.artifacts || []).map(() => version.value);
    throw new Error(`Unsupported library catalog entry type '${entry.type}'.`);
  });
}

export function sortedUnique(values) {
  return [...new Set(values)].sort();
}
