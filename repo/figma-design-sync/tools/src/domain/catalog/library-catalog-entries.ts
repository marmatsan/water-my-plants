export function libraryArtifactNames(entries) {
  return libraryArtifacts(entries).map((artifact) => artifact.name);
}

export function libraryArtifacts(entries) {
  return entries.flatMap((entry) => {
    if (entry.type === "artifact") {
      const providedByConventionPlugins = sortedConventionPluginUsages(entry.providedByConventionPlugins || []);
      const requiredByModules = effectiveRequiredByModules(entry.requiredByModules || [], providedByConventionPlugins);
      return [{ name: entry.artifact, requiredByModules, providedByConventionPlugins }];
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
    .map((entry) => {
      const providedByConventionPlugins = sortedConventionPluginUsages(entry.providedByConventionPlugins || []);
      return {
        alias: entry.alias,
        requiredByModules: effectiveRequiredByModules(entry.requiredByModules || [], providedByConventionPlugins),
        providedByConventionPlugins,
      };
    });
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

function sortedConventionPluginUsages(usages) {
  return [...usages]
    .map((usage) => ({
      pluginId: usage.pluginId,
      pluginModule: usage.pluginModule,
      requiredByModules: sortedUnique(usage.requiredByModules || []),
    }))
    .sort((first, second) =>
      first.pluginId.localeCompare(second.pluginId) ||
        first.pluginModule.localeCompare(second.pluginModule)
    );
}

function effectiveRequiredByModules(requiredByModules, providedByConventionPlugins) {
  return sortedUnique([
    ...requiredByModules,
    ...providedByConventionPlugins.flatMap((usage) => usage.requiredByModules || []),
  ]);
}
