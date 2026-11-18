// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
// Consegna 5, variante 2: la classe Livelli della consegna 3, con guadagnaVita in più. I contratti ci sono;
// i campi e i corpi dei metodi li scrivete voi (potete ripartire dalla vostra consegna 3).
package pong;

/**
 * Il gioco a livelli contro il giocatore automatico: a che livello si è e quante vite restano.
 * Si vince un livello vincendo una partita; perdendo una partita si perde una vita.
 */
public class Livelli {
    int livelloCorrente;
    int numeroLivelli;
    int viteIniziali;
    int viteAttuali;
    Lato umano;
    boolean completato;

    /**
     * Un gioco nuovo.
     * Pretende: niente.
     * Promette: livello 1, con tutte le vite; il gioco non è completato né perso.
     * Lancia IllegalArgumentException se numeroLivelli o vite è minore di 1, o se umano è null.
     */
    public Livelli(int numeroLivelli, int vite, Lato umano) {
        if (umano == null) {
            throw new IllegalArgumentException("Il lato dell'umano non può essere nullo");
        }
        if (vite < 1) {
            throw new IllegalArgumentException("Le vite devono essere almeno una");
        }
        if (numeroLivelli < 1) {
            throw new IllegalArgumentException("I livelli devono essere almeno uno");
        }
        livelloCorrente = 1;
        this.numeroLivelli = numeroLivelli;
        viteIniziali = viteAttuali= vite;
        this.umano = umano;
        completato = false;
    }

    /** Il livello a cui si gioca, fra 1 e getNumeroLivelli(). */
    public int getLivello() {
        return livelloCorrente;
    }

    /** Quanti livelli ha il gioco. */
    public int getNumeroLivelli() {
        return numeroLivelli; 
    }

    /** Le vite che restano, fra 0 e quelle iniziali. */
    public int getVite() {
        return viteAttuali;
    }

    /** Il lato su cui gioca la persona (l'altro è il giocatore automatico). */
    public Lato getUmano() {
        return umano; 
    }

    /** Vero se è stato vinto l'ultimo livello. */
    public boolean isCompletato() {
        return completato;
    }

    /** Vero se le vite sono finite. */
    public boolean isPerso() {
        return viteAttuali == 0; 
    }

    /** Vero se il gioco è completato o perso. */
    public boolean isFinito() {
        return completato || isPerso();
    }

    /**
     * Registra com'è finita una partita.
     * Pretende: niente.
     * Promette: se il gioco è finito (completato o perso), non cambia niente. Altrimenti:
     *           se ha vinto umano, si passa al livello dopo; se era l'ultimo, il gioco è completato
     *           e il livello resta l'ultimo; le vite non cambiano.
     *           Se ha vinto l'altro lato, una vita in meno e il livello non cambia;
     *           con l'ultima vita il gioco è perso.
     * Lancia IllegalArgumentException se vincitore è null, e non cambia niente.
     */
    public void partitaFinita(Lato vincitore) {
        if (vincitore == null) {
            throw new IllegalArgumentException("il vincitore non può essere nullo");
        }
        if (!isFinito()) {
            if (vincitore == umano) {
                if (livelloCorrente == numeroLivelli) {
                    completato = true;
                } else {
                    livelloCorrente++;
                }
            } else {
                viteAttuali--;
            }
        }
    }

    /**
     * Si ricomincia da capo.
     * Pretende: niente.
     * Promette: livello 1, con tutte le vite; il gioco non è completato né perso.
     */
    public void ricomincia() {
        livelloCorrente = 1;
        completato = false;
        viteAttuali = viteIniziali;
    }

    /**
     * Si riguadagna una vita (variante 2: con le stelle).
     * Pretende: niente.
     * Promette: se il gioco è finito (completato o perso), non cambia niente. Altrimenti una vita in più,
     *           ma mai più delle vite iniziali: con tutte le vite, non cambia niente. Il livello non cambia.
     */
    public void guadagnaVita() {
        if (!isFinito() && viteAttuali < viteIniziali) {
            viteAttuali++;
        }
    }
}
