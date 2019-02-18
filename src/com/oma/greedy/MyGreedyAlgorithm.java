package com.oma.greedy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

class MyGreedyAlgorithm {

    /**
     * Risolve l'instanza "problem" applicando l'intero algoritmo greedy
     */
    static String solve(MyInstance problem, int numThread, boolean saveSolution, boolean verbose) throws InterruptedException, IOException {

        // Dichiaro e inizializzo la lista delle soluzioni generata dalla prima parte dell'algoritmo (lista di MySolution)
        List<MySolution> solList = new ArrayList<>(500000);

        // Creo gli oggetti "task" e "thread" per il Multi-Threading
        Runnable[] task = new Runnable[numThread];
        Thread[] thread = new Thread[numThread];

        // Avvio il timer
        long startTime = System.currentTimeMillis();

        // Calcolo le strutture di supporto
        utilsGenerator(problem);

        // PRIMA PARTE DELL'ALGORITMO
        // Inizializzo i task per l'esecuzione in parallelo della prima parte dell'algoritmo
        // Risolve l'istanza ripetutamente nella ricerca di una buona soluzione sulla quale applicare la fase successiva
        for(int i = 0; i < numThread / 2; i++) {
            task[i] = () -> {
                while( System.currentTimeMillis() - startTime < 1500 )
                    solList.add( greedySolver(problem) );
            };
        }
        for(int i = numThread / 2; i < numThread; i++) {
            task[i] = () -> {
                while( System.currentTimeMillis() - startTime < 1500 )
                    solList.add( greedySolver(problem, 1) );
            };
        }

        // Inizializzo i thread
        for(int i = 0; i < numThread; i++)
            thread[i] = new Thread(task[i]);

        // Avvio i thread
        for(int i = 0; i < numThread; i++) {
            thread[i].start();
        }

        // Eseguo il join dei thread
        for(int i = 0; i < numThread; i++) {
            thread[i].join();
        }

        // Cerco le 4 soluzioni migliori tra quelle generate
        int[] bestIndexes = getFourLowest(solList);
        final MySolution[] bestSolution = new MySolution[4];
        for(int i = 0; i < 4; i++)
            bestSolution[i] = solList.get(bestIndexes[i]);

        // SECONDA PARTE DELL'ALGORITMO
        // Calcolo il numero di iterazioni de eseguire prima di aumentare la profondità di ricerca (in base ad un stima empirica basata sul numero di celle e di frame temporali)
        final int incrementAt;
        int testValue = problem.nCells * problem.nTimeSteps;

        if(testValue <= 100)
            incrementAt = 300;
        else if(testValue <= 600)
            incrementAt = 1800;
        else if(testValue <= 2000)
            incrementAt = 5000;
        else
            incrementAt = 500;

        // Inizializzo i task per l'esecuzione in parallelo della seconda parte dell'algoritmo
        for(int i = 0; i < numThread; i++) {

            int index = i;

            task[i] = () -> {

                // Variabili di supporto
                int count = 0;
                int moduleState = 0;
                int depth = 2;
                boolean solutionState = bestSolution[index].complete;

                // Eseguo la ricerca di soluzioni migliori sino a scadenza dei 5 secondi
                while( System.currentTimeMillis() - startTime < 5000 ) {

                    // Genero una nuova soluzione a partire dalla migliore
                    MySolution tempSol = greedySolver(problem, 0, bestSolution[index], depth);

                    // Verifico se la nuova soluzione generata è migliorativa (o completa nel caso di attuale soluzione incompleta)
                    if(!solutionState && tempSol.complete) {
                        bestSolution[index] = tempSol;
                        solutionState = true;
                    }
                    else if(tempSol.complete && tempSol.objFunction < bestSolution[index].objFunction) {
                        bestSolution[index] = tempSol;
                        depth = 2;
                    }

                    // Incremento la profondità di ricerca in base alla variabile "incrementAt" calcolata precedentemente
                    if(++count / incrementAt > moduleState) {
                        moduleState++;
                        depth++;
                    }

                }

            };
        }

        // Inizializzo i thread
        for(int i = 0; i < numThread; i++)
            thread[i] = new Thread(task[i]);

        // Avvio i thread
        for(int i = 0; i < numThread; i++) {
            thread[i].start();
        }

        // Eseguo il join dei thread
        for(int i = 0; i < numThread; i++) {
            thread[i].join();
        }

        // Stoppo il timer
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        // Recupero la soluzione migliore generata
        int bestObjFunction = 1000000;
        MySolution finalSolution = new MySolution();

        for (MySolution item : bestSolution) {
            int thisObjFunction = item.objFunction;
            if (thisObjFunction < bestObjFunction) {
                bestObjFunction = thisObjFunction;
                finalSolution = item;
            }
        }

        // Genero la stringa resoconto
        String result = problem.instanceName + ";" + elapsedTime + ";" + finalSolution.objFunction + (finalSolution.complete ? ";Completa" : ";Incompleta");

        // Salvo la soluzione su file se richiesto
        if(saveSolution)
            finalSolution.saveResult(problem);

        // Stampo il reconto su terminale se richiesto
        if(verbose)
            verbose(problem, finalSolution, elapsedTime);

        // Ritorno la stringa contenente il resoconto della soluzione generata
        return result;

    }

    // Metodo per l'applicazione dell'algoritmo di ricerca
    private static MySolution greedySolver(MyInstance problem, int mode, MySolution bestSolution, int depth) {

        // Variabili di supporto
        MySolution mySol;
        ActivityCell[] jActive = problem.jActive.clone();

        // Randomizzo l'array delle attività in modo da generare soluzione sempre diverse (o quasi)
        ActivityCell.randomize(jActive);

        // Applico il Greedy da zero, genero quindi una nuova soluzione
        if(bestSolution == null) {

            // Dichiaro e inizializzo la soluzione (lista di MySolution)
            mySol = new MySolution(
                    new List[problem.jCount],
                    problem.activities.clone(),
                    MySolution.cloneUsersCell(problem.usersCell),
                    0,
                    true
            );

            // Inizializzo l'array delle liste solution
            for(int i = 0; i < mySol.solution.length; i++)
                mySol.solution[i] = new ArrayList<>();

        }

        // Applico il Greedy sulla copia della migliore soluzione nella quale elimino alcuni valori in base alla profondità (parametro "depth")
        else {

            // Clono la migliore soluzione attuale in modo da lavorare su di una copia
            mySol = bestSolution.clone();

            // Imposto la soluzione come completa (utile nel caso in cui, partendo da una soluzione incompleta, cerco di generarne una completa facendo gli swap)
            mySol.complete = true;

            // Elimino alcune soluzioni dalle liste delle soluzioni delle celle j (ed aggiorno le strutture di supporto)
            for(int i = 0; i < mySol.solution.length; i++) {

                for(int d = 1; d <= depth; d++) {
                    int lastIndex = mySol.solution[i].size() - 1;
                    if(lastIndex > 0) {
                        // del[0] -> i, del[1] -> j, del[2] -> m, del[3] -> t
                        int[] del = mySol.solution[i].get(lastIndex);
                        mySol.activities[del[1]] += problem.tasksPerCustomer[del[2]];
                        mySol.usersCell[del[2]][del[3]][del[0]]++;
                        mySol.objFunction -= problem.costsMatrix[del[2]][del[3]][del[0]][del[1]];
                        mySol.solution[i].remove(lastIndex);
                    }
                }

            }

        }

        // Comincio ad iterare per generare o completare la soluzione sorgente
        // Ad ogni esecuzione, l'array delle celle attive "ActivityCell" viene randomizzato andando a variare quindi la soluzione finale
        for (ActivityCell j : jActive) {

            // Imposto lo spreco a 0: inizialmente l'algoritmo cercherà di non sprecare;
            // se ciò non fosse possibile, comincerà a sprecare nel tentativo di convergere ad una soluzione completa
            int maxWaste = 0;

            // Eseguo sino a quando soddisfo i task della cella j
            while (mySol.activities[j.jIndex] > 0) {

                // Variabili per la ricerca dell'utente dal costo minimo
                float min = 100000;
                int cM = 0;
                int cT = 0;
                int cI = 0;
                int cJ = j.jIndex;

                // Ricerco l'utente dal costo minore per la cella j
                // mode = 0 -> trovo un nuovo minimo se MINORE o UGUALE dell'attuale
                // mode = 1 -> trovo un nuovo minimo solo se MINORE dell'attuale
                // ciò garantisce l'accesso ad uno spazio più ampio del dominio delle soluzioni
                if (mode == 0) {
                    for (int m = 0; m < problem.nCustomerTypes; m++) {
                        for (int t = 0; t < problem.nTimeSteps; t++) {
                            for (int i : problem.iActive[m][t]) {

                                // Verifico se il costo in esame e minore dell'attuale, in caso positivo verifico i VINCOLI D'INTEGRITA'
                                if (problem.normCosts[m][t][i][j.jIndex] <= min && i != j.jIndex && mySol.usersCell[m][t][i] > 0 && (mySol.activities[j.jIndex] - problem.tasksPerCustomer[m] >= maxWaste)) {
                                    min = problem.normCosts[m][t][i][j.jIndex];
                                    cM = m;
                                    cT = t;
                                    cI = i;
                                    cJ = j.jIndex;
                                }

                            }
                        }
                    }
                }
                else {
                    for (int m = 0; m < problem.nCustomerTypes; m++) {
                        for (int t = 0; t < problem.nTimeSteps; t++) {
                            for (int i : problem.iActive[m][t]) {

                                // Verifico se il costo in esame e minore dell'attuale, in caso positivo verifico i VINCOLI D'INTEGRITA'
                                if (problem.normCosts[m][t][i][j.jIndex] < min && i != j.jIndex && mySol.usersCell[m][t][i] > 0 && (mySol.activities[j.jIndex] - problem.tasksPerCustomer[m] >= maxWaste)) {
                                    min = problem.normCosts[m][t][i][j.jIndex];
                                    cM = m;
                                    cT = t;
                                    cI = i;
                                    cJ = j.jIndex;
                                }

                            }
                        }
                    }
                }

                // Se trovo un utente applicabile, aggiorno la soluzione e le strutture accessorie
                if (min != 100000) {
                    mySol.activities[cJ] -= problem.tasksPerCustomer[cM];
                    mySol.solution[j.index].add(new int[]{cI, cJ, cM, cT});
                    mySol.usersCell[cM][cT][cI]--;
                    mySol.objFunction += problem.costsMatrix[cM][cT][cI][cJ];
                }

                // Incremento la variabile di spreco nel caso in cui non dovessi trovare un utente che svolge esattamente i task rimanenti nella cella j
                else if (maxWaste > -problem.tasksPerCustomer[0])
                    maxWaste--;

                    // Interrompo la ricerca ed imposto la soluzione come incompleta (se neanche sprecando riesco a trovare utenti applicabili)
                else {
                    mySol.complete = false;
                    break;
                }
            }
        }

        // Ritorno la soluzione generata
        return mySol;

    }

    // Overload GreedySolver
    private static MySolution greedySolver(MyInstance problem) {
        return greedySolver(problem, 0, null, 0);
    }
    private static MySolution greedySolver(MyInstance problem, int mode) {
        return greedySolver(problem, mode, null, 0);
    }

    // Metodo per la generazione delle strutture dati di supporto (jCount, jActive, iActive, normCosts)
    private static void utilsGenerator(MyInstance problem) {

        // Estraggo gli indici delle celle j con task da soddisfare
        int jCount = 0;
        for(int j = 0; j < problem.nCells; j++) {
            if(problem.activities[j] > 0)
                jCount++;
        }

        int index = 0;
        ActivityCell[] jActive = new ActivityCell[jCount];
        for(int j = 0; j < problem.nCells; j++) {
            if(problem.activities[j] > 0) {
                jActive[index] = new ActivityCell(index, j);
                index++;
            }
        }

        // Estraggo gli indici delle celle i in cui vi sono utenti di tipo m al tempo t
        int[][] iCount = new int[problem.nCustomerTypes][problem.nTimeSteps];
        for(int m = 0; m < problem.nCustomerTypes; m++)
            for(int t = 0; t < problem.nTimeSteps; t++)
                for(int i = 0; i < problem.nCells; i++)
                    if(problem.usersCell[m][t][i] > 0)
                        iCount[m][t]++;

        int[][][] iActive = new int[problem.nCustomerTypes][problem.nTimeSteps][];
        for(int m = 0; m < problem.nCustomerTypes; m++) {
            for(int t = 0; t < problem.nTimeSteps; t++) {
                index = 0;
                iActive[m][t] = new int[ iCount[m][t] ];
                for(int i = 0; i < problem.nCells; i++) {
                    if(problem.usersCell[m][t][i] > 0)
                        iActive[m][t][index++] = i;
                }
            }
        }

        // Calcolo la tabella dei costi normalizzati
        float[][][][] normCosts = new float[problem.nCustomerTypes][problem.nTimeSteps][problem.nCells][problem.nCells];

        for (ActivityCell j:jActive) {
            for(int m = 0; m < problem.nCustomerTypes; m++) {
                for(int t = 0; t < problem.nTimeSteps; t++) {
                    for(int i:iActive[m][t]) {

                        // Calcolo il minimo normalizzato per lo spostamento in esame
                        normCosts[m][t][i][j.jIndex] = problem.costsMatrix[m][t][i][j.jIndex] / (float) problem.tasksPerCustomer[m];

                    }
                }
            }
        }

        // Memorizzo i valori jCount, jActive, iActive e normCosts nell'istanza "problem" in modo da velocizzare le varie fasi di ricerca previste dall'algoritmo
        problem.jCount = jCount;
        problem.jActive = jActive;
        problem.iActive = iActive;
        problem.normCosts = normCosts;

    }

    // Metodo per la stampa su terminale del resoconto a run-time
    private static void verbose(MyInstance problem, MySolution solution, long elapsedTime) {
        System.out.println();
        System.out.println("Nome istanza:  " + problem.instanceName);
        System.out.println("Soluzione Greedy elaborata in " + elapsedTime + "ms!");
        System.out.println("Il valore finale dell'Objective Function è: " + solution.objFunction + (solution.complete ? " - Completa" : " - Incompleta"));
        System.out.println();
    }

    /**
     * Idea di fondo: invece di provare ad ottimizzare la soluzione migliore generata dalla prima fase dell'algoritmo,
     *                estraggo le quattro soluzioni migliori generate e lavoro random su di esse in modo da stabilizzare
     *                le soluzioni finali della seconda parte dell'algoritmo
     */
    private static int[] getFourLowest(List<MySolution> solList) {

        Pair[] lowestObject = new Pair[4];
        for(int i = 0; i < 4; i++) {
            lowestObject[i] = new Pair(Integer.MAX_VALUE);
        }

        for(int i = 0; i < solList.size(); i++) {
            int objFunction = solList.get(i).objFunction;
            if(objFunction < lowestObject[3].objFun) {
                lowestObject[3].objFun = objFunction;
                lowestObject[3].index = i;
                Arrays.sort(lowestObject);
            }
        }
        return new int[]{ lowestObject[0].index, lowestObject[1].index, lowestObject[2].index, lowestObject[3].index };
    }

}
