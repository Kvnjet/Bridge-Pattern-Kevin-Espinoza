package Manager;

import Dispositivos.*;

public class ControlRemoto {
	private DispositivoCasa DispositivoCasa;

    public ControlRemoto(DispositivoCasa DispositivoCasa) {
        this.DispositivoCasa = DispositivoCasa;
    }

    public void displayPhoto(String photo) {
    	DispositivoCasa.showPhoto(photo);
    }

    public void adjustVolume(int volume) {
    	DispositivoCasa.changeVolume(volume);
    }

    public void setDisplaySize(int width, int height) {
    	DispositivoCasa.autoAdjustDisplaySize();
    }
}
