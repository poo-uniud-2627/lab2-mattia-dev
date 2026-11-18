// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import giocatori.GiocatoreAutomatico;
import java.util.Random;
import geometria.Rettangolo;
import ostacoli.Livello;
import ostacoli.MotoreDeiLivelli;
import ostacoli.Ostacoli;
import ostacoli.Ostacolo;
import stelle.Raccolta;
import stelle.Stella;

/**
 * Una partita a un livello: voi a sinistra, il giocatore automatico a destra, due racchette,
 * una palla, gli ostacoli del livello e i conti di chi vince.
 */
class Partita {
    /** Si gioca a 5, con almeno due punti di scarto (5 a 4 non basta, 6 a 4 sì). */
    static final int PUNTI_PER_VINCERE = 5;
    static final int SCARTO_MINIMO = 2;

    private final Campo campo;
    private final Giocatore giocatoreSinistra;
    private Giocatore giocatoreDestra;
    private final Racchetta racchettaSinistra;
    private final Racchetta racchettaDestra;
    private final Palla palla;
    private final Punteggio punteggio = new Punteggio(PUNTI_PER_VINCERE, SCARTO_MINIMO);
    private Livello livello;
    private Ostacoli ostacoli;
    private boolean iniziata;
    private final Livelli livelli;
    private final Raccolta raccolta = new Raccolta(Raccolta.STELLE_PER_UNA_VITA);
    private Stella stella;
    private int fotogrammiAllaStella;
    private final Random caso = new Random();

    /** Una partita fra umano, a sinistra, e il giocatore automatico, a destra; si comincia dal livello 1. */
    Partita(Campo campo, Giocatore umano) {
        this.campo = campo;
        this.giocatoreSinistra = umano;
        double centroY = campo.getAltezza() / 2.0 - Racchetta.ALTEZZA / 2.0;
        this.racchettaSinistra = new Racchetta(30, centroY);
        this.racchettaDestra = new Racchetta(campo.getLarghezza() - 30 - Racchetta.LARGHEZZA, centroY);
        this.palla = new Palla(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        this.livelli = new Livelli(MotoreDeiLivelli.NUMERO_LIVELLI, 3, Lato.SINISTRA);
        preparaLivello(1);
    }

    /**
     * Prepara il livello numero: chiede al motore dei livelli ostacoli nuovi, velocità della palla ed
     * errore del giocatore automatico; rimette i punti a zero e la palla ferma al centro.
     * Pretende: la partita non è in corso; numero è fra 1 e MotoreDeiLivelli.NUMERO_LIVELLI.
     */
    void preparaLivello(int numero) {
        livello = MotoreDeiLivelli.livello(numero, campo.getLarghezza(), campo.getAltezza());
        ostacoli = livello.ostacoli();
        giocatoreDestra = new GiocatoreAutomatico(livello.erroreMassimo());
        punteggio.azzera();
        iniziata = false;
        palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        stella = null;
        programmaStella();
    }

    // Quello che lo Schermo può chiedere: solo leggere. Il Punteggio resta qui dentro.

    /** I punti del lato chi. */
    int getPunti(Lato chi) { return punteggio.getPunti(chi); }

    /** Chi ha vinto; null se la partita non è finita. */
    Lato getVincitore() { return punteggio.getVincitore(); }

    /** Vero se si sta giocando: la partita è cominciata e non è finita. */
    boolean isInCorso() { return iniziata && !punteggio.isFinita(); }

    /** Vero se la partita è cominciata (anche se poi è finita). */
    boolean isIniziata() { return iniziata; }

    Livello getLivello() { return livello; }
    Ostacoli getOstacoli() { return ostacoli; }

    /**
     * Fa cominciare la partita e lancia la palla. Se una partita era già stata giocata, prima
     * prepara di nuovo lo stesso livello (ostacoli nuovi, punti a zero).
     * Pretende: la partita non è in corso.
     */
    void avvia() {
        if (livelli.isFinito()) {
            livelli.ricomincia();
            preparaLivello(livelli.getLivello());
        } else if (iniziata) {
            preparaLivello(livelli.getLivello());
        }
        iniziata = true;
        lanciaPalla(caso.nextBoolean());
    }

    /** Lancia la palla dal centro, alla velocità del livello. */
    private void lanciaPalla(boolean versoDestra) {
        palla.lancia(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0, versoDestra, livello.velocitaPalla());
    }

    /** Un passo del gioco: viene chiamato 60 volte al secondo. */
    void aggiorna() {
        if (!isInCorso()) return;
        ostacoli.avanzaTutti();
        muoviRacchette();
        aggiornaStella();
        palla.muovi();
        controllaRimbalzi();
        controllaOstacoli();
        controllaPunto();
    }

    /** Chiede a ciascun giocatore cosa vuole fare, e lo fa. Non sa chi sono i giocatori. */
    private void muoviRacchette() {
        Mossa mossaSinistra = giocatoreSinistra.decidiMossa(palla, racchettaSinistra);
        Mossa mossaDestra = giocatoreDestra.decidiMossa(palla, racchettaDestra);
        racchettaSinistra.muovi(mossaSinistra, campo);
        racchettaDestra.muovi(mossaDestra, campo);
    }

    private void controllaRimbalzi() {
        double y = palla.getPosizione().y();
        boolean toccaAlto = y - palla.getRaggio() <= 0 && palla.getVelocitaY() < 0;
        boolean toccaBasso = y + palla.getRaggio() >= campo.getAltezza() && palla.getVelocitaY() > 0;
        if (toccaAlto || toccaBasso) palla.rimbalzaVerticale();

        if (palla.tocca(racchettaSinistra) && palla.getVelocitaX() < 0) palla.rimbalzaSu(racchettaSinistra);
        if (palla.tocca(racchettaDestra) && palla.getVelocitaX() > 0) palla.rimbalzaSu(racchettaDestra);
    }

    /**
     * Se la palla tocca un ostacolo non rotto, rimbalza e l'ostacolo viene colpito (un ostacolo per passo).
     * Qui non si sa di che tipo è l'ostacolo: che cosa fa il colpo lo decide l'ostacolo.
     */
    private void controllaOstacoli() {
        Ostacolo colpito = ostacoli.ostacoloCheTocca(palla.getArea());
        if (colpito != null) {
            palla.rimbalzaSu(colpito.getArea());
            colpito.colpisci(ostacoli);
        }
    }

    /** Fa apparire, avanzare e raccogliere una stella nella corsia sinistra. */
    private void aggiornaStella() {
        if (stella == null) {
            fotogrammiAllaStella--;
            if (fotogrammiAllaStella <= 0) {
                double x = racchettaSinistra.getX() + racchettaSinistra.getLarghezza();
                double y = caso.nextDouble() * Math.max(1, campo.getAltezza() - 16);
                Rettangolo area = new Rettangolo(x, y, 16, 16);
                stella = new Stella(area, livello.durataStella());
            }
        } else {
            stella.avanza();
            Rettangolo areaRacchetta = new Rettangolo(
                    racchettaSinistra.getX(), racchettaSinistra.getY(),
                    racchettaSinistra.getLarghezza(), racchettaSinistra.getAltezza());
            if (stella.isVisibile() && areaRacchetta.tocca(stella.getArea())) {
                stella.raccogli();
                if (raccolta.aggiungi()) livelli.guadagnaVita();
                programmaStella();
            } else if (!stella.isVisibile()) {
                stella = null;
                programmaStella();
            }
        }
    }

    private void programmaStella() {
        fotogrammiAllaStella = 120 + caso.nextInt(181);
    }

    /** Se la palla è uscita da un lato, il punto va all'altro lato. */
    private void controllaPunto() {
        double x = palla.getPosizione().x();
        if (x < 0) {
            punteggio.segnaPunto(Lato.DESTRA);
            dopoIlPunto(true);
        } else if (x > campo.getLarghezza()) {
            punteggio.segnaPunto(Lato.SINISTRA);
            dopoIlPunto(false);
        }
    }

    /** Dopo un punto: se la partita non è finita si serve verso chi ha subito il punto, altrimenti la palla si ferma. */
    private void dopoIlPunto(boolean versoSinistra) {
        if (punteggio.isFinita()) {
            palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
            livelli.partitaFinita(punteggio.getVincitore());
        } else {
            lanciaPalla(!versoSinistra);
        }
    }

    Campo getCampo() { return campo; }
    Palla getPalla() { return palla; }
    Racchetta getRacchettaSinistra() { return racchettaSinistra; }
    Racchetta getRacchettaDestra() { return racchettaDestra; }
    Livelli getLivelli() { return livelli; }
    Stella getStella() { return stella; }
    Raccolta getRaccolta() { return raccolta; }
}
