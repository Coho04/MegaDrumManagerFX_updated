package info.megadrum.managerfx.ui;

import java.io.Serial;
import java.util.EventObject;

public class ControlChangeEvent extends EventObject {

	@Serial
    private static final long serialVersionUID = -5177491652573679931L;

	public ControlChangeEvent(Object source) {
        super(source);
    }
}
