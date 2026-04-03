// This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

package com.gamebuster19901.excite.modding.game.file.kaitai;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import io.kaitai.struct.KaitaiStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

public class ResMonster extends KaitaiStruct {
    public static ResMonster fromFile(String fileName) throws IOException {
        return new ResMonster(new ByteBufferKaitaiStream(fileName));
    }

    public ResMonster(KaitaiStream _io) {
        this(_io, null, null);
    }

    public ResMonster(KaitaiStream _io, KaitaiStruct _parent) {
        this(_io, _parent, null);
    }

    public ResMonster(KaitaiStream _io, KaitaiStruct _parent, ResMonster _root) {
        super(_io);
        this._parent = _parent;
        this._root = _root == null ? this : _root;
        _read();
    }
    private void _read() {
        this.header = new Header(this._io, this, _root);
        this.data = new Data(this._io, this, _root);
    }
    public static class Header extends KaitaiStruct {
        public static Header fromFile(String fileName) throws IOException {
            return new Header(new ByteBufferKaitaiStream(fileName));
        }

        public Header(KaitaiStream _io) {
            this(_io, null, null);
        }

        public Header(KaitaiStream _io, ResMonster _parent) {
            this(_io, _parent, null);
        }

        public Header(KaitaiStream _io, ResMonster _parent, ResMonster _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.magic1 = this._io.readBytes(4);
            if (!(Arrays.equals(magic1(), new byte[] { 48, 84, 83, 82 }))) {
                throw new KaitaiStream.ValidationNotEqualError(new byte[] { 48, 84, 83, 82 }, magic1(), _io(), "/types/header/seq/0");
            }
            this.sizeHeader = this._io.readU4le();
            this.version = this._io.readU4le();
            this.compressionBitfield = new ArrayList<Boolean>();
            for (int i = 0; i < 32; i++) {
                this.compressionBitfield.add(this._io.readBitsIntBe(1) != 0);
            }
            this._io.alignToByte();
            this.archiveSize = this._io.readU4le();
            this.unixTimestamp = this._io.readU4le();
            this.rcmpOffset = this._io.readU4le();
            this.unknown1 = this._io.readU4le();
            this.numFile = this._io.readU4le();
            this.uncompressedSize = this._io.readU4le();
            this.compressedSize = this._io.readU4le();
            this.compressed = this._io.readU4le();
            this.unknown3 = this._io.readU4le();
            this.filenameDirlen = this._io.readU4le();
            this.unknown4 = this._io.readU4le();
            this.hash = this._io.readU4le();
            this.unknown5 = new ArrayList<Integer>();
            for (int i = 0; i < 64; i++) {
                this.unknown5.add(this._io.readU1());
            }
        }
        private byte[] magic1;
        private long sizeHeader;
        private long version;
        private ArrayList<Boolean> compressionBitfield;
        private long archiveSize;
        private long unixTimestamp;
        private long rcmpOffset;
        private long unknown1;
        private long numFile;
        private long uncompressedSize;
        private long compressedSize;
        private long compressed;
        private long unknown3;
        private long filenameDirlen;
        private long unknown4;
        private long hash;
        private ArrayList<Integer> unknown5;
        private ResMonster _root;
        private ResMonster _parent;
        public byte[] magic1() { return magic1; }
        public long sizeHeader() { return sizeHeader; }
        public long version() { return version; }
        public ArrayList<Boolean> compressionBitfield() { return compressionBitfield; }
        public long archiveSize() { return archiveSize; }

        /**
         * Creation Date
         */
        public long unixTimestamp() { return unixTimestamp; }
        public long rcmpOffset() { return rcmpOffset; }
        public long unknown1() { return unknown1; }
        public long numFile() { return numFile; }

        /**
         * Add 128
         */
        public long uncompressedSize() { return uncompressedSize; }

        /**
         * Add 128
         */
        public long compressedSize() { return compressedSize; }

        /**
         * 128 if compressed 0 if uncompressed
         */
        public long compressed() { return compressed; }
        public long unknown3() { return unknown3; }
        public long filenameDirlen() { return filenameDirlen; }
        public long unknown4() { return unknown4; }
        public long hash() { return hash; }
        public ArrayList<Integer> unknown5() { return unknown5; }
        public ResMonster _root() { return _root; }
        public ResMonster _parent() { return _parent; }
    }
    public static class Data extends KaitaiStruct {
        public static Data fromFile(String fileName) throws IOException {
            return new Data(new ByteBufferKaitaiStream(fileName));
        }

        public Data(KaitaiStream _io) {
            this(_io, null, null);
        }

        public Data(KaitaiStream _io, ResMonster _parent) {
            this(_io, _parent, null);
        }

        public Data(KaitaiStream _io, ResMonster _parent, ResMonster _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            if ( ((_root().header().compressed() == 128) || (_root().header().compressed() == 1152)) ) {
                this.compressedData = new QuicklzRcmp(this._io);
            }
            if (_root().header().compressed() == 0) {
                this.uncompressedData = this._io.readBytesFull();
            }
        }
        private QuicklzRcmp compressedData;
        private byte[] uncompressedData;
        private ResMonster _root;
        private ResMonster _parent;
        public QuicklzRcmp compressedData() { return compressedData; }
        public byte[] uncompressedData() { return uncompressedData; }
        public ResMonster _root() { return _root; }
        public ResMonster _parent() { return _parent; }
    }
    private Header header;
    private Data data;
    private ResMonster _root;
    private KaitaiStruct _parent;
    public Header header() { return header; }
    public Data data() { return data; }
    public ResMonster _root() { return _root; }
    public KaitaiStruct _parent() { return _parent; }
}
