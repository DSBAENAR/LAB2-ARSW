package DogTrack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;

public class MainCanodromo {

    private static Galgo[] galgos;

    private static Canodromo can;

    private static RegistroLlegada reg = new RegistroLlegada();

    private static ControlLock control = new ControlLock();

    public static void main(String[] args) {
        can = new Canodromo(17, 100);
        galgos = new Galgo[can.getNumCarriles()];
        can.setVisible(true);

        //Acción del botón start
        can.setStartAction(
                new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
						//como acción, se crea un nuevo hilo que cree los hilos
                        //'galgos', los pone a correr, y luego muestra los resultados.
                        //La acción del botón se realiza en un hilo aparte para evitar
                        //bloquear la interfaz gráfica.
                        ((JButton) e.getSource()).setEnabled(false);
                        new Thread() {
                            public void run() {
                                for (int i = 0; i < can.getNumCarriles(); i++) {

                                    //crea los hilos 'galgos'
                                    galgos[i] = new Galgo(can.getCarril(i), "" + i, reg, control);
                                    //inicia los hilos
                                    galgos[i].start();


                                }
                                //espera a que terminen los hilos
                                for (int i = 0; i < can.getNumCarriles(); i++) {
                                    try {
                                        galgos[i].join();
                                    } catch (InterruptedException exception) {
                                        exception.printStackTrace();
                                    }
                                }

				                can.winnerDialog(reg.getGanador(),reg.getUltimaPosicionAlcanzada() - 1); 
                                System.out.println("El ganador fue:" + reg.getGanador());
                                ((JButton) e.getSource()).setEnabled(true);
                            }
                        }.start();
                        

                    }
                }
        );

        can.setStopAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.pause();
                        System.out.println("Carrera pausada!");

                    }
                }
        );

        can.setContinueAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.resume();
                        System.out.println("Carrera reanudada!");
                    }
                }
        );

        can.setRestartAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.resume();
                        can.restart();

                        System.out.println("Carrera reiniciada!");
                        ((JButton) e.getSource()).setEnabled(true);
                    }
                }
        );

    }

}