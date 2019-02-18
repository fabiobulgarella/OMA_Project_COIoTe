package com.oma.greedy;

/**
 * Classe Pair: accoppia un indice soluzione al relativo objectiveFunction
 * Utilizzata nella ricerca delle quattro migliori soluzioni generate dalla prima parte dell'algoritmo
 */
class Pair implements Comparable<Pair> {

    int index;
    int objFun;

    Pair(int objFun) {
        this.index = -1;
        this.objFun = objFun;
    }

    @Override
    public int compareTo(Pair other) {
        // ovverride necessario per confrontare elementi della classe Pair in base al valore "objFun"
        return Integer.valueOf(this.objFun).compareTo(other.objFun);
    }
}