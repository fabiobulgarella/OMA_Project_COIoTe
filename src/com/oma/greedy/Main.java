package com.oma.greedy;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Verifico che siano stati passati argomenti da linea di comando
        // In caso negativo stampo l'help del programma
        if(args.length == 0) {
            printHelp();
        }
        else {

            // Variabili di settaggio
            boolean verbose = false;
            boolean saveSolution = false;
            List<String> instances = new ArrayList<>();

            // Verifico le opzioni passate da command line
            for(String arg : args) {
                switch (arg) {
                    case "-v":
                        verbose = true;
                        break;
                    case "-s":
                        saveSolution = true;
                        break;
                    case "-vs":
                    case "-sv":
                        verbose = saveSolution = true;
                        break;
                    default:
                        instances.add(arg);
                        break;
                }
            }

            // Verifico che sia stato passato almeno il nome di una istanza
            if(instances.size() == 0) {
                System.out.println();
                System.out.println("Attenzione specificare il nome di almeno un'istanza da risolvere!");
                printHelp();
            }
            else {

                String result = "";

                // Carico la/le istanza/e ed eseguo l'algoritmo risolutivo
                for (String instance : instances) {
                    MyInstance problem = new MyInstance(instance); //caricamento istanza da file a strutture dati contenuta in problem
                    if (problem.instanceName != null)
                        result += MyGreedyAlgorithm.solve(problem, 4, saveSolution, verbose) + System.lineSeparator();
                }

                // Scrivo il file contente il resoconto delle istanze processate
                if (result.length() > 0)
                    Files.write(Paths.get("./Report.csv"), result.getBytes());

            }

        }

    }

    // Metodo per la stampa dell'help
    private static void printHelp() {
        System.out.println("");
        System.out.println("Optimization Methods and Algorithms -> Assignment (Group n. 1)");
        System.out.println("--------------------------------------------------------------");
        System.out.println("Usage: [-options] path_of_the_instance_file [other_instance_file...]");
        System.out.println("Options ->  -v:  print info during execution");
        System.out.println("            -s:  save detailed solution");
        System.out.println("Example: -v -s Co_30_1_NT_0.txt Co_100_20_NT_0.txt");
        System.out.println("--------------------------------------------------------------");
        System.out.println("A Report.csv file containing info about each instance analyzed");
        System.out.println("is saved automatically at the end of every execution.");
        System.out.println("");
    }
}
