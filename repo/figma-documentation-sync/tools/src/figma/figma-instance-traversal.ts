/**
 * Executes one Figma adapter operation with invisible instance descendants
 * available, then restores the caller's traversal option on every exit path.
 */
export function withInvisibleInstanceChildren(operation) {
  if (typeof figma === "undefined") return operation();

  const previousValue = figma.skipInvisibleInstanceChildren;
  figma.skipInvisibleInstanceChildren = false;
  try {
    const result = operation();
    if (result && typeof result.then === "function") {
      return result.finally(() => {
        figma.skipInvisibleInstanceChildren = previousValue;
      });
    }
    figma.skipInvisibleInstanceChildren = previousValue;
    return result;
  } catch (error) {
    figma.skipInvisibleInstanceChildren = previousValue;
    throw error;
  }
}
