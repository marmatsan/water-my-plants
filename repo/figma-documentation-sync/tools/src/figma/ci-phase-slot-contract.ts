import {
  CI_PHASE_SLOT_COUNT,
  CI_PHASE_SLOT_NAME_PREFIX,
} from "@figma-documentation-sync/project-config";

export function ciPhaseSlotName(slotNumber: number): string {
  if (!Number.isInteger(slotNumber) || slotNumber < 1 || slotNumber > CI_PHASE_SLOT_COUNT) {
    throw new Error(
      `CI phase slot number must be between 1 and ${CI_PHASE_SLOT_COUNT}; found '${slotNumber}'.`
    );
  }
  return `${CI_PHASE_SLOT_NAME_PREFIX} ${String(slotNumber).padStart(2, "0")}`;
}

export function ciPhaseSlotNames(): string[] {
  return Array.from(
    { length: CI_PHASE_SLOT_COUNT },
    (_, index) => ciPhaseSlotName(index + 1)
  );
}

export function hasCiPhaseSlotNamePrefix(name: string): boolean {
  return name.startsWith(`${CI_PHASE_SLOT_NAME_PREFIX} `);
}
