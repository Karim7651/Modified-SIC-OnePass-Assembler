import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OnePass {
    public String inputFile = "src\\input.txt";
    ArrayList<String> input = new ArrayList<>();

    ArrayList<String> locationCounter = new ArrayList<>();
    ArrayList<String> label = new ArrayList<>();
    ArrayList<String> instruction = new ArrayList<>();
    ArrayList<String> reference = new ArrayList<>();
    ArrayList<String> objectCode = new ArrayList<>();

    int numberOfLines;
    String currentLocationCounter;
    int instructionsIndexStart;

    HashMap<String, String> instructionFormatOneOPCodeMap = new HashMap<>();
    HashMap<String, String> instructionFormatThreeOPCodeMap = new HashMap<>();
    HashMap<Character, String> asciiHexMap = new HashMap<>();
    HashSet<String> allInstructionsSet = new HashSet<>();

    public OnePass() {
        initializeMaps();
        countNumberOfLines();
        prepareArrayLists();
        readFromFile();
        calculateLocationCounterAndObjectCodeForVariables();
    }

    public void calculateLocationCounterAndObjectCodeForVariables() {
        currentLocationCounter = reference.get(0);
        int lastIndex = calcIndexOfLastVariable();
        instructionsIndexStart = 1;
        locationCounter.set(0, currentLocationCounter);
        locationCounter.set(1, currentLocationCounter);
        for (int i = 1; i <= lastIndex; i++) {
            if (instruction.get(i).equals("WORD")) {
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                locationCounter.set(i + 1, currentLocationCounter);
                objectCode.set(i, makeObjectCodeSixHexadecimal(decimalToHexadecimal(Integer.parseInt(reference.get(i)))));
            } else if (instruction.get(i).equals("BYTE")) {
                String byteValue = reference.get(i);
                byteValue = cutFirstTwoCharacters(byteValue);
                byteValue = removeLastCharacter(byteValue);
                int valueToAdd = byteValue.length() ;
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, decimalToHexadecimal(valueToAdd));
                locationCounter.set(i + 1, currentLocationCounter);
                objectCode.set(i,getByteObjectCode(byteValue));


            } else if (instruction.get(i).equals("RESW")) {
                String value = reference.get(i);
                value = multiplyTwoHexadecimal(value, "3");
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, value);
                locationCounter.set(i + 1, currentLocationCounter);


            } else if (instruction.get(i).equals("RESB")) {
                String value = reference.get(i);
                String valueToAdd = decimalToHexadecimal(Integer.parseInt(value));
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, valueToAdd);
                locationCounter.set(i + 1, currentLocationCounter);
            }
            instructionsIndexStart++;
        }
        System.out.println(instructionsIndexStart);
    }

    public void prepareArrayLists() {
        for (int i = 0; i < numberOfLines; i++) {
            locationCounter.add("");
            label.add("");
            instruction.add("");
            reference.add("");
            objectCode.add("");
        }
    }

    public void readFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int currentIndex = 0;

            while ((line = br.readLine()) != null) {
                // Split the line into words
                String[] words = line.split("\\s+");
                if (words.length == 3) {
                    label.set(currentIndex, words[0]);
                    instruction.set(currentIndex, words[1]);
                    reference.set(currentIndex, words[2]);

                }
                if (words.length == 2) {
                    instruction.set(currentIndex, words[0]);
                    reference.set(currentIndex, words[1]);
                }
                if (words.length == 1) {
                    instruction.set(currentIndex, words[0]);
                }
                currentIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void countNumberOfLines() {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            int lineCount = 0;
            String line;

            while ((line = br.readLine()) != null) {
                // Increment the counter for each line
                lineCount++;
            }
            numberOfLines = lineCount;

            System.out.println("Number of lines in the file: " + lineCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initializeMaps() {
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

        for (int i = 0; i <= 127; i++) {
            String hexValue = Integer.toHexString(i).toUpperCase();
            char asciiChar = (char) i;
            asciiHexMap.put(asciiChar, hexValue);
        }
    }

    public int calcIndexOfLastVariable() {
        for (int i = 1; i < instruction.size(); i++) {
            String current = instruction.get(i);
            if (!current.equals("WORD") && !current.equals("BYTE") && !current.equals("RESW") && !current.equals("RESB")) {
                return i - 1;
            }
        }
        return -1;
    }

    public void printArrayLists() {
        System.out.println(locationCounter);
        System.out.println(label);
        System.out.println(instruction);
        System.out.println(reference);
        System.out.println(objectCode);
    }

    public String addTwoHexadecimal(String input1, String input2) {
        int decimal1 = hexadecimalToDecimal(input1);
        int decimal2 = hexadecimalToDecimal(input2);
        int sum = decimal1 + decimal2;
        String sumString = decimalToHexadecimal(sum);
        StringBuilder stringBuilder = new StringBuilder();
        //make it 4 characters
        if (sumString.length() % 4 == 1) {
            stringBuilder.append("000");
        } else if (sumString.length() == 2) {
            stringBuilder.append("00");
        } else if (sumString.length() == 3) {
            stringBuilder.append("0");
        }
        stringBuilder.append(sumString);

        return stringBuilder.toString();
    }

    public int hexadecimalToDecimal(String input) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("A", 10);
        map.put("B", 11);
        map.put("C", 12);
        map.put("D", 13);
        map.put("E", 14);
        map.put("F", 15);
        map.put("a", 10);
        map.put("b", 11);
        map.put("c", 12);
        map.put("d", 13);
        map.put("e", 14);
        map.put("f", 15);
        int exponent = input.length() - 1;
        int coefficient;
        int result = 0;
        for (int i = 0; i < input.length(); i++) {
            String current = String.valueOf(input.charAt(i));
            if (current.equals("a") || current.equals("b") || current.equals("c") || current.equals("d") || current.equals("e") || current.equals("f") || current.equals("A") || current.equals("B") || current.equals("C") || current.equals("D") || current.equals("E") || current.equals("F")) {
                coefficient = map.get(current);
            } else {
                coefficient = Integer.parseInt(current);
            }
            result += coefficient * Math.pow(16, exponent);
            exponent--;
        }
        return result;
    }

    public String decimalToHexadecimal(int input) {
        return Integer.toHexString(input);
    }

    public String cutFirstTwoCharacters(String input) {
        //substring doesn't modify the original string it returns new a string
        if (input.length() <= 2)
            return "";
        return input.substring(2);
    }

    public String multiplyTwoHexadecimal(String input1, String input2) {
        int decNum1 = Integer.parseInt(input1, 16);
        int decNum2 = Integer.parseInt(input2, 16);

        int resultDecimal = decNum1 * decNum2;

        String resultHex = Integer.toHexString(resultDecimal).toUpperCase();

        return resultHex;
    }

    public String makeObjectCodeSixHexadecimal(String input) {
        if (input.length() >= 6) {
            return input.toUpperCase();
        }

        // Calculate the number of zeros to append
        int zerosToAppend = 6 - input.length();

        // Append zeros to the left
        StringBuilder objectCodeBuilder = new StringBuilder();
        for (int i = 0; i < zerosToAppend; i++) {
            objectCodeBuilder.append('0');
        }
        objectCodeBuilder.append(input.toUpperCase());

        return objectCodeBuilder.toString();
    }

    public String getByteObjectCode(String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            String value = asciiHexMap.get(current);
            stringBuilder.append(value);
        }
        return stringBuilder.toString();
    }

    public  String removeLastCharacter(String str) {
        if (str != null && str.length() > 0) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }


}
