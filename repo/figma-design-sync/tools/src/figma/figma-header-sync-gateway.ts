import {
  HEADER_INSTANCE_NAME,
  HEADER_LINK_PROPERTY_NAME,
  HEADER_SECTION_TARGETS,
} from "../config/figma-config";
import type { HeaderSyncGateway } from "../ports/sync-gateways";
import {
  applySectionStrokeContractTree,
  lockOnlyRootSection,
  requireOutlineColorVariable,
  requireSection,
  resizeNodeToFit,
  stackAncestorSectionSiblingsWithGap,
  stackDirectChildSectionsWithGap,
  unlockSectionTreeForMutation,
} from "./figma-node-gateway";
import { loadTextNodeFonts } from "./figma-text-gateway";

export class FigmaHeaderSyncGateway implements HeaderSyncGateway {
  async syncHeaders() {
    const outlineVariable = await requireOutlineColorVariable();
    const updatedHeaders = [];
    const mutatedNodeIds = [];

    for (const target of HEADER_SECTION_TARGETS) {
      const section = await requireSection(target.sectionNodeId);
      unlockSectionTreeForMutation(section, mutatedNodeIds);

      const header = requireDirectHeader(section);
      const linkText = requireLinkText(header);
      await loadTextNodeFonts(linkText);

      const linkTextValue = headerLinkText(target.links);
      const linkProperty = requireTextProperty(header, HEADER_LINK_PROPERTY_NAME);
      if (linkProperty.value !== linkTextValue) {
        header.setProperties({ [linkProperty.key]: linkTextValue });
        mutatedNodeIds.push(header.id);
      }

      linkText.setRangeHyperlink(0, linkText.characters.length, null);
      for (const range of headerLinkRanges(target.links)) {
        linkText.setRangeHyperlink(range.start, range.end, {
          type: "URL",
          value: range.url,
        });
      }
      if (headerLinkNeedsLeftAlignment(linkText)) {
        linkText.textAlignHorizontal = "LEFT";
      }
      mutatedNodeIds.push(linkText.id);
      updatedHeaders.push(section.id);

      stackDirectChildSectionsWithGap(section, mutatedNodeIds);
      resizeNodeToFit(section, section.children.filter((child) => child.visible !== false), mutatedNodeIds);
      applySectionStrokeContractTree(section, outlineVariable, mutatedNodeIds);
      stackAncestorSectionSiblingsWithGap(section, mutatedNodeIds);
      lockOnlyRootSection(section, mutatedNodeIds);
    }

    return {
      updatedHeaders,
      mutatedNodeIds,
    };
  }
}

export function headerLinkText(links) {
  return links.map((link) => link.label).join("\n");
}

export function headerLinkRanges(links) {
  let start = 0;
  return links.map((link) => {
    const range = {
      start,
      end: start + link.label.length,
      url: link.url,
    };
    start = range.end + 1;
    return range;
  });
}

export function headerLinkNeedsLeftAlignment(linkText) {
  return linkText.textAlignHorizontal !== "LEFT";
}

function requireDirectHeader(section) {
  const header = section.children.find(
    (child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME
  );
  if (!header) {
    throw new Error(`Section '${section.id}' is missing a direct '${HEADER_INSTANCE_NAME}' instance.`);
  }
  return header;
}

function requireLinkText(header) {
  const linkTexts = header.findAllWithCriteria({ types: ["TEXT"] })
    .filter((text) => text.name === HEADER_LINK_PROPERTY_NAME);
  if (linkTexts.length !== 1) {
    throw new Error(`Header '${header.id}' expected one '${HEADER_LINK_PROPERTY_NAME}' text node, found ${linkTexts.length}.`);
  }
  return linkTexts[0];
}

function requireTextProperty(instance, propertyName) {
  const properties: Record<string, { type: string; value: unknown }> = instance.componentProperties || {};
  const entry = Object.entries(properties)
    .find(([key, property]) =>
      (key === propertyName || key.startsWith(`${propertyName}#`)) && property.type === "TEXT"
    );
  if (!entry) {
    throw new Error(`Header '${instance.id}' is missing TEXT property '${propertyName}'.`);
  }
  return {
    key: entry[0],
    value: entry[1].value,
  };
}
