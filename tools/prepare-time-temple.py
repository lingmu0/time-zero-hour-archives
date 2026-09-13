#!/usr/bin/env python3
"""Normalize and center a large Minecraft structure template before packaging it.

The export used for the Time Temple stores every position in its 219x202x76
volume, including more than three million explicit air entries. Vanilla can
place a structure across chunk boundaries, but carrying those air entries
makes every intersecting chunk do unnecessary work. The natural-structure
reference pass also only scans eight chunks around the start, so the template
coordinates must be centered around that start instead of extending entirely
to one side. This tool recenters X/Z, keeps block entities and entities
aligned, and removes only entries whose palette state is minecraft:air.
"""

from __future__ import annotations

import gzip
import struct
import sys
from pathlib import Path


class Reader:
    def __init__(self, data: bytes):
        self.data = data
        self.pos = 0

    def read(self, size: int) -> bytes:
        end = self.pos + size
        if end > len(self.data):
            raise ValueError("unexpected end of NBT data")
        result = self.data[self.pos:end]
        self.pos = end
        return result

    def byte(self) -> int:
        return self.read(1)[0]

    def string(self) -> str:
        size = struct.unpack(">H", self.read(2))[0]
        return self.read(size).decode("utf-8")

    def payload(self, tag_type: int):
        if tag_type == 1:
            return struct.unpack(">b", self.read(1))[0]
        if tag_type == 2:
            return struct.unpack(">h", self.read(2))[0]
        if tag_type == 3:
            return struct.unpack(">i", self.read(4))[0]
        if tag_type == 4:
            return struct.unpack(">q", self.read(8))[0]
        if tag_type == 5:
            return struct.unpack(">f", self.read(4))[0]
        if tag_type == 6:
            return struct.unpack(">d", self.read(8))[0]
        if tag_type == 7:
            return list(self.read(struct.unpack(">i", self.read(4))[0]))
        if tag_type == 8:
            return self.string()
        if tag_type == 9:
            element_type = self.byte()
            count = struct.unpack(">i", self.read(4))[0]
            return element_type, [self.payload(element_type) for _ in range(count)]
        if tag_type == 10:
            tags = []
            while True:
                child_type = self.byte()
                if child_type == 0:
                    return tags
                tags.append((child_type, self.string(), self.payload(child_type)))
        if tag_type == 11:
            count = struct.unpack(">i", self.read(4))[0]
            return list(struct.unpack(">" + "i" * count, self.read(4 * count)))
        if tag_type == 12:
            count = struct.unpack(">i", self.read(4))[0]
            return list(struct.unpack(">" + "q" * count, self.read(8 * count)))
        raise ValueError(f"unsupported NBT tag type {tag_type}")

    def tag(self):
        tag_type = self.byte()
        if tag_type == 0:
            return tag_type, "", None
        return tag_type, self.string(), self.payload(tag_type)


class Writer:
    def __init__(self):
        self.data = bytearray()

    def put(self, data: bytes):
        self.data.extend(data)

    def string(self, value: str):
        encoded = value.encode("utf-8")
        self.put(struct.pack(">H", len(encoded)))
        self.put(encoded)

    def payload(self, tag_type: int, value):
        if tag_type == 1:
            self.put(struct.pack(">b", value))
        elif tag_type == 2:
            self.put(struct.pack(">h", value))
        elif tag_type == 3:
            self.put(struct.pack(">i", value))
        elif tag_type == 4:
            self.put(struct.pack(">q", value))
        elif tag_type == 5:
            self.put(struct.pack(">f", value))
        elif tag_type == 6:
            self.put(struct.pack(">d", value))
        elif tag_type == 7:
            self.put(struct.pack(">i", len(value)))
            self.put(bytes(value))
        elif tag_type == 8:
            self.string(value)
        elif tag_type == 9:
            element_type, items = value
            self.put(bytes([element_type]))
            self.put(struct.pack(">i", len(items)))
            for item in items:
                self.payload(element_type, item)
        elif tag_type == 10:
            for child_type, name, child_value in value:
                self.tag(child_type, name, child_value)
            self.put(b"\x00")
        elif tag_type == 11:
            self.put(struct.pack(">i", len(value)))
            for item in value:
                self.put(struct.pack(">i", item))
        elif tag_type == 12:
            self.put(struct.pack(">i", len(value)))
            for item in value:
                self.put(struct.pack(">q", item))
        else:
            raise ValueError(f"unsupported NBT tag type {tag_type}")

    def tag(self, tag_type: int, name: str, value):
        self.put(bytes([tag_type]))
        if tag_type == 0:
            return
        self.string(name)
        self.payload(tag_type, value)


def child(compound, name: str):
    for tag_type, tag_name, value in compound:
        if tag_name == name:
            return tag_type, value
    raise KeyError(name)


def main() -> int:
    if len(sys.argv) != 3:
        print("usage: prepare-time-temple.py <source.nbt> <target.nbt>", file=sys.stderr)
        return 2
    source = Path(sys.argv[1])
    target = Path(sys.argv[2])
    with gzip.open(source, "rb") as stream:
        reader = Reader(stream.read())
    root_type, root_name, root = reader.tag()
    if root_type != 10:
        raise ValueError("structure root is not a compound")

    palette_type, palette = child(root, "palette")
    if palette_type != 9:
        raise ValueError("structure palette is not a list")
    palette_names = [child(entry, "Name")[1] for entry in palette[1]]

    size_type, size_value = child(root, "size")
    if size_type != 9 or size_value[0] != 3 or len(size_value[1]) != 3:
        raise ValueError("structure size is not an integer triplet")
    size = size_value[1]
    offset_x = -(size[0] // 2)
    offset_z = -(size[2] // 2)

    def shift_position(compound, name: str, element_type: int) -> bool:
        for index, (tag_type, tag_name, value) in enumerate(compound):
            if tag_name != name:
                continue
            if tag_type != 9 or value[0] != element_type or len(value[1]) != 3:
                raise ValueError(f"structure {name} is not a 3-element list")
            coordinates = list(value[1])
            coordinates[0] += offset_x
            coordinates[2] += offset_z
            compound[index] = (tag_type, tag_name, (value[0], coordinates))
            return True
        return False

    blocks_index = next(i for i, tag in enumerate(root) if tag[1] == "blocks")
    blocks_type, blocks_value = root[blocks_index][0], root[blocks_index][2]
    if blocks_type != 9 or blocks_value[0] != 10:
        raise ValueError("structure blocks are not a compound list")
    original_count = len(blocks_value[1])
    kept = []
    bounds = [[None, None], [None, None], [None, None]]
    for block in blocks_value[1]:
        if not shift_position(block, "pos", 3):
            raise ValueError("structure block has no integer pos list")
        state_index = child(block, "state")[1]
        if palette_names[state_index] != "minecraft:air":
            kept.append(block)
            coordinates = child(block, "pos")[1][1]
            for axis in range(3):
                bounds[axis][0] = coordinates[axis] if bounds[axis][0] is None else min(bounds[axis][0], coordinates[axis])
                bounds[axis][1] = coordinates[axis] if bounds[axis][1] is None else max(bounds[axis][1], coordinates[axis])
    root[blocks_index] = (blocks_type, "blocks", (blocks_value[0], kept))

    entities_index = next((i for i, tag in enumerate(root) if tag[1] == "entities"), None)
    if entities_index is not None:
        entities_type, entities_value = root[entities_index][0], root[entities_index][2]
        if entities_type != 9 or (entities_value[1] and entities_value[0] != 10):
            raise ValueError("structure entities are not a compound list")
        for entity in entities_value[1]:
            if not shift_position(entity, "pos", 6):
                raise ValueError("structure entity has no double pos list")
            shift_position(entity, "blockPos", 3)

    if any(
        bounds[axis][0] is not None
        and max(abs(bounds[axis][0]), abs(bounds[axis][1])) >= 128
        for axis in (0, 2)
    ):
        raise ValueError(f"recentered structure exceeds vanilla 128-block reference radius: {bounds}")

    writer = Writer()
    writer.tag(root_type, root_name, root)
    target.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(target, "wb", compresslevel=9) as stream:
        stream.write(writer.data)

    print(
        f"prepared {source} -> {target}: size={size}, "
        f"offset=({offset_x},0,{offset_z}), bounds={bounds}, "
        f"blocks={original_count}->{len(kept)}, removed_air={original_count-len(kept)}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
