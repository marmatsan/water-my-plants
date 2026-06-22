// Generated from sync-develop-design-model.mcp.ts. Do not edit directly.
const DESIGN_MODEL = undefined;
var FigmaDevelopSync = (() => {
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

  // src/app/sync-develop-design-model.mcp.ts
  var sync_develop_design_model_mcp_exports = {};
  __export(sync_develop_design_model_mcp_exports, {
    main: () => main
  });

  // src/config/figma-config.ts
  var METADATA_PAGE_ID = "62934:908";
  var METADATA_NAMESPACE = "water_my_plants_sync";
  var VERSIONS_COLLECTION_NAME = "build-logic\\versions.properties";
  var VERSION_ALIAS_MODE_NAME = "Version alias";
  var VERSION_NUMBER_MODE_NAME = "Version number";
  var PROJECT_VERSION_COMPONENT_ID = "63075:591";
  var TREE_NODE_COMPONENT_IDS = {
    Library: "63069:681",
    Plugin: "63069:694"
  };
  var CONNECTOR_TEMPLATE_NAME = "simple-solid_arrow";
  var VERSION_SECTION_TARGETS = {
    "Main project dependencies": {
      parentNodeId: "63075:634",
      variableFolder: "Main project dependencies"
    },
    Libraries: {
      parentNodeId: "63075:644",
      variableFolder: "Libraries"
    },
    Plugins: {
      parentNodeId: "63075:804",
      variableFolder: "Plugins"
    }
  };
  var TREE_NODE_PROPS = {
    libraryGroup: "Library group#1345:12",
    pluginId: "Plugin ID#1345:16",
    showPluginVersion: "Show plugin version#58719:0",
    showArtifacts: "Show artifacts#63079:0",
    pluginVersion: "Plugin version#63081:2",
    showConsumerModule: "Show consumer module#63085:0",
    showIsGradleConventionPlugin: "Show is a gradle convention plugin#63112:4",
    type: "Type"
  };
  var ARTIFACT_PROPS = {
    showConsumerModules: "Show consumer modules#63086:1"
  };
  var ARTIFACTS_BUNDLE_PROPS = {
    showConsumerModules: "Show consumer modules#63107:0"
  };
  var ARTIFACT_INSTANCE_NAME = ".artifact";
  var ARTIFACTS_BUNDLE_INSTANCE_NAME = ".artifacts bundle";
  var MODULE_INSTANCE_NAME = ".module";
  var MODULE_PROPS = {
    name: "name",
    size: "size"
  };
  var SMALL_MODULE_SIZE = "small";
  var BIG_MODULE_SIZE = "big";
  var CATALOG_TREE_TARGETS = [
    {
      name: "waterMyPlants.libraries",
      sectionNodeId: "63069:629",
      type: "Library",
      nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.libraries
    },
    {
      name: "waterMyPlants.plugins",
      sectionNodeId: "63069:594",
      type: "Plugin",
      nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.plugins
    },
    {
      name: "waterMyPlants.customGradleConventionPlugins",
      sectionNodeId: "63216:6907",
      type: "Plugin",
      gradleConventionPluginNodes: true,
      nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.customGradleConventionPlugins
    },
    {
      name: "buildLogic.libraries",
      sectionNodeId: "63099:951",
      type: "Library",
      nodes: (designModel) => designModel.content?.catalogs?.buildLogic?.libraries
    },
    {
      name: "buildLogic.plugins",
      sectionNodeId: "63100:2952",
      type: "Plugin",
      nodes: (designModel) => designModel.content?.catalogs?.buildLogic?.plugins
    }
  ];
  var MODULE_DEPENDENCY_TARGETS = [
    {
      name: "waterMyPlants.moduleDependencies",
      sectionNodeId: "63112:2622",
      dependencies: (designModel) => designModel.content?.moduleDependencies?.main
    },
    {
      name: "buildLogic.moduleDependencies",
      sectionNodeId: "63111:2516",
      dependencies: (designModel) => designModel.content?.moduleDependencies?.buildLogic
    }
  ];

  // src/domain/catalog/flatten-catalog-nodes.ts
  function flattenCatalogNodes(nodes, type, parentPath = []) {
    return nodes.flatMap((node) => {
      const label = type === "Library" ? node.group : node.id;
      const path = [...parentPath, label];
      const current = type === "Library" ? {
        type,
        label,
        parentPath,
        path,
        artifactsVisible: node.artifactsVisible,
        entries: node.entries || [],
        children: node.children || []
      } : {
        type,
        label,
        parentPath,
        path,
        version: node.version,
        appliedToModules: node.appliedToModules || [],
        children: node.children || []
      };
      return [
        current,
        ...flattenCatalogNodes(current.children, type, path)
      ];
    });
  }
  function requireUniqueLabels(target, nodes) {
    const labels = /* @__PURE__ */ new Map();
    for (const node of nodes) {
      labels.set(node.label, (labels.get(node.label) || 0) + 1);
    }
    const duplicateLabels = [...labels.entries()].filter(([, count]) => count > 1).map(([label]) => label);
    if (duplicateLabels.length > 0) {
      throw new Error(
        `${target.name} contains duplicate labels (${duplicateLabels.join(", ")}). Catalog tree update-only sync needs stable Figma node path metadata before it can continue.`
      );
    }
  }

  // src/figma/figma-connector-gateway.ts
  function collectTreeConnectors(section) {
    return section.findAllWithCriteria({ types: ["CONNECTOR"] }).filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
  }
  function hasConnector(connectors, parentNodeId, childNodeId) {
    return connectors.some(
      (connector) => connector.connectorStart?.endpointNodeId === parentNodeId && connector.connectorEnd?.endpointNodeId === childNodeId
    );
  }
  function createTreeConnector(section, parentInstance, childInstance) {
    const container = childInstance.parent && childInstance.parent.type === "SECTION" ? childInstance.parent : section;
    const template = findConnectorTemplate(container, section);
    const connector = template.clone();
    container.appendChild(connector);
    connector.name = CONNECTOR_TEMPLATE_NAME;
    connector.connectorStart = {
      endpointNodeId: parentInstance.id,
      magnet: "BOTTOM"
    };
    connector.connectorEnd = {
      endpointNodeId: childInstance.id,
      magnet: "TOP"
    };
    return connector;
  }
  function findConnectorTemplate(container, section) {
    const containerTemplate = container.findAllWithCriteria({ types: ["CONNECTOR"] }).find((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
    if (containerTemplate) {
      return containerTemplate;
    }
    const sectionTemplate = collectTreeConnectors(section)[0];
    if (sectionTemplate) {
      return sectionTemplate;
    }
    throw new Error(`No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.name}'.`);
  }

  // src/figma/figma-node-gateway.ts
  async function requireVariableCollection(name) {
    const collections = await figma.variables.getLocalVariableCollectionsAsync();
    const collection = collections.find((candidate) => candidate.name === name);
    if (!collection) {
      throw new Error(`Variable collection '${name}' was not found.`);
    }
    return collection;
  }
  function requireModeId(collection, modeName) {
    const mode = collection.modes.find((candidate) => candidate.name === modeName);
    if (!mode) {
      throw new Error(`Mode '${modeName}' was not found in '${collection.name}'.`);
    }
    return mode.modeId;
  }
  async function loadVariablesByVersionKey(collection) {
    const variables = /* @__PURE__ */ new Map();
    for (const variableId of collection.variableIds) {
      const variable = await figma.variables.getVariableByIdAsync(variableId);
      if (!variable) continue;
      const versionKey = variable.name.split("/").at(-1);
      variables.set(versionKey, variable);
    }
    return variables;
  }
  async function requireComponent(nodeId) {
    const node = await figma.getNodeByIdAsync(nodeId);
    if (!node || node.type !== "COMPONENT") {
      throw new Error(`Expected '${nodeId}' to be a COMPONENT.`);
    }
    return node;
  }
  async function requireFrame(nodeId) {
    const node = await figma.getNodeByIdAsync(nodeId);
    if (!node || node.type !== "FRAME") {
      throw new Error(`Expected '${nodeId}' to be a FRAME.`);
    }
    return node;
  }
  async function requirePage(nodeId) {
    const node = await figma.getNodeByIdAsync(nodeId);
    if (!node || node.type !== "PAGE") {
      throw new Error(`Expected '${nodeId}' to be a PAGE.`);
    }
    return node;
  }
  async function requireSection(nodeId) {
    const node = await figma.getNodeByIdAsync(nodeId);
    if (!node || node.type !== "SECTION") {
      throw new Error(`Expected '${nodeId}' to be a SECTION.`);
    }
    return node;
  }
  function getComponentPropertyValue(instance, propertyName) {
    return instance.componentProperties?.[propertyName]?.value;
  }
  async function requireTreeNodeComponent(type, componentIds, componentCache) {
    if (componentCache.has(type)) {
      return componentCache.get(type);
    }
    const componentId = componentIds[type];
    const component = await requireComponent(componentId);
    componentCache.set(type, component);
    return component;
  }

  // src/figma/figma-text-gateway.ts
  function collectText(node) {
    const text = [];
    if ("characters" in node) {
      text.push(node.characters);
    }
    if ("children" in node) {
      for (const child of node.children) {
        text.push(...collectText(child));
      }
    }
    return text;
  }
  function countNamedTextNodes(root, name) {
    return root.findAllWithCriteria({ types: ["TEXT"] }).filter((textNode) => textNode.name === name).length;
  }
  async function updateNamedTextNodes(root, name, expectedValues, mutatedNodeIds) {
    const textNodes = root.findAllWithCriteria({ types: ["TEXT"] }).filter((textNode) => textNode.name === name);
    if (textNodes.length !== expectedValues.length) {
      throw new Error(
        `Tree node '${root.id}' expected ${expectedValues.length} '${name}' text nodes, found ${textNodes.length}. Run the create-missing catalog tree phase before writing metadata.`
      );
    }
    for (let index = 0; index < expectedValues.length; index += 1) {
      const textNode = textNodes[index];
      const nextValue = expectedValues[index];
      if (textNode.characters === nextValue) continue;
      await loadTextNodeFonts(textNode);
      textNode.characters = nextValue;
      mutatedNodeIds.push(textNode.id);
    }
  }
  async function loadTextNodeFonts(textNode) {
    const segments = textNode.getStyledTextSegments(["fontName"]);
    const fontKeys = /* @__PURE__ */ new Set();
    for (const segment of segments) {
      if (segment.fontName === figma.mixed) {
        continue;
      }
      fontKeys.add(`${segment.fontName.family}\0${segment.fontName.style}`);
    }
    for (const fontKey of fontKeys) {
      const [family, style] = fontKey.split("\0");
      await figma.loadFontAsync({ family, style });
    }
  }

  // src/figma/figma-consumer-modules-gateway.ts
  async function updateLibraryArtifactConsumerModules(root, artifacts, mutatedNodeIds) {
    const artifactInstances = root.findAllWithCriteria({ types: ["INSTANCE"] }).filter((candidate) => candidate.name === ARTIFACT_INSTANCE_NAME);
    if (artifactInstances.length < artifacts.length) {
      throw new Error(
        `Tree node '${root.id}' expected at least ${artifacts.length} '${ARTIFACT_INSTANCE_NAME}' instances, found ${artifactInstances.length}. Update the .tree node component structure before writing metadata.`
      );
    }
    for (let index = 0; index < artifacts.length; index += 1) {
      const artifactInstance = artifactInstances[index];
      const artifact = artifacts[index];
      artifactInstance.setProperties({
        [ARTIFACT_PROPS.showConsumerModules]: artifact.requiredByModules.length > 0
      });
      mutatedNodeIds.push(artifactInstance.id);
      await updateConsumerModuleInstances(
        artifactInstance,
        "Required by",
        artifact.requiredByModules,
        mutatedNodeIds
      );
    }
  }
  async function updateLibraryBundleConsumerModules(root, bundles, mutatedNodeIds) {
    const bundleInstances = root.findAllWithCriteria({ types: ["INSTANCE"] }).filter((candidate) => candidate.name === ARTIFACTS_BUNDLE_INSTANCE_NAME);
    if (bundleInstances.length < bundles.length) {
      throw new Error(
        `Tree node '${root.id}' expected at least ${bundles.length} '${ARTIFACTS_BUNDLE_INSTANCE_NAME}' instances, found ${bundleInstances.length}. Update the .tree node component structure before writing metadata.`
      );
    }
    for (let index = 0; index < bundles.length; index += 1) {
      const bundleInstance = bundleInstances[index];
      const bundle = bundles[index];
      bundleInstance.setProperties({
        [ARTIFACTS_BUNDLE_PROPS.showConsumerModules]: bundle.requiredByModules.length > 0
      });
      mutatedNodeIds.push(bundleInstance.id);
      await updateConsumerModuleInstances(
        bundleInstance,
        "Required by",
        bundle.requiredByModules,
        mutatedNodeIds,
        { excludeArtifactDescendants: true }
      );
    }
  }
  async function updateConsumerModuleInstances(root, heading, modules, mutatedNodeIds, options = {}) {
    if (modules.length > 0) {
      requireConsumerModuleHeading(root, heading);
    }
    const moduleInstances = root.findAllWithCriteria({ types: ["INSTANCE"] }).filter((candidate) => candidate.name === MODULE_INSTANCE_NAME).filter((candidate) => !options.excludeArtifactDescendants || !hasAncestorInstanceNamed(candidate, ARTIFACT_INSTANCE_NAME, root));
    if (moduleInstances.length < modules.length) {
      throw new Error(
        `Node '${root.id}' expected at least ${modules.length} '${MODULE_INSTANCE_NAME}' instances for '${heading}', found ${moduleInstances.length}. Update the .tree node component structure before writing metadata.`
      );
    }
    for (let index = 0; index < modules.length; index += 1) {
      const moduleInstance = moduleInstances[index];
      await setModuleInstance(moduleInstance, modules[index], mutatedNodeIds);
    }
    for (const moduleInstance of moduleInstances.slice(modules.length)) {
      if (moduleInstance.visible === false) continue;
      moduleInstance.visible = false;
      mutatedNodeIds.push(moduleInstance.id);
    }
  }
  function hasAncestorInstanceNamed(node, name, boundary) {
    let current = node.parent;
    while (current && current.id !== boundary.id) {
      if (current.type === "INSTANCE" && current.name === name) return true;
      current = current.parent;
    }
    return false;
  }
  function requireConsumerModuleHeading(root, heading) {
    const hasHeading = root.findAllWithCriteria({ types: ["TEXT"] }).some((textNode) => textNode.name === "label" && textNode.characters === heading);
    if (!hasHeading) {
      throw new Error(`Node '${root.id}' is missing '${heading}' consumer module heading text.`);
    }
  }
  async function setModuleInstance(moduleInstance, moduleName, mutatedNodeIds) {
    requireModuleVariantProperty(moduleInstance, MODULE_PROPS.name);
    requireModuleVariantProperty(moduleInstance, MODULE_PROPS.size);
    moduleInstance.visible = true;
    moduleInstance.setProperties({
      [MODULE_PROPS.name]: moduleName,
      [MODULE_PROPS.size]: SMALL_MODULE_SIZE
    });
    mutatedNodeIds.push(moduleInstance.id);
    await updateNamedTextNodes(moduleInstance, "label", [moduleName], mutatedNodeIds);
  }
  function requireModuleVariantProperty(moduleInstance, propertyName) {
    const property = moduleInstance.componentProperties?.[propertyName];
    if (!property || property.type !== "VARIANT") {
      throw new Error(
        `Expected '${moduleInstance.id}' to be a '${MODULE_INSTANCE_NAME}' instance with '${propertyName}' variant property.`
      );
    }
  }

  // src/domain/catalog/library-catalog-entries.ts
  function libraryArtifactNames(entries) {
    return libraryArtifacts(entries).map((artifact) => artifact.name);
  }
  function libraryArtifacts(entries) {
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
  function libraryBundles(entries) {
    return entries.filter((entry) => entry.type === "bundle").map((entry) => ({
      alias: entry.alias,
      requiredByModules: sortedUnique(entry.requiredByModules || [])
    }));
  }
  function libraryArtifactVersions(entries) {
    return entries.flatMap((entry) => {
      const version = entry.version;
      if (!version?.visible || !version.value) return [];
      if (entry.type === "artifact") return [version.value];
      if (entry.type === "bundle") return (entry.artifacts || []).map(() => version.value);
      throw new Error(`Unsupported library catalog entry type '${entry.type}'.`);
    });
  }
  function sortedUnique(values) {
    return [...new Set(values)].sort();
  }

  // src/figma/figma-tree-node-gateway.ts
  function collectTreeNodeInstancesByLabel(section, type) {
    const instances = section.findAllWithCriteria({ types: ["INSTANCE"] }).filter((instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === type);
    const instancesByLabel = /* @__PURE__ */ new Map();
    for (const instance of instances) {
      const label = type === "Library" ? getComponentPropertyValue(instance, TREE_NODE_PROPS.libraryGroup) : getComponentPropertyValue(instance, TREE_NODE_PROPS.pluginId);
      if (!label || label === "Library group" || label === "Plugin ID") continue;
      if (instancesByLabel.has(label)) {
        throw new Error(`Duplicate '${label}' ${type} tree node instances were found in section '${section.name}'.`);
      }
      instancesByLabel.set(label, instance);
    }
    return instancesByLabel;
  }
  async function createMissingTreeNode(target, section, node, instancesByLabel, componentCache, mutatedNodeIds) {
    const container = requireTreeNodeContainer(section, node);
    const template = findTreeNodeTemplate(section, node);
    const instance = template ? template.clone() : (await requireTreeNodeComponent(node.type, TREE_NODE_COMPONENT_IDS, componentCache)).createInstance();
    container.appendChild(instance);
    instance.name = ".tree node";
    const position = nextTreeNodePosition(container, node, instancesByLabel);
    instance.x = position.x;
    instance.y = position.y;
    if (node.type === "Library") {
      await updateLibraryTreeNode(instance, node, mutatedNodeIds);
    } else {
      await updatePluginTreeNode(instance, node, mutatedNodeIds, target);
    }
    mutatedNodeIds.push(instance.id);
    return instance;
  }
  async function updateLibraryTreeNode(instance, node, mutatedNodeIds) {
    const artifacts = libraryArtifacts(node.entries);
    const bundles = libraryBundles(node.entries);
    const artifactNames = artifacts.map((artifact) => artifact.name);
    const artifactVersions = libraryArtifactVersions(node.entries);
    const requiredByModules = sortedUnique([
      ...artifacts.flatMap((artifact) => artifact.requiredByModules),
      ...bundles.flatMap((bundle) => bundle.requiredByModules)
    ]);
    instance.setProperties({
      [TREE_NODE_PROPS.libraryGroup]: node.label,
      [TREE_NODE_PROPS.showArtifacts]: node.artifactsVisible && artifactNames.length > 0,
      [TREE_NODE_PROPS.showConsumerModule]: requiredByModules.length > 0,
      [TREE_NODE_PROPS.type]: "Library"
    });
    mutatedNodeIds.push(instance.id);
    await updateNamedTextNodes(instance, "Library group", [node.label], mutatedNodeIds);
    await updateNamedTextNodes(instance, "artifact name", artifactNames, mutatedNodeIds);
    await updateNamedTextNodes(instance, "artifact version", artifactVersions, mutatedNodeIds);
    await updateLibraryArtifactConsumerModules(instance, artifacts, mutatedNodeIds);
    await updateLibraryBundleConsumerModules(instance, bundles, mutatedNodeIds);
  }
  async function updatePluginTreeNode(instance, node, mutatedNodeIds, target) {
    const versionValue = node.version?.visible && node.version?.value ? node.version.value : "Plugin version";
    const appliedToModules = sortedUnique(node.appliedToModules || []);
    const isGradleConventionPlugin = target?.gradleConventionPluginNodes === true && node.children.length === 0;
    instance.setProperties({
      [TREE_NODE_PROPS.pluginId]: node.label,
      [TREE_NODE_PROPS.pluginVersion]: versionValue,
      [TREE_NODE_PROPS.showPluginVersion]: node.version?.visible === true && Boolean(node.version?.value),
      [TREE_NODE_PROPS.showConsumerModule]: appliedToModules.length > 0,
      [TREE_NODE_PROPS.showIsGradleConventionPlugin]: isGradleConventionPlugin,
      [TREE_NODE_PROPS.type]: "Plugin"
    });
    mutatedNodeIds.push(instance.id);
    await updateConsumerModuleInstances(instance, "Applied by", appliedToModules, mutatedNodeIds);
  }
  function requireTreeNodeContainer(section, node) {
    const rootLabel = node.path[0];
    const existingContainer = section.children.find((child) => child.type === "SECTION" && child.name === rootLabel);
    if (existingContainer) {
      return existingContainer;
    }
    if (node.path.length > 1) {
      throw new Error(`Cannot create ${node.path.join("/")}: root section '${rootLabel}' is missing.`);
    }
    const newSection = figma.createSection();
    newSection.name = rootLabel;
    section.appendChild(newSection);
    const position = nextTopLevelSectionPosition(section);
    newSection.x = position.x;
    newSection.y = position.y;
    newSection.resizeWithoutConstraints(1200, 900);
    return newSection;
  }
  function nextTopLevelSectionPosition(section) {
    const sections = section.children.filter((child) => child.type === "SECTION").sort((first, second) => first.y - second.y || first.x - second.x);
    if (sections.length === 0) {
      return { x: 100, y: 100 };
    }
    const last = sections[sections.length - 1];
    return {
      x: last.x + last.width + 100,
      y: last.y
    };
  }
  function findTreeNodeTemplate(section, node) {
    const candidates = section.findAllWithCriteria({ types: ["INSTANCE"] }).filter((instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === node.type);
    if (node.type === "Plugin") {
      const showPluginVersion = node.version?.visible === true && Boolean(node.version?.value);
      return candidates.find(
        (instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.showPluginVersion) === showPluginVersion
      ) || candidates[0];
    }
    const artifactNameCount = libraryArtifactNames(node.entries).length;
    const artifactVersionCount = libraryArtifactVersions(node.entries).length;
    return candidates.find(
      (instance) => countNamedTextNodes(instance, "artifact name") === artifactNameCount && countNamedTextNodes(instance, "artifact version") === artifactVersionCount
    ) || candidates.find(
      (instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.showArtifacts) === (node.artifactsVisible && artifactNameCount > 0)
    ) || candidates[0];
  }
  function nextTreeNodePosition(container, node, instancesByLabel) {
    if (node.parentPath.length > 0) {
      const parentLabel = node.parentPath[node.parentPath.length - 1];
      const parentInstance = instancesByLabel.get(parentLabel);
      if (parentInstance) {
        const siblingInstances = container.children.filter((child) => child.type === "INSTANCE" && child.id !== parentInstance.id).sort((first, second) => first.y - second.y || first.x - second.x);
        const existingSiblingRowY = siblingInstances[0]?.y;
        const lastSibling = siblingInstances[siblingInstances.length - 1];
        const x = lastSibling ? lastSibling.x + lastSibling.width + 120 : parentInstance.x;
        return {
          x,
          y: existingSiblingRowY ?? parentInstance.y + parentInstance.height + TREE_NODE_PARENT_CHILD_GAP
        };
      }
    }
    const instances = container.children.filter((child) => child.type === "INSTANCE").sort((first, second) => first.y - second.y || first.x - second.x);
    if (instances.length === 0) {
      return { x: 100, y: 100 };
    }
    const last = instances[instances.length - 1];
    return {
      x: last.x + last.width + 120,
      y: last.y
    };
  }
  var TREE_NODE_PARENT_CHILD_GAP = 128;

  // src/figma/figma-catalog-tree-sync-gateway.ts
  var FigmaCatalogTreeSyncGateway = class {
    async syncCatalogTrees(designModel) {
      const updatedCatalogNodes = [];
      const createdCatalogNodes = [];
      const createdCatalogConnectors = [];
      const removedCatalogNodes = [];
      const removedCatalogConnectors = [];
      const mutatedNodeIds = [];
      const componentCache = /* @__PURE__ */ new Map();
      for (const target of CATALOG_TREE_TARGETS) {
        const modelNodes = target.nodes(designModel);
        if (!Array.isArray(modelNodes)) {
          throw new Error(`designModel.content.catalogs.${target.name} is required for catalog tree sync.`);
        }
        const expectedNodes = flattenCatalogNodes(modelNodes, target.type);
        requireUniqueLabels(target, expectedNodes);
        const section = await requireSection(target.sectionNodeId);
        const instancesByLabel = collectTreeNodeInstancesByLabel(section, target.type);
        let connectors = collectTreeConnectors(section);
        const disconnectedConnectors = connectors.filter(
          (connector) => !connector.connectorStart?.endpointNodeId || !connector.connectorEnd?.endpointNodeId
        );
        for (const connector of disconnectedConnectors) {
          removedCatalogConnectors.push(`${target.name}/${connector.id}`);
          connector.remove();
        }
        connectors = connectors.filter((connector) => !disconnectedConnectors.includes(connector));
        for (const node of expectedNodes) {
          if (instancesByLabel.has(node.label)) continue;
          const instance = await createMissingTreeNode(
            target,
            section,
            node,
            instancesByLabel,
            componentCache,
            mutatedNodeIds
          );
          instancesByLabel.set(node.label, instance);
          createdCatalogNodes.push(`${target.name}/${node.path.join("/")}`);
        }
        for (const node of expectedNodes) {
          const instance = instancesByLabel.get(node.label);
          if (node.type === "Library") {
            await updateLibraryTreeNode(instance, node, mutatedNodeIds);
          } else {
            await updatePluginTreeNode(instance, node, mutatedNodeIds, target);
          }
          updatedCatalogNodes.push(`${target.name}/${node.path.join("/")}`);
        }
        for (const node of expectedNodes.filter((candidate) => candidate.parentPath.length > 0)) {
          const parentLabel = node.parentPath[node.parentPath.length - 1];
          const parentInstance = instancesByLabel.get(parentLabel);
          const childInstance = instancesByLabel.get(node.label);
          if (!parentInstance || !childInstance) {
            throw new Error(`Cannot create connector for ${target.name}/${node.path.join("/")}: parent or child is missing.`);
          }
          if (hasConnector(connectors, parentInstance.id, childInstance.id)) {
            continue;
          }
          const connector = createTreeConnector(section, parentInstance, childInstance);
          connectors.push(connector);
          mutatedNodeIds.push(connector.id);
          createdCatalogConnectors.push(`${target.name}/${node.parentPath.join("/")} -> ${node.path.join("/")}`);
        }
        const staleResult = removeStaleCatalogNodes(target, expectedNodes, instancesByLabel, connectors);
        removedCatalogNodes.push(...staleResult.removedCatalogNodes);
        removedCatalogConnectors.push(...staleResult.removedCatalogConnectors);
        connectors = staleResult.connectors;
        layoutCatalogTreeNodes(section, expectedNodes, instancesByLabel, mutatedNodeIds);
        resizeSectionsToFit(section, [...instancesByLabel.values()], mutatedNodeIds);
      }
      return {
        updatedCatalogNodes,
        createdCatalogNodes,
        createdCatalogConnectors,
        removedCatalogNodes,
        removedCatalogConnectors,
        mutatedNodeIds
      };
    }
  };
  function removeStaleCatalogNodes(target, expectedNodes, instancesByLabel, connectors) {
    const expectedLabels = new Set(expectedNodes.map((node) => node.label));
    const staleInstances = [...instancesByLabel.entries()].filter(([label]) => !expectedLabels.has(label));
    const staleInstanceIds = new Set(staleInstances.map(([, instance]) => instance.id));
    const removedCatalogNodes = [];
    const removedCatalogConnectors = [];
    for (const connector of connectors) {
      if (staleInstanceIds.has(connector.connectorStart?.endpointNodeId) || staleInstanceIds.has(connector.connectorEnd?.endpointNodeId)) {
        removedCatalogConnectors.push(`${target.name}/${connector.id}`);
        connector.remove();
      }
    }
    for (const [label, instance] of staleInstances) {
      removedCatalogNodes.push(`${target.name}/${label}`);
      instance.remove();
      instancesByLabel.delete(label);
    }
    return {
      removedCatalogNodes,
      removedCatalogConnectors,
      connectors: connectors.filter(
        (connector) => !staleInstanceIds.has(connector.connectorStart?.endpointNodeId) && !staleInstanceIds.has(connector.connectorEnd?.endpointNodeId)
      )
    };
  }
  function layoutCatalogTreeNodes(section, nodes, instancesByLabel, mutatedNodeIds) {
    const nodesByPath = new Map(nodes.map((node) => [pathKey(node.path), node]));
    const childrenByParentPath = /* @__PURE__ */ new Map();
    for (const node of nodes) {
      const parentKey = pathKey(node.parentPath);
      childrenByParentPath.set(
        parentKey,
        [...childrenByParentPath.get(parentKey) || [], node]
      );
    }
    const roots = childrenByParentPath.get(pathKey([])) || [];
    const containers = /* @__PURE__ */ new Map();
    for (const root of roots) {
      const rootInstance = instancesByLabel.get(root.label);
      const container = rootInstance?.parent?.type === "SECTION" ? rootInstance.parent : section;
      containers.set(
        container.id,
        [...containers.get(container.id) || [], root]
      );
    }
    for (const containerRoots of containers.values()) {
      let nextX = CATALOG_TREE_LAYOUT_PADDING;
      for (const root of containerRoots) {
        const layout = layoutCatalogSubtree(
          root,
          nodesByPath,
          childrenByParentPath,
          instancesByLabel,
          nextX,
          CATALOG_TREE_LAYOUT_PADDING
        );
        applyCatalogPlacements(layout.placements, mutatedNodeIds);
        nextX = layout.maxX + CATALOG_TREE_SIBLING_GAP;
      }
    }
  }
  function layoutCatalogSubtree(node, nodesByPath, childrenByParentPath, instancesByLabel, x, y) {
    const instance = instancesByLabel.get(node.label);
    if (!instance) {
      throw new Error(`Cannot layout catalog node '${node.path.join("/")}' because its Figma instance is missing.`);
    }
    const children = (childrenByParentPath.get(pathKey(node.path)) || []).map((child) => nodesByPath.get(pathKey(child.path))).filter(Boolean);
    const placements = /* @__PURE__ */ new Map();
    if (children.length === 0) {
      placements.set(instance.id, { instance, x, y });
      return {
        placements,
        minX: x,
        maxX: x + instance.width,
        nextX: x + instance.width + CATALOG_TREE_SIBLING_GAP
      };
    }
    let childX = x;
    const childY = y + instance.height + CATALOG_TREE_PARENT_CHILD_GAP;
    let minX = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let firstChildCenter;
    let lastChildCenter;
    for (const child of children) {
      const childLayout = layoutCatalogSubtree(
        child,
        nodesByPath,
        childrenByParentPath,
        instancesByLabel,
        childX,
        childY
      );
      for (const [id, placement] of childLayout.placements) {
        placements.set(id, placement);
      }
      minX = Math.min(minX, childLayout.minX);
      maxX = Math.max(maxX, childLayout.maxX);
      const childInstance = instancesByLabel.get(child.label);
      const childPlacement = childLayout.placements.get(childInstance.id);
      const childCenter = childPlacement.x + childInstance.width / 2;
      firstChildCenter = firstChildCenter ?? childCenter;
      lastChildCenter = childCenter;
      childX = childLayout.nextX;
    }
    let parentX = (firstChildCenter + lastChildCenter) / 2 - instance.width / 2;
    placements.set(instance.id, { instance, x: parentX, y });
    minX = Math.min(minX, parentX);
    maxX = Math.max(maxX, parentX + instance.width);
    if (minX < x) {
      const offset = x - minX;
      for (const placement of placements.values()) {
        placement.x += offset;
      }
      minX += offset;
      maxX += offset;
    }
    return {
      placements,
      minX,
      maxX,
      nextX: maxX + CATALOG_TREE_SIBLING_GAP
    };
  }
  function applyCatalogPlacements(placements, mutatedNodeIds) {
    for (const placement of placements.values()) {
      placement.instance.x = placement.x;
      placement.instance.y = placement.y;
      mutatedNodeIds.push(placement.instance.id);
    }
  }
  function resizeSectionsToFit(section, nodes, mutatedNodeIds) {
    const nodesBySection = /* @__PURE__ */ new Map();
    for (const node of nodes) {
      const parentSection = node.parent?.type === "SECTION" ? node.parent : section;
      nodesBySection.set(
        parentSection.id,
        {
          section: parentSection,
          nodes: [...nodesBySection.get(parentSection.id)?.nodes || [], node]
        }
      );
    }
    for (const { section: targetSection, nodes: sectionNodes } of nodesBySection.values()) {
      resizeSectionToFit(targetSection, sectionNodes, mutatedNodeIds);
    }
    resizeSectionToFit(section, section.children.filter((child) => child.visible !== false), mutatedNodeIds);
  }
  function resizeSectionToFit(section, nodes, mutatedNodeIds) {
    if (nodes.length === 0) return;
    const maxRight = Math.max(...nodes.map((node) => node.x + node.width));
    const maxBottom = Math.max(...nodes.map((node) => node.y + node.height));
    section.resizeWithoutConstraints(
      Math.max(section.width, maxRight + 100),
      Math.max(section.height, maxBottom + 100)
    );
    mutatedNodeIds.push(section.id);
  }
  function pathKey(path) {
    return path.join("\0");
  }
  var CATALOG_TREE_LAYOUT_PADDING = 100;
  var CATALOG_TREE_SIBLING_GAP = 120;
  var CATALOG_TREE_PARENT_CHILD_GAP = 128;

  // src/figma/figma-metadata-gateway.ts
  var FigmaMetadataGateway = class {
    async writeMetadata(designModel) {
      const page = await requirePage(METADATA_PAGE_ID);
      await figma.setCurrentPageAsync(page);
      page.setSharedPluginData(METADATA_NAMESPACE, "schemaVersion", String(designModel.schemaVersion));
      page.setSharedPluginData(METADATA_NAMESPACE, "branch", designModel.branch);
      page.setSharedPluginData(METADATA_NAMESPACE, "gitSha", designModel.gitSha);
      page.setSharedPluginData(METADATA_NAMESPACE, "modelHash", designModel.modelHash);
      page.setSharedPluginData(METADATA_NAMESPACE, "syncedAt", (/* @__PURE__ */ new Date()).toISOString());
      return {
        metadata: {
          pageId: page.id,
          namespace: METADATA_NAMESPACE,
          gitSha: page.getSharedPluginData(METADATA_NAMESPACE, "gitSha"),
          modelHash: page.getSharedPluginData(METADATA_NAMESPACE, "modelHash")
        },
        mutatedNodeIds: [page.id]
      };
    }
  };

  // src/figma/figma-module-dependency-sync-gateway.ts
  var FigmaModuleDependencySyncGateway = class {
    async syncModuleDependencies(designModel) {
      const updatedModules = [];
      const hiddenModules = [];
      const updatedModuleConnectors = [];
      const removedModuleConnectors = [];
      const mutatedNodeIds = [];
      for (const target of MODULE_DEPENDENCY_TARGETS) {
        const dependencies = target.dependencies(designModel);
        if (!Array.isArray(dependencies)) {
          throw new Error(`designModel.content.moduleDependencies.${target.name} is required for module dependency sync.`);
        }
        const section = await requireSection(target.sectionNodeId);
        const existingInstances = collectModuleInstances(section);
        const existingConnectors = collectModuleConnectors(section);
        const template = existingInstances[0];
        if (!template && dependencies.length > 0) {
          throw new Error(`No '${MODULE_INSTANCE_NAME}' module template was found in section '${section.name}'.`);
        }
        const slots = layoutDependencySlots(section, dependencies, existingInstances);
        const assignedInstances = assignModuleInstances(section, slots, existingInstances, existingConnectors, template, mutatedNodeIds);
        for (const slot of slots) {
          const instance = assignedInstances.get(slot.key);
          await updateModuleInstance(instance, slot, mutatedNodeIds);
          updatedModules.push(`${target.name}/${slot.key}`);
        }
        hideUnusedModuleInstances(target, existingInstances, assignedInstances, hiddenModules, mutatedNodeIds);
        const connectorResult = syncModuleConnectors(
          target,
          section,
          dependencies,
          assignedInstances,
          existingConnectors,
          mutatedNodeIds
        );
        updatedModuleConnectors.push(...connectorResult.updatedModuleConnectors);
        removedModuleConnectors.push(...connectorResult.removedModuleConnectors);
        resizeSectionToFit2(section, [...assignedInstances.values()]);
      }
      return {
        updatedModules,
        hiddenModules,
        updatedModuleConnectors,
        removedModuleConnectors,
        mutatedNodeIds
      };
    }
  };
  function collectModuleInstances(section) {
    return section.findAllWithCriteria({ types: ["INSTANCE"] }).filter((instance) => instance.name === MODULE_INSTANCE_NAME).sort((first, second) => first.y - second.y || first.x - second.x);
  }
  function collectModuleConnectors(section) {
    return section.findAllWithCriteria({ types: ["CONNECTOR"] }).filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME).sort((first, second) => first.y - second.y || first.x - second.x);
  }
  function moduleNameOf(instance) {
    const propertyValue = getComponentPropertyValue(instance, MODULE_PROPS.name);
    if (propertyValue && propertyValue !== MODULE_PROPS.name) {
      return propertyValue;
    }
    return instance.findAllWithCriteria({ types: ["TEXT"] }).find((textNode) => textNode.name === "label")?.characters;
  }
  function layoutDependencySlots(section, dependencies, existingInstances) {
    const dependenciesByDependencyModule = groupDependenciesByDependencyModule(dependencies);
    const groupModules = [...dependenciesByDependencyModule.keys()].sort(
      (first, second) => groupLevel(first, dependencies).localeCompare(groupLevel(second, dependencies)) || first.localeCompare(second)
    );
    const slots = [];
    let nextY = MODULE_LAYOUT_PADDING;
    for (const groupModule of groupModules) {
      const groupDependencies = dependenciesByDependencyModule.get(groupModule).sort((first, second) => first.dependentModule.localeCompare(second.dependentModule));
      const parentWidth = widthForModule(groupModule, existingInstances);
      let childX = MODULE_LAYOUT_PADDING;
      let childY = nextY + MODULE_LAYOUT_DEFAULT_HEIGHT + MODULE_PARENT_CHILD_GAP;
      let rowHeight = 0;
      const childSlots = [];
      for (const dependency of groupDependencies) {
        const childWidth = widthForModule(dependency.dependentModule, existingInstances);
        if (childX > MODULE_LAYOUT_PADDING && childX + childWidth > section.width - MODULE_LAYOUT_PADDING) {
          childX = MODULE_LAYOUT_PADDING;
          childY += rowHeight + MODULE_CHILD_ROW_GAP;
          rowHeight = 0;
        }
        const childSlot = {
          key: childSlotKey(dependency),
          moduleName: dependency.dependentModule,
          role: "child",
          groupModule,
          dependency,
          x: childX,
          y: childY
        };
        childSlots.push(childSlot);
        childX += childWidth + MODULE_LAYOUT_COLUMN_GAP;
        rowHeight = Math.max(rowHeight, MODULE_LAYOUT_DEFAULT_HEIGHT);
      }
      const firstChild = childSlots[0];
      const lastChild = childSlots[childSlots.length - 1];
      const firstChildWidth = firstChild ? widthForModule(firstChild.moduleName, existingInstances) : parentWidth;
      const lastChildWidth = lastChild ? widthForModule(lastChild.moduleName, existingInstances) : parentWidth;
      let parentX = firstChild && lastChild ? (firstChild.x + firstChildWidth / 2 + (lastChild.x + lastChildWidth / 2)) / 2 - parentWidth / 2 : MODULE_LAYOUT_PADDING;
      if (parentX < MODULE_LAYOUT_PADDING) {
        const offset = MODULE_LAYOUT_PADDING - parentX;
        parentX += offset;
        for (const childSlot of childSlots) {
          childSlot.x += offset;
        }
      }
      slots.push({
        key: parentSlotKey(groupModule),
        moduleName: groupModule,
        role: "parent",
        groupModule,
        x: parentX,
        y: nextY
      });
      slots.push(...childSlots);
      nextY = childY + Math.max(rowHeight, MODULE_LAYOUT_DEFAULT_HEIGHT) + MODULE_GROUP_GAP;
      if (parentWidth > section.width - MODULE_LAYOUT_PADDING * 2) {
        throw new Error(`Module '${groupModule}' is wider than dependency section '${section.name}'.`);
      }
    }
    return slots;
  }
  function groupDependenciesByDependencyModule(dependencies) {
    return dependencies.reduce((groups, dependency) => {
      groups.set(
        dependency.dependencyModule,
        [...groups.get(dependency.dependencyModule) || [], dependency]
      );
      return groups;
    }, /* @__PURE__ */ new Map());
  }
  function groupLevel(moduleName, dependencies) {
    const outgoingDependencyCount = dependencies.filter((dependency) => dependency.dependentModule === moduleName).length;
    return String(outgoingDependencyCount).padStart(4, "0");
  }
  function widthForModule(moduleName, existingInstances) {
    const existingInstance = existingInstances.find((instance) => moduleNameOf(instance) === moduleName);
    return Math.max(existingInstance?.width || MODULE_LAYOUT_DEFAULT_WIDTH, MODULE_LAYOUT_DEFAULT_WIDTH);
  }
  function assignModuleInstances(section, slots, existingInstances, existingConnectors, template, mutatedNodeIds) {
    const assignedInstances = /* @__PURE__ */ new Map();
    const usedInstanceIds = /* @__PURE__ */ new Set();
    for (const slot of slots) {
      const preferred = findPreferredInstance(slot, existingInstances, existingConnectors, usedInstanceIds);
      const instance = preferred || template.clone();
      if (!preferred) {
        section.appendChild(instance);
        instance.name = MODULE_INSTANCE_NAME;
        mutatedNodeIds.push(instance.id);
      }
      assignedInstances.set(slot.key, instance);
      usedInstanceIds.add(instance.id);
    }
    return assignedInstances;
  }
  function findPreferredInstance(slot, existingInstances, existingConnectors, usedInstanceIds) {
    if (slot.role === "child") {
      const connectedInstance = existingConnectors.map((connector) => ({
        start: existingInstances.find((instance) => instance.id === connector.connectorStart?.endpointNodeId),
        end: existingInstances.find((instance) => instance.id === connector.connectorEnd?.endpointNodeId)
      })).find(
        (pair) => pair.start && pair.end && moduleNameOf(pair.start) === slot.moduleName && moduleNameOf(pair.end) === slot.groupModule && !usedInstanceIds.has(pair.start.id)
      )?.start;
      if (connectedInstance) {
        return connectedInstance;
      }
    }
    return existingInstances.find(
      (instance) => moduleNameOf(instance) === slot.moduleName && !usedInstanceIds.has(instance.id)
    );
  }
  async function updateModuleInstance(instance, slot, mutatedNodeIds) {
    requireModuleVariantProperty2(instance, MODULE_PROPS.name);
    requireModuleVariantProperty2(instance, MODULE_PROPS.size);
    instance.visible = true;
    instance.x = slot.x;
    instance.y = slot.y;
    instance.setProperties({
      [MODULE_PROPS.name]: slot.moduleName,
      [MODULE_PROPS.size]: BIG_MODULE_SIZE
    });
    mutatedNodeIds.push(instance.id);
    await updateNamedTextNodes(instance, "label", [slot.moduleName], mutatedNodeIds);
  }
  function requireModuleVariantProperty2(moduleInstance, propertyName) {
    const property = moduleInstance.componentProperties?.[propertyName];
    if (!property || property.type !== "VARIANT") {
      throw new Error(
        `Expected '${moduleInstance.id}' to be a '${MODULE_INSTANCE_NAME}' instance with '${propertyName}' variant property.`
      );
    }
  }
  function hideUnusedModuleInstances(target, existingInstances, assignedInstances, hiddenModules, mutatedNodeIds) {
    const visibleModuleIds = new Set([...assignedInstances.values()].map((instance) => instance.id));
    for (const instance of existingInstances) {
      if (visibleModuleIds.has(instance.id)) continue;
      if (instance.visible === false) continue;
      instance.visible = false;
      hiddenModules.push(`${target.name}/${moduleNameOf(instance) || instance.id}`);
      mutatedNodeIds.push(instance.id);
    }
  }
  function syncModuleConnectors(target, section, dependencies, assignedInstances, existingConnectors, mutatedNodeIds) {
    const sortedDependencies = [...dependencies].sort(
      (first, second) => first.dependencyModule.localeCompare(second.dependencyModule) || first.dependentModule.localeCompare(second.dependentModule)
    );
    const template = existingConnectors[0];
    if (!template && sortedDependencies.length > 0) {
      throw new Error(`No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.name}'.`);
    }
    const updatedModuleConnectors = [];
    const removedModuleConnectors = [];
    for (let index = 0; index < sortedDependencies.length; index += 1) {
      const dependency = sortedDependencies[index];
      const dependentModuleInstance = assignedInstances.get(childSlotKey(dependency));
      const dependencyModuleInstance = assignedInstances.get(parentSlotKey(dependency.dependencyModule));
      if (!dependentModuleInstance || !dependencyModuleInstance) {
        throw new Error(
          `Cannot create connector for ${target.name}/${dependency.dependentModule} -> ${dependency.dependencyModule}: dependent or dependency module is missing.`
        );
      }
      const connector = existingConnectors[index] || template.clone();
      if (!existingConnectors[index]) {
        section.appendChild(connector);
        connector.name = CONNECTOR_TEMPLATE_NAME;
      }
      connector.visible = true;
      connector.connectorStart = {
        endpointNodeId: dependentModuleInstance.id,
        magnet: "TOP"
      };
      connector.connectorEnd = {
        endpointNodeId: dependencyModuleInstance.id,
        magnet: "BOTTOM"
      };
      updatedModuleConnectors.push(`${target.name}/${dependency.dependentModule} -> ${dependency.dependencyModule}`);
      mutatedNodeIds.push(connector.id);
    }
    for (const connector of existingConnectors.slice(sortedDependencies.length)) {
      removedModuleConnectors.push(`${target.name}/${connector.id}`);
      connector.remove();
    }
    return {
      updatedModuleConnectors,
      removedModuleConnectors
    };
  }
  function parentSlotKey(moduleName) {
    return `parent:${moduleName}`;
  }
  function childSlotKey(dependency) {
    return `child:${dependency.dependencyModule}->${dependency.dependentModule}`;
  }
  function resizeSectionToFit2(section, nodes) {
    if (nodes.length === 0) return;
    const maxRight = Math.max(...nodes.map((node) => node.x + node.width));
    const maxBottom = Math.max(...nodes.map((node) => node.y + node.height));
    section.resizeWithoutConstraints(
      Math.max(section.width, maxRight + MODULE_LAYOUT_PADDING),
      Math.max(section.height, maxBottom + MODULE_LAYOUT_PADDING)
    );
  }
  var MODULE_LAYOUT_PADDING = 100;
  var MODULE_LAYOUT_COLUMN_GAP = 80;
  var MODULE_PARENT_CHILD_GAP = 128;
  var MODULE_CHILD_ROW_GAP = 128;
  var MODULE_GROUP_GAP = 96;
  var MODULE_LAYOUT_DEFAULT_WIDTH = 340;
  var MODULE_LAYOUT_DEFAULT_HEIGHT = 182;

  // src/figma/figma-version-sync-gateway.ts
  var FigmaVersionSyncGateway = class {
    async syncVersions(designModel) {
      const versionSections = requireVersionSections(designModel);
      const collection = await requireVariableCollection(VERSIONS_COLLECTION_NAME);
      const versionAliasModeId = requireModeId(collection, VERSION_ALIAS_MODE_NAME);
      const versionNumberModeId = requireModeId(collection, VERSION_NUMBER_MODE_NAME);
      const projectVersionComponent = await requireComponent(PROJECT_VERSION_COMPONENT_ID);
      const variables = await loadVariablesByVersionKey(collection);
      const mutatedNodeIds = [];
      const createdVariables = [];
      const createdInstances = [];
      const updatedVersions = [];
      for (const section of versionSections) {
        const target = VERSION_SECTION_TARGETS[section.name];
        if (!target) {
          throw new Error(`No Figma target configured for version section '${section.name}'.`);
        }
        const parent = await requireFrame(target.parentNodeId);
        const entries = Object.entries(section.versions);
        for (const [versionKey, versionNumber] of entries) {
          let variable = variables.get(versionKey);
          if (!variable) {
            variable = figma.variables.createVariable(
              `${target.variableFolder}/${versionKey}`,
              collection,
              "STRING"
            );
            variable.scopes = ["TEXT_CONTENT"];
            variables.set(versionKey, variable);
            createdVariables.push(variable.name);
          }
          variable.setValueForMode(versionAliasModeId, versionKey);
          variable.setValueForMode(versionNumberModeId, versionNumber);
          updatedVersions.push(versionKey);
          const existingInstance = findProjectVersionInstance(parent, versionKey);
          if (!existingInstance) {
            const instance = projectVersionComponent.createInstance();
            const position = nextProjectVersionPosition(parent);
            parent.appendChild(instance);
            instance.x = position.x;
            instance.y = position.y;
            bindProjectVersionInstance(instance, variable);
            mutatedNodeIds.push(instance.id);
            createdInstances.push(versionKey);
          }
          mutatedNodeIds.push(variable.id);
        }
      }
      return {
        updatedVersions,
        createdVariables,
        createdInstances,
        mutatedNodeIds
      };
    }
  };
  function requireVersionSections(designModel) {
    const sections = designModel.content?.versionSections;
    if (!Array.isArray(sections) || sections.length === 0) {
      throw new Error("designModel.content.versionSections is required for visual version sync.");
    }
    return sections;
  }
  function findProjectVersionInstance(parent, versionKey) {
    return parent.children.find((child) => {
      if (child.type !== "INSTANCE" || child.name !== ".project version") return false;
      return collectText(child).includes(versionKey);
    });
  }
  function nextProjectVersionPosition(parent) {
    const instances = parent.children.filter((child) => child.type === "INSTANCE" && child.name === ".project version").sort((first, second) => first.y - second.y || first.x - second.x);
    if (instances.length === 0) {
      return { x: 0, y: 0 };
    }
    const columnXs = [...new Set(instances.map((instance) => Math.round(instance.x)))].sort((first, second) => first - second).slice(0, 2);
    if (columnXs.length === 1) {
      columnXs.push(columnXs[0] + Math.round(instances[0].width) + 64);
    }
    const nextColumnIndex = instances.length % columnXs.length;
    const columnInstances = instances.filter((instance) => Math.round(instance.x) === columnXs[nextColumnIndex]);
    const lastInColumn = columnInstances.at(-1);
    const rowGap = 64;
    return {
      x: columnXs[nextColumnIndex],
      y: lastInColumn ? lastInColumn.y + lastInColumn.height + rowGap : 0
    };
  }
  function bindProjectVersionInstance(instance, variable) {
    const alias = figma.variables.createVariableAlias(variable);
    instance.setProperties({
      "Version alias#63075:0": alias,
      "Version number#63075:1": alias
    });
  }

  // src/usecases/sync-figma-design-model.ts
  async function syncFigmaDesignModel(designModel, dependencies) {
    if (!designModel) {
      throw new Error("Replace DESIGN_MODEL with build/reports/figma-sync/design-model.json.");
    }
    const versionSyncResult = await dependencies.versionSyncGateway.syncVersions(designModel);
    const catalogSyncResult = await dependencies.catalogTreeSyncGateway.syncCatalogTrees(designModel);
    const moduleDependencySyncResult = await dependencies.moduleDependencySyncGateway.syncModuleDependencies(designModel);
    const metadataSyncResult = await dependencies.metadataSyncGateway.writeMetadata(designModel);
    return {
      updatedVersions: versionSyncResult.updatedVersions,
      createdVariables: versionSyncResult.createdVariables,
      createdInstances: versionSyncResult.createdInstances,
      updatedCatalogNodes: catalogSyncResult.updatedCatalogNodes,
      createdCatalogNodes: catalogSyncResult.createdCatalogNodes,
      createdCatalogConnectors: catalogSyncResult.createdCatalogConnectors,
      updatedModules: moduleDependencySyncResult.updatedModules,
      hiddenModules: moduleDependencySyncResult.hiddenModules,
      updatedModuleConnectors: moduleDependencySyncResult.updatedModuleConnectors,
      removedModuleConnectors: moduleDependencySyncResult.removedModuleConnectors,
      metadata: metadataSyncResult.metadata,
      mutatedNodeIds: [
        .../* @__PURE__ */ new Set([
          ...versionSyncResult.mutatedNodeIds,
          ...catalogSyncResult.mutatedNodeIds,
          ...moduleDependencySyncResult.mutatedNodeIds,
          ...metadataSyncResult.mutatedNodeIds
        ])
      ]
    };
  }

  // src/app/create-figma-design-model-sync.ts
  function createFigmaDesignModelSync() {
    return (designModel) => syncFigmaDesignModel(
      designModel,
      {
        versionSyncGateway: new FigmaVersionSyncGateway(),
        catalogTreeSyncGateway: new FigmaCatalogTreeSyncGateway(),
        moduleDependencySyncGateway: new FigmaModuleDependencySyncGateway(),
        metadataSyncGateway: new FigmaMetadataGateway()
      }
    );
  }

  // src/app/sync-develop-design-model.mcp.ts
  async function main(designModel) {
    return createFigmaDesignModelSync()(designModel);
  }
  return __toCommonJS(sync_develop_design_model_mcp_exports);
})();

return await FigmaDevelopSync.main(DESIGN_MODEL);
