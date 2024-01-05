package com.arms.egovframework.javaservice.treeframework.util;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EgovFormBasedUUID implements Serializable {
    private final long mostSigBits;
    private final long leastSigBits;
    private transient int version = -1;
    private transient int variant = -1;
    private transient volatile long timestamp = -1L;
    private transient int sequence = -1;
    private transient long node = -1L;
    private transient int hashCode = -1;

    private static SecureRandom numberGenerator = null;

    private EgovFormBasedUUID(byte[] data) {
        long msb = 0L;
        long lsb = 0L;

        int i;
        for(i = 0; i < 8; ++i) {
            msb = msb << 8 | (long)(data[i] & 255);
        }

        for(i = 8; i < 16; ++i) {
            lsb = lsb << 8 | (long)(data[i] & 255);
        }

        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    public EgovFormBasedUUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    public static EgovFormBasedUUID randomUUID() {
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] = (byte)(randomBytes[6] & 15);
        randomBytes[6] = (byte)(randomBytes[6] | 64);
        randomBytes[8] = (byte)(randomBytes[8] & 63);
        randomBytes[8] = (byte)(randomBytes[8] | 128);
        return new EgovFormBasedUUID(randomBytes);
    }

    public static EgovFormBasedUUID nameUUIDFromBytes(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException var6) {
            throw new InternalError("SHA-256 not supported");
        }

        if (md == null) {
            throw new RuntimeException("MessageDigest is null!!");
        } else {
            SecureRandom ng = new SecureRandom();
            byte[] randomBytes = new byte[16];
            ng.nextBytes(randomBytes);
            md.reset();
            md.update(randomBytes);
            byte[] sha = md.digest(name);
            byte[] md5Bytes = new byte[8];
            System.arraycopy(sha, 0, md5Bytes, 0, 8);
            md5Bytes[6] = (byte)(md5Bytes[6] & 15);
            md5Bytes[6] = (byte)(md5Bytes[6] | 48);
            md5Bytes[8] = (byte)(md5Bytes[8] & 63);
            md5Bytes[8] = (byte)(md5Bytes[8] | 128);
            return new EgovFormBasedUUID(md5Bytes);
        }
    }

    public static EgovFormBasedUUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid UUID string: " + name);
        } else {
            for(int i = 0; i < 5; ++i) {
                components[i] = "0x" + components[i];
            }

            long mostSigBits = Long.decode(components[0]);
            mostSigBits <<= 16;
            mostSigBits |= Long.decode(components[1]);
            mostSigBits <<= 16;
            mostSigBits |= Long.decode(components[2]);
            long leastSigBits = Long.decode(components[3]);
            leastSigBits <<= 48;
            leastSigBits |= Long.decode(components[4]);
            return new EgovFormBasedUUID(mostSigBits, leastSigBits);
        }
    }

    public long getLeastSignificantBits() {
        return this.leastSigBits;
    }

    public long getMostSignificantBits() {
        return this.mostSigBits;
    }

    public int version() {
        if (this.version < 0) {
            this.version = (int)(this.mostSigBits >> 12 & 15L);
        }

        return this.version;
    }

    public int variant() {
        if (this.variant < 0) {
            if (this.leastSigBits >>> 63 == 0L) {
                this.variant = 0;
            } else if (this.leastSigBits >>> 62 == 2L) {
                this.variant = 2;
            } else {
                this.variant = (int)(this.leastSigBits >>> 61);
            }
        }

        return this.variant;
    }

    public long timestamp() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        } else {
            long result = this.timestamp;
            if (result < 0L) {
                result = (this.mostSigBits & 4095L) << 48;
                result |= (this.mostSigBits >> 16 & 65535L) << 32;
                result |= this.mostSigBits >>> 32;
                this.timestamp = result;
            }

            return result;
        }
    }

    public int clockSequence() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        } else {
            if (this.sequence < 0) {
                this.sequence = (int)((this.leastSigBits & 4611404543450677248L) >>> 48);
            }

            return this.sequence;
        }
    }

    public long node() {
        if (this.version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        } else {
            if (this.node < 0L) {
                this.node = this.leastSigBits & 281474976710655L;
            }

            return this.node;
        }
    }

    public String toString() {
        return digits(this.mostSigBits >> 32, 8) + "-" + digits(this.mostSigBits >> 16, 4) + "-" + digits(this.mostSigBits, 4) + "-" + digits(this.leastSigBits >> 48, 4) + "-" + digits(this.leastSigBits, 12);
    }

    private static String digits(long val, int digits) {
        long hi = 1L << digits * 4;
        return Long.toHexString(hi | val & hi - 1L).substring(1);
    }

    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = (int)(this.mostSigBits >> 32 ^ this.mostSigBits ^ this.leastSigBits >> 32 ^ this.leastSigBits);
        }

        return this.hashCode;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof EgovFormBasedUUID)) {
            return false;
        } else if (((EgovFormBasedUUID)obj).variant() != this.variant()) {
            return false;
        } else {
            EgovFormBasedUUID id = (EgovFormBasedUUID)obj;
            return this.mostSigBits == id.mostSigBits && this.leastSigBits == id.leastSigBits;
        }
    }

    public int compareTo(EgovFormBasedUUID val) {
        return this.mostSigBits < val.mostSigBits ? -1 : (this.mostSigBits > val.mostSigBits ? 1 : (this.leastSigBits < val.leastSigBits ? -1 : (this.leastSigBits > val.leastSigBits ? 1 : 0)));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.version = -1;
        this.variant = -1;
        this.timestamp = -1L;
        this.sequence = -1;
        this.node = -1L;
        this.hashCode = -1;
    }
}
