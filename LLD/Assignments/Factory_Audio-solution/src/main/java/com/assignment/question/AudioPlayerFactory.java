package com.assignment.question;

public class AudioPlayerFactory {

    public static AudioPlayer getAudioPlayer(MediaFormat mediaFormat, int volume, double playBackRate) {
        return switch (mediaFormat) {
            case WAV -> new WAVPlayer(volume, playBackRate);
            case FLAC -> new FLACPlayer(volume, playBackRate);
            case MP3 -> new MP3Player(volume, playBackRate);
            default -> throw new IllegalArgumentException("Invalid media format");
        };
    }
}