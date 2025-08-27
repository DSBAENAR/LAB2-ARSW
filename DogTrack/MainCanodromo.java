package DogTrack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;

public class MainCanodromo {

    private static Galgo[] galgos;

    private static Canodromo can;

    private static RegistroLlegada reg = new RegistroLlegada();

    private static ControlLock control = new ControlLock();

   
    /*
     * The main entry point for the DogTrack application.
     * <p>
     * Initializes the race track and sets up the event listeners for the GUI controls:
     * <ul>
     *   <li>Start: Launches a new thread to create and start the Galgo (dog) threads, waits for them to finish, and displays the winner.</li>
     *   <li>Stop: Pauses the race using the control object.</li>
     *   <li>Continue: Resumes the race using the control object.</li>
     *   <li>Restart: Resumes the race, restarts the race track, and re-enables the start button.</li>
     * </ul>
     * The use of threads ensures that the GUI remains responsive during the race.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        can = new Canodromo(17, 100);
        galgos = new Galgo[can.getNumCarriles()];
        can.setVisible(true);

        
        can.setStartAction(
                new ActionListener() {

                    /**
                     * Handles the action event triggered by the button press.
                     * <p>
                     * This method disables the source button and starts a new thread to manage the race logic.
                     * Within the new thread, it performs the following actions:
                     * <ul>
                     *   <li>Creates and starts a thread for each 'Galgos' (dog) participating in the race.</li>
                     *   <li>Waits for all 'Galgos' threads to finish using {@code join()} to ensure the race completes before proceeding.</li>
                     *   <li>Displays the winner dialog and prints the winner information to the console.</li>
                     *   <li>Re-enables the source button after the race is complete.</li>
                     * </ul>
                     * This approach ensures that the graphical user interface remains responsive by offloading the race logic to a separate thread.
                     *
                     * @param e the {@link ActionEvent} that triggered this handler, typically a button press
                     */
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        ((JButton) e.getSource()).setEnabled(false);
                        new Thread() {
                            public void run() {
                                for (int i = 0; i < can.getNumCarriles(); i++) {

                                    galgos[i] = new Galgo(can.getCarril(i), "" + i, reg, control);
                                    galgos[i].start();


                                }
                                
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
                    /**
                     * Handles the action event triggered by the associated component.
                     * This implementation pauses the race by invoking the control.pause() method
                     * and prints a message to the console indicating that the race has been paused.
                     *
                     * @param e the ActionEvent that occurred
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.pause();
                        System.out.println("Carrera pausada!");

                    }
                }
        );

        can.setContinueAction(
                new ActionListener() {
                    /**
                     * Handles the action event triggered when the associated component is activated.
                     * Resumes the race by calling the control's resume method and prints a message
                     * indicating that the race has been resumed.
                     *
                     * @param e the ActionEvent that occurred
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        control.resume();
                        System.out.println("Carrera reanudada!");
                    }
                }
        );

        can.setRestartAction(
                new ActionListener() {
                    /**
                     * Handles the action event triggered by a user interaction, such as clicking a button.
                     * This method resumes the race control, restarts the race, prints a message to the console,
                     * and re-enables the source button.
                     *
                     * @param e the ActionEvent generated by the user's action
                     */
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