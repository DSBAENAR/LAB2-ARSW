package DogTrack;

public class RegistroLlegada {

	private int ultimaPosicionAlcanzada=1;

	private String ganador=null;

	public synchronized int marcarLlegada(String nombreGalgo) {
        if (ganador == null) {
            ganador = nombreGalgo;
        }
        return ultimaPosicionAlcanzada++;
    }

	
	public synchronized String getGanador() {
		return ganador;
	}

	public void setGanador(String ganador) {
		this.ganador = ganador;
	}

	public synchronized int getUltimaPosicionAlcanzada() {
		return ultimaPosicionAlcanzada;
	}

	public void setUltimaPosicionAlcanzada(int ultimaPosicionAlcanzada) {
		this.ultimaPosicionAlcanzada = ultimaPosicionAlcanzada;
	}

	
	
}