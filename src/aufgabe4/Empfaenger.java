package aufgabe4;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: denisfleischhauer
 * Date: 04.06.13
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class Empfaenger extends Thread {
    private Connection connection;

    private long aktuelleFrameNummer;
    private long alteFrameNummer;
    private long aktuelleSlotNummer;
    private long alteSlotNummer;

    private boolean kollision = false;
    private int systemZeitAbweichung;
    private boolean[] belegteSlots;

    private boolean[] freieSlots;
    private long abweichung;

    public Empfaenger(Connection connection, int systemZeitAbweichung) {
        this.connection = connection;

        this.aktuelleFrameNummer = getZeit() / 1000;
        this.alteFrameNummer = getZeit() / 1000;

        this.aktuelleSlotNummer = 0;
        this.alteSlotNummer = 0;

        this.systemZeitAbweichung = systemZeitAbweichung;

        this.abweichung = 0;

        this.freieSlots = new boolean[25];
        Arrays.fill(freieSlots, true);

    this.belegteSlots = new boolean[25];
	Arrays.fill(belegteSlots, false);

        start();
    }

    public void run() {
        while (true) {
                Nachricht nachricht = new Nachricht(connection.receive());
                setBelegteSlots(nachricht.getReserviertenSlot());

                long empfangszeit = getZeit();


                // Akktualisierung der Abweichung wenn Nachricht von Station A
                // Mittel von der alten und neuen Abweichung
                if (nachricht.getStationsKlasse() == 'A') {
                    abweichung = (empfangszeit - nachricht.getSendezeit() + abweichung) / 2;
                }

                aktuelleFrameNummer = synchrinisierteZeit() / 1000;
                aktuelleSlotNummer = (synchrinisierteZeit() % 1000) / 40;


                // freie Slots fuer Reservierung zuruecksetzen wenn Frame zuende
                if (alteFrameNummer < aktuelleFrameNummer) {
                    System.out.println(" ==================== ");
                    resetFreieSlots();
	            resetBelegteSlots();
                }

                // wenn gleicher Frame und Slot
                if (belegteSlots[(int) aktuelleSlotNummer]) {
                    // dann Kollision

                    String msg = "--kollision im Frame: " + (aktuelleFrameNummer % 1000) + " Slot: " + aktuelleSlotNummer + nachricht.toString("");
                    System.out.println(msg);

                    // Nachricht nicht auswerten und Rest ueberspringen

                } else {

                    String msg = nachricht.toString("emfpangen im Frame: " + (aktuelleFrameNummer % 1000) + " Slot: " + aktuelleSlotNummer + " Abweichung: " + abweichung);
                    System.out.println(msg);
                    alteFrameNummer = aktuelleFrameNummer;
                    alteSlotNummer = aktuelleSlotNummer;
                }

                //kollisionen[(int)aktuelleSlotNummer] = true;
            }
    }

    public long getZeit() {
        return System.currentTimeMillis() + systemZeitAbweichung;
    }

    public long getAbweichung() {
        return abweichung;
    }

    public synchronized int getFreienSlot() {
        ArrayList<Integer> liste = new ArrayList<Integer>();
        for (int i = 0; i < belegteSlots.length; i++) {
            if (!belegteSlots[i]) {
                liste.add(i);
            }
        }
        Random random = new Random();

        return liste.get(random.nextInt(liste.size()));
       /*
        for(int i = 0; i < belegteSlots.length; i++) {
            if (freieSlots[i]) {
                return i;
            }
        }
        return 0; */
    }

    public synchronized void setFreienSlot(int i) {
        freieSlots[i] = false;
    }

    public synchronized void resetFreieSlots() {
        Arrays.fill(freieSlots, false);
    }

    public synchronized void resetBelegteSlots(){
        Arrays.fill(belegteSlots, false);
    }

    public synchronized void setBelegteSlots(int slot){
        belegteSlots[slot] = true;
    }


    public boolean isKollision() {
        return kollision;
    }

    public long synchrinisierteZeit() {
        return System.currentTimeMillis() + systemZeitAbweichung - abweichung;
    }
}
