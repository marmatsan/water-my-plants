import {
  CI_STEP_SLOT_COUNT,
  CI_STEP_SLOT_NAME_PREFIX,
} from "@figma-documentation-sync/project-config";

export function ciStepSlotName(slotNumber: number): string {
  if (!Number.isInteger(slotNumber) || slotNumber < 1 || slotNumber > CI_STEP_SLOT_COUNT) {
    throw new Error(
      `CI step slot number must be between 1 and ${CI_STEP_SLOT_COUNT}; found '${slotNumber}'.`
    );
  }
  return `${CI_STEP_SLOT_NAME_PREFIX} ${String(slotNumber).padStart(2, "0")}`;
}

export function ciStepSlotNames(): string[] {
  return Array.from(
    { length: CI_STEP_SLOT_COUNT },
    (_, index) => ciStepSlotName(index + 1)
  );
}

export function hasCiStepSlotNamePrefix(name: string): boolean {
  return name.startsWith(`${CI_STEP_SLOT_NAME_PREFIX} `);
}
