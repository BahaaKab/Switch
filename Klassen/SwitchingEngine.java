package programmieraufgaben;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Eine vereinfachte Switch Engine mit folgenden möglichen Kommandos:
 * frame <Eingangsportnummer> <Absenderadresse> <Zieladresse>,
 * table,
 * statistics,
 * del Xs bzw. del Xmin,
 * exit
 */
public class SwitchingEngine {

    private ArrayList<String[]> table;
    private int[][] statistics;
    int portNumber;

    /**
     * Diese Methode überprüft die Eingabe und erstellt die für den
     * weiteren Funktionsablauf nötige Datenstruktur
     * @param portNumber Anzahl der Ports, die der Switch verwalten soll
     * @return Gibt bei erfolgreicher erstellung TRUE sonst FALSE zurück
     */
    public boolean createSwitch(int portNumber) {
        if(portNumber>0) {
            this.portNumber = portNumber;
            System.out.println("\nEin " + portNumber + "-Port-Switch wurde erzeugt.\n");


            table = new ArrayList<>();
            statistics = new int[portNumber][2];
            //Initialisierung der Statistics Tabelle.
            for(int i=0; i<portNumber; i++) {
                statistics[i][0] = i + 1;
                statistics[i][1] = 0;
            }

            return true;
        }

        System.out.println("FEHLER: Bitte geben sie eine Natürliche Zahl > 0 an.");
        return false;
    }

    /**
     * Diese Methode überprüft und interpretiert die Eingaben und führt
     * die geforderten Funktionen aus.
     * @param command Anweisung die der Switch verarbeiten soll
     * @return Gibt an ob der Switch beendet werden soll: TRUE beenden, FALSE weitermachen
     */
    public boolean handleCommand(String command) {

        if(command.equals("table")) {
            if(table.isEmpty()) {
                System.out.println("Die Switch-Tabelle ist leer.");
            }else {
                printTable();
            }
        }else if(command.equals("statistics")) {
            printStatistics();
        }else if(command.matches("frame\\s[0-9]+\\s[0-9]+\\s[0-9]+")) {
            /*
            Die Eingabe wird gesplittert und in einem Array gespeichert, jedes Element wird dann in einem Int Variable
            gespeichert um die frameFunction darauf anzuwenden.
             */
            String[] userInput = command.split(" ");
            int portNum = Integer.parseInt(userInput[1]);
            int senderAddress = Integer.parseInt(userInput[2]);
            int receiverAddress = Integer.parseInt(userInput[3]);
            //Pruefen der Korrektheit des Definitionsbereiches.
            if(portNum >= 1 && portNum <= portNumber && senderAddress >= 1 && senderAddress <= 254 && receiverAddress >= 1 && receiverAddress <= 255) {
                frameFunction(portNum, senderAddress, receiverAddress);
            }else {
                System.out.println("Ungültiger Definitionsbereich!");
            }

        }else if(command.matches("del\\s[0-9]+s") || command.matches("del\\s[0-9]+min")){
            String[] time = command.split(" ");
            /*
            Die Zeit muss in Millisekunden sein, und wird mit der if-else Statement entsprechend umgewandelt.
            Danach wird die delFunction darauf angewendet.
             */
            long milliseconds;
            if(time[1].endsWith("s")) {
                milliseconds = TimeUnit.SECONDS.toMillis(Integer.parseInt(time[1].replace("s",""))); //must be in milliseconds
            }else {
                milliseconds = TimeUnit.MINUTES.toMillis(Integer.parseInt(time[1].replace("min",""))); //must be in milliseconds
            }
            delFunction(milliseconds);
        }else if(command.equals("exit")){
            return true;
        }else {
            System.out.println("Ungültige Eingabe!");
        }

        return false;
    }

    /**
     * In diese Methode wird die Switch-Funktionalitaet realisiert.
     * @param portNum Eingangsportnummer.
     * @param senderAddress Die Addresse des Absenders.
     * @param receiverAddress Die Zieladdresse.
     */
    public void frameFunction(int portNum, int senderAddress, int receiverAddress) {
        boolean check = false;
        int port = 0;
        //hier wird gecheckt, ob es ein Eintrag in der Tabelle gibt, dessen Address gleich der ReceiverAddress ist.
        for (String[] strings : table) {
            if (strings[0].equals(Integer.toString(receiverAddress))) {
                port = Integer.parseInt(strings[1]);
                check = true;
                break;
            }
        }

        if(check && (receiverAddress != 255)) { //gibt es einen Eintrag.
            //fall 1: der obengennante Eintrag existiert in der Tabelle, und der Eingagns- und Ausgangsport sind unterschiedlich.
            if(portNum != port) {
                // Addresse, Portnummer und die aktualisierte Zeit werden in der Tabelle eingetragen, nachdem der alte Eintrag geloescht wird.
                insertIntoTable(senderAddress, portNum);
                //Aktualisierung der Statistic Tabelle.
                for(int i=0; i<portNumber; i++) {
                    if(statistics[i][0] == port || statistics[i][0] == portNum) {
                        statistics[i][1] = statistics[i][1] + 1;
                    }
                }
                System.out.println("Ausgabe auf port " + port +".");
            //fall 2: Eingangsport und Ausgangsport sind identisch.
            }else {
                insertIntoTable(senderAddress, portNum);
                //Aktualisierung der Statistic Tabelle.
                for(int i=0; i<portNumber; i++) {
                    if(statistics[i][0] == port || statistics[i][0] == portNum) {
                        statistics[i][1] = statistics[i][1] + 1;
                    }
                }
                System.out.println("Frame wird gefiltert und verworfen.");
            }
        // der obengennante Eintrag existiert nicht in der Tabelle.
        }else {
            //Broadcast Fall.
            if(receiverAddress == 255) {
                System.out.println("Broadcast: Ausgabe auf allen Ports außer Port " + portNum +".");
            }else {
                System.out.println("Ausgabe auf allen Ports außer Port " + portNum +".");
            }
            //Neuer Eintrag wird in die Tabelle eingetragen.
            insertIntoTable(senderAddress, portNum);
            //Aktualisierung der Statistic Tabelle.
            for(int i=0; i<portNumber; i++) {
                statistics[i][1] = statistics[i][1] + 1;
            }
        }

    }

    /**
     * Loescht alle Switch-Tabelleneintraege, die aelter als eine angegebene Zeit sind.
     * @param milliseconds Die gegebene Zeit in millisekunden.
     */
    public void delFunction(long milliseconds) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String address = "";
        String time = timeFormat.format(new Date());
        Date actualTime = null;
        try {
            actualTime = timeFormat.parse(time);
        } catch (ParseException e1) {
        }
        for(int i=0; i<table.size(); i++) {
            try {
                //Die frameZeit wird von der aktuellen Zeit subtrahiert, und geprueft ob es groesser als die als Parameter gegebene Zeit ist.
                Date frameTime = timeFormat.parse(table.get(i)[2]);
                if((actualTime.getTime() - frameTime.getTime()) > milliseconds) {
                    if(!address.isEmpty()) {
                        address = address + ", " + table.get(i)[0];
                    }else {
                        address =table.get(i)[0];
                    }
                    table.remove(i);
                    i--;

                }
            } catch (ParseException e) {
            }
        }
        //gibt die Adressen aus, die aus der Switch-Tabelle geloescht werden.
        if(!address.isEmpty()) {
            System.out.println("Folgende Adressen wurden aus der Switch-Tabelle gelöscht: " + address);
        }else {
            System.out.println("Keine Adressen wurden gelöscht.");
        }
    }

    /**
     * Diese Methode sortiert die Tabelle aufsteigend, mit der BubbleSort Algorithmus.
     */
    public void sortTable() {
        String[] temp;
        for (int j=0; j < table.size()-1; j++) {
            for(int i=j+1; i< table.size(); i++) {
                if(Integer.parseInt(table.get(i)[0]) < Integer.parseInt(table.get(j)[0])) {
                    temp = table.get(j);
                    table.set(j, table.get(i));
                    table.set(i, temp);
                }
            }
        }
    }

    /**
     * Diese Methode gibt die Switch-Tabelle aus.
     */
    public void printTable() {
        sortTable();
        System.out.println("Adresse\tPort\tZeit");
        for (String[] strings : table) {
            System.out.println(strings[0] + "\t" + strings[1] + "\t" + strings[2]);
        }

    }

    /**
     * Diese Methode gibt die Statistics-Tabelle aus.
     */
    public void printStatistics() {
        System.out.println("Port\tFrames");
        for(int i=0; i<portNumber; i++) {
            System.out.println(statistics[i][0] +"\t" + statistics[i][1]);
        }
    }

    /**
     * In diese Methode werden die Senderaddresse, die Portnummer und die aktuelle Zeit in einem Array gespeichert,
     * und dann in der Tabelle hinzugefuegt.
     * @param senderAddress Die addresse des Absenders.
     * @param portNumber Die Portnummer.
     */
    public void insertIntoTable(int senderAddress, int portNumber){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String[] input = new String[3];
        input[0] = Integer.toString(senderAddress);
        input[1] = Integer.toString(portNumber);
        String time = timeFormat.format(new Date());
        input[2] = time;
        for(int i=0; i<table.size(); i++) {
            if(table.get(i)[0].equals(input[0])) {
                table.remove(i);
                break;
            }
        }
        table.add(input);
    }
}
