package com.oma.greedy;

import java.io.*;
import java.util.Arrays;

/**
 * Carica nele strutture dati generali del problema un'istanza ( istanza = file di input)
 */
class MyInstance {

    /**
     * attributi della classe
     */
    String instanceName;         // Nome dell'istanza
    int nCells;                  // Numero celle presenti nell'istanza
    int nTimeSteps;              // Numero periodi temporali da considerare
    int nCustomerTypes;          // Numero dei diversi tipi di utenti
    int[] tasksPerCustomer;      // Task che ogni tipologia di utenti può soddisfare
    int[][][][] costsMatrix;     // Matrice dei costi (m t i j)
    int[] activities;            // Task da effettuare in ogni cella j
    int[][][] usersCell;         // Numero di utenti presenti nella cella di partenza (m t i)

    // Strutture accessorie
    int jCount;                  // Numero celle j in cui vi sono task da soddisfare
    ActivityCell[] jActive;               // Indici delle celle j in cui vi sono task da soddisfare
    int[][][] iActive;           // Indici delle celle i in cui vi sono utenti di tipo m al tempo t
    float[][][][] normCosts;     // Tabella dei costi normalizzati


    /**
     * Costruttore: inizializza l'oggetto con tutti i dati dell'istanza in analisi.
     * Riceve in input il nome del file da caricare.
     */
    MyInstance(String instanceFile) {

        try {
            // Apro il file e lo preparo per la lettura Line by Line
            File currentFile = new File(instanceFile);
            BufferedReader inputData = new BufferedReader(new FileReader(currentFile));

            // Recupero il nome dell'istanza
            String fileName = currentFile.getName();
            int posExt = fileName.lastIndexOf(".");
            String justName = posExt > 0 ? fileName.substring(0, posExt) : fileName;

            // Leggo le specifiche dell'istanza
            instanceName = justName;
            int[] instanceConfig = Arrays.stream( inputData.readLine().trim().split(" ") ).mapToInt(Integer::parseInt).toArray();
            nCells = instanceConfig[0];
            nTimeSteps = instanceConfig[1];
            nCustomerTypes = instanceConfig[2];
            inputData.readLine();
            tasksPerCustomer = Arrays.stream( inputData.readLine().trim().split(" ") ).mapToInt(Integer::parseInt).toArray();
            inputData.readLine();


            // Inizializzo la struttura per la tabella dei costi
            costsMatrix = new int[nCustomerTypes][nTimeSteps][nCells][nCells];

            // Popolo la tabella dei costi (leggo nTimeSteps*nCustomerTypes matrici
            for(int z = 0; z < nTimeSteps * nCustomerTypes; z++) {

                // Leggo il riferimento PERIODO e TIPO CLIENTE per le matrici da acquisire
                String[] matrixId = inputData.readLine().trim().split(" ");
                int m = Integer.parseInt( matrixId[0] );
                int t = Integer.parseInt( matrixId[1] );

                // Scrivo i costi nell'array multidimensionale
                for(int i = 0; i < nCells; i++) {
                    double[] values = Arrays.stream( inputData.readLine().trim().split(" ") ).mapToDouble(Double::parseDouble).toArray();
                    for(int j = 0; j < values.length; j++)
                        costsMatrix[m][t][i][j] = (int) values[j];
                }
            }

            // Leggo il numero di task da eseguire in ciascuna cella
            inputData.readLine();
            activities = Arrays.stream( inputData.readLine().trim().split(" ") ).mapToInt(Integer::parseInt).toArray();
            inputData.readLine();

            // Inizializzo la struttura per i vettori utenti
            usersCell = new int[nCustomerTypes][nTimeSteps][];

            // Leggo i dati relativi al numero di utenti di tipo m presenti nella cella i al tempo t
            for(int z = 0; z < nTimeSteps * nCustomerTypes; z++) {

                // Leggo il riferimento PERIODO e TIPO CLIENTE per i vettori da acquisire
                String[] vectorId = inputData.readLine().trim().split(" ");
                int m = Integer.parseInt( vectorId[0] );
                int t = Integer.parseInt( vectorId[1] );

                // Scrivo il numero degli utenti per cella nell'array multidimensionale
                usersCell[m][t] = Arrays.stream( inputData.readLine().trim().split(" ") ).mapToInt(Integer::parseInt).toArray();
            }

            // Chiudo il file dell'istanza, lettura completata!
            inputData.close();
        }
        catch(Exception e) {
            System.out.println();
            System.out.println("Errore durante il caricamento del file!");
            System.out.println("Impossibile trovare il file specificato: " + instanceFile);
        }

    }

}
