package com.yplay.ypprotocol;

public class YPPPackage {

    public static final int REQUEST_SIZE = 35;

    public static final byte VERSION_1 = 1;

    public static final byte TYPE_REQUEST = 'R';
    public static final byte TYPE_FILE = 'F';

    private String youtubeID;
    private String UUID;
    private byte quality;

    public YPPPackage(String youtubeID, String UUID, byte quality) {
        this.youtubeID = youtubeID;
        this.UUID = UUID;
        this.quality = quality;
    }

    public String getYoutubeID() {
        return youtubeID;
    }

    public String getUUID() {
        return UUID;
    }

    public byte getQuality() {
        return quality;
    }

    /*
    |0            0|1       1|2       17|18                33|34          34|
    *************************************************************************
    *** VERSION(1) | TYPE(1) | UUID(16) | YOUTUBE_ID(11 + 5) | QUALITY(1) ***
    *************************************************************************
    */
    public byte[] asRequest() {
        byte[] array = new byte[REQUEST_SIZE];

        // VERSION(1)
        array[0] = VERSION_1;

        // TYPE(1)
        array[1] = TYPE_REQUEST;

        // UUID(16)
        for (int i = 0; i < UUID.length(); i++) {
            array[2 + i] = (byte) UUID.charAt(i);
        }

        // YOUTUBE_ID(11 + 5)
        for (int i = 0; i < youtubeID.length(); i++) {
            array[18 + i] = (byte) youtubeID.charAt(i);
        }

        // QUALITY(1)
        array[34] = quality;

        return array;
    }

}
