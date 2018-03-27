package com.example.michael.handpiano;

/**
 * Created by john on 2018/3/9.
 */

import org.billthefarmer.mididriver.MidiDriver;

import android.os.Handler;
import java.util.logging.LogRecord;

class MidiPlayer extends MidiDriver{
    private byte[] event;
    private static final int[] notesRelation = {0, 2, 4, 5, 7, 9, 11};
    private int[] currentNotes = {0, 2, 4, 5, 7, 9, 11};
    private int currentMajor = 60;
    private Handler handler=new Handler();

    public void setMajor(int major){
        if (major < 0 || major > 6) return;

        for (int i = 0; i < 7; ++i) {
            currentNotes[i] = notesRelation[(major + i) % 7] - notesRelation[major];
            if (currentNotes[i] < 0)
                currentNotes[i] += 12;
        }
        currentMajor = 60 + notesRelation[major];

    }

    public void playShortOctave(byte octave, final byte channel, long period){

        if (octave < 1 || octave > 7) return;

        final byte note = (byte) (currentMajor + currentNotes[octave - 1]);
        playNote(note, channel);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                stopNote(note, channel);
            }
        };
        handler.postDelayed(r, period);

    }

    public void playLongOctave(byte octave, byte channel){

        if (octave < 1 || octave > 7) return;

        byte note = (byte) (currentMajor + currentNotes[octave - 1]);
        playNote(note, channel);

    }

    public void stopOctave(byte octave, byte channel){
        if (octave < 1 || octave > 7) return;

        byte note = (byte) (currentMajor + currentNotes[octave - 1]);
        stopNote(note, channel);
    }

    public void playNote(byte note, byte channel) {

        // Construct a note ON message for the middle C at maximum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x90 | channel);  // 0x90 = note On, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)

        // Internally this just calls write() and can be considered obsoleted:
        //midiDriver.queueEvent(event);

        // Send the MIDI event to the synthesizer.
        write(event);

    }

    public void stopNote(byte note, byte channel) {

        // Construct a note OFF message for the middle C at minimum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x80 | channel);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

        // Send the MIDI event to the synthesizer.
        write(event);

    }

    public void selectInstrument(byte instrument, byte channel){
        // Construct a program change to select the instrument on channel 1:
        event = new byte[2];
        event[0] = (byte)(0xC0 | channel); // 0xC0 = program change, 0x00 = channel 1
        event[1] = (byte)instrument;

        // Send the MIDI event to the synthesizer.
        write(event);
    }

}
