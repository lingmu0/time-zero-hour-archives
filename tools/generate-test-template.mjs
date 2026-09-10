// Generate a minimal NBT template for the real Minecraft GameTest harness.
import fs from 'node:fs';
import path from 'node:path';
import { gzipSync } from 'node:zlib';
import { fileURLToPath } from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const i = n => {const b=Buffer.alloc(4); b.writeInt32BE(n); return b;};
const str = s => {const b=Buffer.from(s);const n=Buffer.alloc(2);n.writeUInt16BE(b.length);return Buffer.concat([n,b]);};
const tag = (type,name,payload) => Buffer.concat([Buffer.from([type]),str(name),payload]);
const list = (type,items) => Buffer.concat([Buffer.from([type]),i(items.length),...items]);
const data = tag(10,'',Buffer.concat([
  tag(3,'DataVersion',i(3955)), tag(9,'size',list(3,[i(8),i(8),i(8)])),
  tag(9,'palette',list(10,[Buffer.concat([tag(8,'Name',str('minecraft:air')),Buffer.from([0])])])),
  tag(9,'blocks',list(10,[])), tag(9,'entities',list(10,[])), Buffer.from([0])
]));
const out=path.join(root,'src/validation/resources/data/time_validation/structure/empty.nbt');
fs.mkdirSync(path.dirname(out),{recursive:true});fs.writeFileSync(out,gzipSync(data));
console.log('Generated isolated GameTest template:',out);
