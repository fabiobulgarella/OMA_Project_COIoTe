package com.oma.greedy;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Classe utilizzata per tenere traccia delle celle j in cui vi sono task da soddisfare e della relativa "lista soluzione"
 * Accoppia di fatto un indice cella j in cui vi sono task al rispettivo indice della "lista soluzione"
 */
class ActivityCell {

    /**
     * Indici di ricerca: index -> indice relativo all'array di ricerca
     *                    jIndex -> indice reale della cella j
     */
    final int index;
    final int jIndex;

    // Costruttore della classe
    ActivityCell(int index, int jIndex) {
        this.index = index;
        this.jIndex = jIndex;
    }

    // Randomizza l'array delle attività
    static void randomize(ActivityCell[] source){

        for (int i = 0; i < source.length; i++) {
            int randomPosition = ThreadLocalRandom.current().nextInt(source.length);
            ActivityCell temp = source[i];
            source[i] = source[randomPosition];
            source[randomPosition] = temp;
        }
    }

}
