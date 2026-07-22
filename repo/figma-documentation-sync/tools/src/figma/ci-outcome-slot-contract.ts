import {
  CI_OUTCOME_SLOT_COUNT,
  CI_OUTCOME_SLOT_NAME_PREFIX,
} from "@figma-documentation-sync/project-config";

export function ciOutcomeSlotName(slotNumber: number): string {
  if (!Number.isInteger(slotNumber) || slotNumber < 1 || slotNumber > CI_OUTCOME_SLOT_COUNT) {
    throw new Error(
      `CI outcome slot number must be between 1 and ${CI_OUTCOME_SLOT_COUNT}; found '${slotNumber}'.`
    );
  }
  return `${CI_OUTCOME_SLOT_NAME_PREFIX} ${String(slotNumber).padStart(2, "0")}`;
}

export function ciOutcomeSlotNames(): string[] {
  return Array.from(
    { length: CI_OUTCOME_SLOT_COUNT },
    (_, index) => ciOutcomeSlotName(index + 1)
  );
}

export function hasCiOutcomeSlotNamePrefix(name: string): boolean {
  return name.startsWith(`${CI_OUTCOME_SLOT_NAME_PREFIX} `);
}
