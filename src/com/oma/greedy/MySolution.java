package com.oma.greedy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/**
 * Contiene le informazioni relative alla soluzione dell'istanza analizzata
 * (incluse le strutture di supporto necessarie per modificare la stessa)
 */
class MySolution {

    /**
     * Struttura dati della soluzione
     * Utilizzo un'array di List<> ognuna delle quali conterrà gli spostamenti per una determinata cella j attiva
     */
    List<int[]>[] solution; // int[]{ i, j, m, t }
    int[] activities;
    int[][][] usersCell;
    int objFunction;
    boolean complete;

    // Costruttore della classe
    MySolution(List<int[]>[] solution, int[] activities, int[][][] usersCell, int objFunction, boolean complete) {

        this.solution = solution;
        this.activities = activities;
        this.usersCell = usersCell;
        this.objFunction = objFunction;
        this.complete = complete;

    }

    // Costruttore utilizzato dal clone
    MySolution() {};

    // Clona l'intera classe e tutti i suoi attributi (in profondità)
    protected MySolution clone() {

        MySolution copy = new MySolution();

        //2) clona ESPLICITAMENTE le strutture dati ed i parametri uno ad uno, java clona solo l'oggetto non gli attributi!
        copy.solution = (List<int[]>[]) cloneSolutionList(this.solution);
        copy.activities = (int[]) this.activities.clone();
        copy.usersCell = (int[][][]) cloneUsersCell(this.usersCell);
        copy.objFunction = (int) this.objFunction;
        copy.complete = (boolean) this.complete;

        return copy;
    }

    // Clono in profondità l'array di liste solution
    private static List<int[]>[] cloneSolutionList(List<int[]>[] source) {

        List<int[]>[] copy = new List[source.length];
        for(int i = 0; i < source.length; i++) {
            copy[i] = new ArrayList<>(source[i].size());
            for (int[] item : source[i])
                copy[i].add(item.clone());
        }
        return copy;

    }

    // Clono in profondità l'array usersCell
    static int[][][] cloneUsersCell(int[][][] source) {

        int[][][] copy = (int[][][]) source.clone();
        for(int m = 0; m < source.length; m++) {
            copy[m] = (int[][]) source[m].clone();
            for(int t = 0; t < source[m].length; t++)
                copy[m][t] = (int[]) source[m][t].clone();
        }
        return copy;

    }

    // Salva il risultato su un file
    void saveResult(MyInstance problem) throws IOException {

        // Preparo la stringa che conterrà i dati da salvare nel file
        String result = "";

        // Dichiaro l'array multidimensionale per trasferire la soluzione attuale nel formato previsto dal progetto
        int[][][][] solution = new int[problem.nCells][problem.nCells][problem.nCustomerTypes][problem.nTimeSteps];

        // Trasferisco i dati
        for(int i = 0; i < this.solution.length; i++) {
            this.solution[i].forEach( item -> {
                solution[item[0]][item[1]][item[2]][item[3]]++;
            });
        }

        // Genero il testo della soluzione
        result += problem.nCells + "; " + problem.nTimeSteps + "; " + problem.nCustomerTypes + System.lineSeparator();
        for(int i = 0; i < problem.nCells; i++)
            for(int j = 0; j < problem.nCells; j++)
                for(int m = 0; m < problem.nCustomerTypes; m++)
                    for(int t = 0; t < problem.nTimeSteps; t++)
                        if(solution[i][j][m][t] > 0)
                            result += i + ";" + j + ";" + m + ";" + t + ";" + solution[i][j][m][t] + System.lineSeparator();

        // Scrivo il file
        Files.write(Paths.get("./Sol_" + problem.instanceName + ".csv"), result.getBytes());

    }

}
