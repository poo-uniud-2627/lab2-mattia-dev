// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import java.awt.event.KeyEvent;

/**
 * L'avvio: apre la finestra. In VS Code: «Run» sopra main.
 * Voi giocate a sinistra con le frecce, contro il giocatore automatico a destra.
 * SPAZIO fa partire la palla; i livelli avanzano automaticamente.
 */
public class Main {

    public static void main(String[] args) {
        Tastiera tastiera = new Tastiera();
        Giocatore voi = new GiocatoreTastiera(tastiera, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
        Partita partita = new Partita(new Campo(800, 500), voi);
        Schermo.mostra(partita, tastiera);
    }
}
