package Manager;

import Manager.*;
import UI.*;
import Dispositivos.*;


public class Main {
	public static void main(String[] args) {
		DispositivoCasa smartTV = new SmartTV();
		ControlRemoto remoteForSmartTV = new ControlRemoto(smartTV);

	    // Usa el control remoto para controlar el SmartTV
	    remoteForSmartTV.displayPhoto("Vacaciones.jpg");
	    remoteForSmartTV.adjustVolume(50);
	    remoteForSmartTV.setDisplaySize(1920, 1080);

	    DispositivoCasa proyector = new Proyector();
	    ControlRemoto remoteForProyector = new ControlRemoto(proyector);

	    // Usa el control remoto para controlar el proyector
	    remoteForProyector.displayPhoto("Presentacion.ppt");
	    remoteForProyector.adjustVolume(30);
	    remoteForProyector.setDisplaySize(1280, 720);
	}

}
