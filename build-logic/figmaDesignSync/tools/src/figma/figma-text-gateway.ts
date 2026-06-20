export function collectText(node) {
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

export function countNamedTextNodes(root, name) {
  return root.findAllWithCriteria({ types: ["TEXT"] })
    .filter((textNode) => textNode.name === name)
    .length;
}

export async function updateNamedTextNodes(root, name, expectedValues, mutatedNodeIds) {
  const textNodes = root.findAllWithCriteria({ types: ["TEXT"] })
    .filter((textNode) => textNode.name === name);

  if (textNodes.length !== expectedValues.length) {
    throw new Error(
      `Tree node '${root.id}' expected ${expectedValues.length} '${name}' text nodes, found ${textNodes.length}. ` +
        "Run the create-missing catalog tree phase before writing metadata."
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
  const fontKeys = new Set<string>();

  for (const segment of segments) {
    if (segment.fontName === figma.mixed) {
      continue;
    }
    fontKeys.add(`${segment.fontName.family}\u0000${segment.fontName.style}`);
  }

  for (const fontKey of fontKeys) {
    const [family, style] = fontKey.split("\u0000");
    await figma.loadFontAsync({ family, style });
  }
}
