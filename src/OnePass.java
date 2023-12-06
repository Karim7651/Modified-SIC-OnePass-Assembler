import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OnePass {
    public String inputFile ="src\\input.txt";
    ArrayList<String> input = new ArrayList<>();

    ArrayList<String> locationCounter = new ArrayList<>();
    ArrayList<String> label = new ArrayList<>();
    ArrayList<String> instruction = new ArrayList<>();
    ArrayList<String> reference = new ArrayList<>();
    ArrayList<String> objectCode = new ArrayList<>();

    int numberOfLines;

    HashMap<String, String> instructionFormatOneOPCodeMap = new HashMap<>();
    HashMap<String, String> instructionFormatThreeOPCodeMap = new HashMap<>();
    HashSet <String> allInstructionsSet = new HashSet<>();

    public OnePass(){
        initializeMaps();
        countNumberOfLines();
        prepareArrayLists();
        readFromFile();
    }

    public void prepareArrayLists() {
        for(int i = 0 ; i < numberOfLines ; i++){
            locationCounter.add("");
            label.add("");
            instruction.add("");
            reference.add("");
            objectCode.add("");
        }
    }

    public void readFromFile(){
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int currentIndex = 0;

            while ((line = br.readLine()) != null) {
                // Split the line into words
                String[] words = line.split("\\s+");
                if(words.length ==3){
                        label.set(currentIndex,words[0]);
                        instruction.set(currentIndex,words[1]);
                        reference.set(currentIndex,words[2]);

                }
                if(words.length ==2){
                    instruction.set(currentIndex,words[0]);
                    reference.set(currentIndex,words[1]);
                }
                if(words.length ==1){
                    instruction.set(currentIndex,words[0]);
                }
                currentIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void countNumberOfLines(){
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            int lineCount = 0;
            String line;

            while ((line = br.readLine()) != null) {
                // Increment the counter for each line
                lineCount++;
            }
            numberOfLines=lineCount;

            System.out.println("Number of lines in the file: " + lineCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void initializeMaps(){
        instructionFormatThreeOPCodeMap.put("ADD", "18");
        instructionFormatThreeOPCodeMap.put("AND", "40");
        instructionFormatThreeOPCodeMap.put("COMP", "28");
        instructionFormatThreeOPCodeMap.put("DIV", "24");
        instructionFormatThreeOPCodeMap.put("J", "3C");
        instructionFormatThreeOPCodeMap.put("JEQ", "30");
        instructionFormatThreeOPCodeMap.put("JGT", "34");
        instructionFormatThreeOPCodeMap.put("JLT", "38");
        instructionFormatThreeOPCodeMap.put("JSUB", "48");
        instructionFormatThreeOPCodeMap.put("LDA", "00");
        instructionFormatThreeOPCodeMap.put("LDCH", "50");
        instructionFormatThreeOPCodeMap.put("LDL", "08");
        instructionFormatThreeOPCodeMap.put("LDX", "04");
        instructionFormatThreeOPCodeMap.put("MUL", "20");
        instructionFormatThreeOPCodeMap.put("OR", "44");
        instructionFormatThreeOPCodeMap.put("RD", "D8");
        instructionFormatThreeOPCodeMap.put("RSUB", "4C");
        instructionFormatThreeOPCodeMap.put("STA", "0C");
        instructionFormatThreeOPCodeMap.put("STCH", "54");
        instructionFormatThreeOPCodeMap.put("STL", "14");
        instructionFormatThreeOPCodeMap.put("STSW", "E8");
        instructionFormatThreeOPCodeMap.put("STX", "10");
        instructionFormatThreeOPCodeMap.put("SUB", "1C");
        instructionFormatThreeOPCodeMap.put("TD", "D0");
        instructionFormatThreeOPCodeMap.put("TIX", "2C");
        instructionFormatThreeOPCodeMap.put("WD", "DC");

        instructionFormatOneOPCodeMap.put("FIX", "C4");
        instructionFormatOneOPCodeMap.put("FLOAT", "C0");
        instructionFormatOneOPCodeMap.put("HIO", "F4");
        instructionFormatOneOPCodeMap.put("NORM", "C8");
        instructionFormatOneOPCodeMap.put("SIO", "F0");
        instructionFormatOneOPCodeMap.put("TIO", "F8");

        allInstructionsSet.add("ADD");
        allInstructionsSet.add("AND");
        allInstructionsSet.add("COMP");
        allInstructionsSet.add("DIV");
        allInstructionsSet.add("J");
        allInstructionsSet.add("JEQ");
        allInstructionsSet.add("JGT");
        allInstructionsSet.add("JLT");
        allInstructionsSet.add("JSUB");
        allInstructionsSet.add("LDA");
        allInstructionsSet.add("LDCH");
        allInstructionsSet.add("LDL");
        allInstructionsSet.add("LDX");
        allInstructionsSet.add("MUL");
        allInstructionsSet.add("OR");
        allInstructionsSet.add("RD");
        allInstructionsSet.add("RSUB");
        allInstructionsSet.add("STA");
        allInstructionsSet.add("STCH");
        allInstructionsSet.add("STL");
        allInstructionsSet.add("STSW");
        allInstructionsSet.add("STX");
        allInstructionsSet.add("SUB");
        allInstructionsSet.add("TD");
        allInstructionsSet.add("TIX");
        allInstructionsSet.add("WD");
        allInstructionsSet.add("FIX");
        allInstructionsSet.add("FLOAT");
        allInstructionsSet.add("HIO");
        allInstructionsSet.add("NORM");
        allInstructionsSet.add("SIO");
        allInstructionsSet.add("TIO");
    }

    public void printArrayLists(){
        System.out.println(label);
        System.out.println(instruction);
        System.out.println(reference);
    }
}
