import { deflateSync } from "node:zlib";

export const PAYLOAD_PNG_TEXT_KEYWORD = "figmaSyncPayload";
export const MAX_FIGMA_UPLOAD_ASSET_BYTES = 10 * 1024 * 1024;

export function buildOfficialSyncPayload({ designModel, modelJson, script, scriptBase64 }) {
  return {
    designModelJson: modelJson,
    designModelHash: designModel.modelHash,
    designModelGitSha: designModel.gitSha,
    designModelLength: modelJson.length,
    scriptBase64,
    scriptLength: script.length,
    scriptBase64Length: scriptBase64.length,
  };
}

export function stringifyAsciiJson(value) {
  return JSON.stringify(value).replace(/[\u007f-\uffff]/g, (character) =>
    `\\u${character.charCodeAt(0).toString(16).padStart(4, "0")}`
  );
}

export function createPayloadPng(payloadJson) {
  const encodedPayload = Buffer.from(payloadJson, "utf8").toString("base64");
  const textPayload = Buffer.concat([
    Buffer.from(`${PAYLOAD_PNG_TEXT_KEYWORD}\0`, "latin1"),
    Buffer.from(encodedPayload, "ascii"),
  ]);

  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(1, 0);
  ihdr.writeUInt32BE(1, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;

  const whitePixelScanline = Buffer.from([0, 255, 255, 255, 255]);
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    pngChunk("IHDR", ihdr),
    pngChunk("tEXt", textPayload),
    pngChunk("IDAT", deflateSync(whitePixelScanline)),
    pngChunk("IEND", Buffer.alloc(0)),
  ]);
}

function pngChunk(type, data) {
  const typeBytes = Buffer.from(type, "ascii");
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length, 0);

  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(Buffer.concat([typeBytes, data])), 0);

  return Buffer.concat([length, typeBytes, data, crc]);
}

const CRC32_TABLE = buildCrc32Table();

function buildCrc32Table() {
  const table = new Uint32Array(256);
  for (let index = 0; index < table.length; index += 1) {
    let crc = index;
    for (let bit = 0; bit < 8; bit += 1) {
      crc = (crc & 1) ? 0xedb88320 ^ (crc >>> 1) : crc >>> 1;
    }
    table[index] = crc >>> 0;
  }
  return table;
}

function crc32(bytes) {
  let crc = 0xffffffff;
  for (const byte of bytes) {
    crc = CRC32_TABLE[(crc ^ byte) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}
