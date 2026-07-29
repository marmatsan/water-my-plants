export function libraryArtifacts(entries) {
  requireSupportedLibraryEntries(entries);

  return entries
    .filter((entry) => entry.type === "artifact")
    .map((entry) => {
      const providedByConventionPlugins = sortedConventionPluginUsages(entry.providedByConventionPlugins || []);
      const configuredByConventionPlugins = sortedConventionPluginConfigurationUsages(entry.configuredByConventionPlugins || []);
      const requiredByModules = effectiveRequiredByModules(entry.requiredByModules || [], providedByConventionPlugins);
      return {
        name: entry.artifact,
        version: entry.version,
        requiredByModules,
        providedByConventionPlugins,
        configuredByConventionPlugins,
        isCatalogEntry: true,
      };
    });
}

export function libraryBundles(entries) {
  requireSupportedLibraryEntries(entries);

  return entries
    .filter((entry) => entry.type === "bundle")
    .map((entry) => {
      const providedByConventionPlugins = sortedConventionPluginUsages(entry.providedByConventionPlugins || []);
      return {
        alias: entry.alias,
        version: entry.version,
        artifacts: (entry.artifacts || []).map((artifact) => ({
          name: artifact,
          version: {
            value: null,
            visible: false,
          },
          requiredByModules: [],
          providedByConventionPlugins: [],
          configuredByConventionPlugins: [],
          isCatalogEntry: false,
        })),
        requiredByModules: effectiveRequiredByModules(entry.requiredByModules || [], providedByConventionPlugins),
        providedByConventionPlugins,
        isCatalogEntry: true,
      };
    });
}

function requireSupportedLibraryEntries(entries) {
  const unsupportedEntry = entries.find((entry) => entry.type !== "artifact" && entry.type !== "bundle");
  if (unsupportedEntry) {
    throw new Error(`Unsupported library catalog entry type '${unsupportedEntry.type}'.`);
  }
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

function sortedConventionPluginConfigurationUsages(usages) {
  return [...usages]
    .map((usage) => ({
      pluginId: usage.pluginId,
      pluginModule: usage.pluginModule,
      target: usage.target,
    }))
    .sort((first, second) =>
      first.pluginId.localeCompare(second.pluginId) ||
        first.pluginModule.localeCompare(second.pluginModule) ||
        first.target.localeCompare(second.target)
    );
}

function effectiveRequiredByModules(requiredByModules, providedByConventionPlugins) {
  return sortedUnique([
    ...requiredByModules,
    ...providedByConventionPlugins.flatMap((usage) => usage.requiredByModules || []),
  ]);
}
