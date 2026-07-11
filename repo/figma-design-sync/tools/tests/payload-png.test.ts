import assert from "node:assert/strict";
import test from "node:test";
import {
  buildOfficialSyncPayload,
  createPayloadPng,
  PAYLOAD_PNG_TEXT_KEYWORD,
  stringifyAsciiJson,
} from "../scripts/payload-png";

test("stringifyAsciiJson escapes non-ascii characters and round-trips", () => {
  const value = {
    name: "dokkaDocumentación",
    nested: ["árbol", "plugin"],
  };

  const json = stringifyAsciiJson(value);

  assert.equal([...json].every((character) => character.charCodeAt(0) < 128), true);
  assert.deepEqual(JSON.parse(json), value);
});

test("createPayloadPng stores the official sync payload as a PNG text chunk", () => {
  const designModel = {
    modelHash: "hash-123",
    gitSha: "sha-123",
  };
  const modelJson = JSON.stringify(designModel);
  const script = "const value = 'sync';";
  const scriptBase64 = Buffer.from(script, "utf8").toString("base64");
  const payload = buildOfficialSyncPayload({
    designModel,
    modelJson,
    script,
    scriptBase64,
  });
  const payloadJson = stringifyAsciiJson(payload);

  const png = createPayloadPng(payloadJson);
  const encodedPayload = readPayloadFromPngText(png, PAYLOAD_PNG_TEXT_KEYWORD);

  assert.ok(encodedPayload);
  assert.deepEqual(
    JSON.parse(Buffer.from(encodedPayload, "base64").toString("utf8")),
    payload
  );
});

function readPayloadFromPngText(bytes: Buffer, keyword: string): string | null {
  assert.deepEqual(
    [...bytes.subarray(0, 8)],
    [137, 80, 78, 71, 13, 10, 26, 10]
  );

  let offset = 8;
  while (offset + 12 <= bytes.length) {
    const length = bytes.readUInt32BE(offset);
    const type = bytes.subarray(offset + 4, offset + 8).toString("latin1");
    const dataStart = offset + 8;
    const dataEnd = dataStart + length;
    const nextOffset = dataEnd + 4;

    assert.ok(dataEnd <= bytes.length);
    assert.ok(nextOffset <= bytes.length);

    if (type === "tEXt") {
      const text = bytes.subarray(dataStart, dataEnd).toString("latin1");
      const separatorIndex = text.indexOf("\0");
      if (separatorIndex > -1 && text.slice(0, separatorIndex) === keyword) {
        return text.slice(separatorIndex + 1);
      }
    }

    offset = nextOffset;
  }

  return null;
}
