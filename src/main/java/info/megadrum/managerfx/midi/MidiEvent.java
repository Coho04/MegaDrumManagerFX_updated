package info.megadrum.managerfx.midi;

import java.io.Serial;
import java.util.EventObject;

public class MidiEvent extends EventObject {

    @Serial
    private static final long serialVersionUID = -5177491652573679931L;

    public MidiEvent(Object source) {
        super(source);
    }
}