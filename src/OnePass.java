import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OnePass {
    public String inputFile = "src\\input.txt";
    public String symbolTableFile = "src\\symbolTable.txt";
    public String output = "src\\output.txt";
    public String hte = "src\\hte.txt";
    ArrayList<String> input = new ArrayList<>();

    ArrayList<String> locationCounter = new ArrayList<>();
    ArrayList<String> label = new ArrayList<>();
    ArrayList<String> instruction = new ArrayList<>();
    ArrayList<String> reference = new ArrayList<>();
    ArrayList<String> objectCode = new ArrayList<>();

    ArrayList<ArrayList<String>> tRecords = new ArrayList<>();

    int numberOfLines;
    String currentLocationCounter;
    int instructionsIndexStart;

    HashMap<String, String> instructionFormatOneOPCodeMap = new HashMap<>();
    HashMap<String, String> instructionFormatThreeOPCodeMap = new HashMap<>();
    HashMap<Character, String> asciiHexMap = new HashMap<>();
    HashSet<String> allInstructionsSet = new HashSet<>();

    //variable addresses
    HashMap<String, String> references = new HashMap<>(); //<Name,Address>
    //<labelName,lastReferenceToIt>


    public OnePass() {
        initializeMaps();
        countNumberOfLines();
        prepareArrayLists();
        readFromFile();
        calculateLocationCounterAndObjectCodeForVariables();
        calculateLocationCounterAndObjectCodeForInstructions();

        generateSymbolTable();

    }

    public void generateSymbolTable() {
        try (FileWriter writer = new FileWriter(symbolTableFile)) {
            for (int i = 1; i < label.size() - 1; i++) {
                String symbol = label.get(i);
                String address = locationCounter.get(i);
                if (!symbol.equals("")) {
                    writer.write(symbol + "\t");
                    writer.write(address);
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("problem writing symbolTable");
        }

    }

    public void calculateLocationCounterAndObjectCodeForInstructions() {
        int currentInstructionIndex = instructionsIndexStart;
        for (int i = instructionsIndexStart; i < instruction.size() - 1; i++) {
            String currentInstruction = instruction.get(i);
            boolean isInstructionOne = instructionFormatOneOPCodeMap.containsKey(currentInstruction);
            //format one handling
            if (isInstructionOne) {
                objectCode.set(i, instructionFormatOneOPCodeMap.get(currentInstruction));
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "1");
                locationCounter.set(i + 1, currentLocationCounter);
            } else {
                //handle things that could include forward referencing
                if (currentInstruction.equals("J") || currentInstruction.equals("JEQ") || currentInstruction.equals("JGT") || currentInstruction.equals("JLT") || currentInstruction.equals("JSUB")) {
                    String ref = reference.get(i);
                    String address;
                    currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                    locationCounter.set(i + 1, currentLocationCounter);
                    if (references.containsKey(ref)) {
                        address = references.get(ref);
                    } else {
                        address = "0000";
                        //check j
                        references.put(label.get(i), locationCounter.get(i));
                        ForwardReference forwardReference = new ForwardReference(reference.get(i),subtractTwoHexadecimal(currentLocationCounter,"2"));
                    }
                    StringBuilder stringBuilder = new StringBuilder(instructionFormatThreeOPCodeMap.get(currentInstruction));
                    stringBuilder.append(address);
                    objectCode.set(i, stringBuilder.toString());
                    //comma x
                } else if (isImmediateInstruction(reference.get(i))) {
                    currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                    locationCounter.set(i + 1, currentLocationCounter);
                    String OPCode = getImmediateObjectCode(instructionFormatThreeOPCodeMap.get(currentInstruction), reference.get(i));
                    objectCode.set(i, OPCode);
                    //immediate
                } else if (isCommaXInstruction(reference.get(i))) {
                    currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                    locationCounter.set(i + 1, currentLocationCounter);
                    String OPCode = getCommaXObjectCode(instructionFormatThreeOPCodeMap.get(currentInstruction), reference.get(i));
                    objectCode.set(i, OPCode);
                    //normal instructions
                } else {
                    currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                    locationCounter.set(i + 1, currentLocationCounter);
                    String ref = reference.get(i);
                    String address = references.get(ref);
                    String OPCode = instructionFormatThreeOPCodeMap.get(currentInstruction);
                    StringBuilder stringBuilder = new StringBuilder(OPCode);
                    if (currentInstruction.equals("RSUB")) {
                        address = "0000";
                    }
                    stringBuilder.append(address);
                    objectCode.set(i, stringBuilder.toString());
                    references.put(reference.get(i), address);
                }
            }

        }

    }

    public void calculateLocationCounterAndObjectCodeForVariables() {
        currentLocationCounter = reference.get(0);
        int lastIndex = calcIndexOfLastVariable();
        instructionsIndexStart = 1;
        locationCounter.set(0, currentLocationCounter);
        locationCounter.set(1, currentLocationCounter);
        for (int i = 1; i <= lastIndex; i++) {
            if (instruction.get(i).equals("WORD")) {
                references.put(label.get(i), currentLocationCounter);
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, "3");
                locationCounter.set(i + 1, currentLocationCounter);
                objectCode.set(i, makeObjectCodeSixHexadecimal(decimalToHexadecimal(Integer.parseInt(reference.get(i)))));

            } else if (instruction.get(i).equals("BYTE")) {
                String byteValue = reference.get(i);
                byteValue = cutFirstTwoCharacters(byteValue);
                byteValue = removeLastCharacter(byteValue);
                int valueToAdd = byteValue.length();
                references.put(label.get(i), currentLocationCounter);
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, decimalToHexadecimal(valueToAdd));
                locationCounter.set(i + 1, currentLocationCounter);
                objectCode.set(i, getByteObjectCode(byteValue));


            } else if (instruction.get(i).equals("RESW")) {
                String value = reference.get(i);
                value = multiplyTwoHexadecimal(value, "3");
                references.put(label.get(i), currentLocationCounter);
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, value);
                locationCounter.set(i + 1, currentLocationCounter);


            } else if (instruction.get(i).equals("RESB")) {
                String value = reference.get(i);
                String valueToAdd = decimalToHexadecimal(Integer.parseInt(value));
                references.put(label.get(i), currentLocationCounter);
                currentLocationCounter = addTwoHexadecimal(currentLocationCounter, valueToAdd);
                locationCounter.set(i + 1, currentLocationCounter);

            }
            instructionsIndexStart++;
        }
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
                lineCount++;
            }
            numberOfLines = lineCount;

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

    public String hexadecimalToBinary(String input) {
        HashMap<Character, String> map = new HashMap<>();
        map.put('0', "0000");
        map.put('1', "0001");
        map.put('2', "0010");
        map.put('3', "0011");
        map.put('4', "0100");
        map.put('5', "0101");
        map.put('6', "0110");
        map.put('7', "0111");
        map.put('8', "1000");
        map.put('9', "1001");
        map.put('A', "1010");
        map.put('B', "1011");
        map.put('C', "1100");
        map.put('D', "1101");
        map.put('E', "1110");
        map.put('F', "1111");
        map.put('a', "1010");
        map.put('b', "1011");
        map.put('c', "1100");
        map.put('d', "1101");
        map.put('e', "1110");
        map.put('f', "1111");
        StringBuilder stringbuilder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            String value = map.get(current);
            stringbuilder.append(value);
        }
        return stringbuilder.toString();
    }

    public String binaryToHexadecimal(String input) {
        StringBuilder stringbuilder = new StringBuilder();
        if (input.length() % 4 == 3) {
            stringbuilder.append("0");
        } else if (input.length() % 4 == 2) {
            stringbuilder.append("00");
        } else if (input.length() % 4 == 1) {
            stringbuilder.append("000");
        }
        stringbuilder.append(input);
        String inputToProcess = stringbuilder.toString();
        StringBuilder output = new StringBuilder();
        HashMap<String, Character> map = new HashMap<>();
        map.put("0000", '0');
        map.put("0001", '1');
        map.put("0010", '2');
        map.put("0011", '3');
        map.put("0100", '4');
        map.put("0101", '5');
        map.put("0110", '6');
        map.put("0111", '7');
        map.put("1000", '8');
        map.put("1001", '9');
        map.put("1010", 'A');
        map.put("1011", 'B');
        map.put("1100", 'C');
        map.put("1101", 'D');
        map.put("1110", 'E');
        map.put("1111", 'F');
        for (int i = 0; i < inputToProcess.length(); i += 4) {
            String current = inputToProcess.substring(i, i + 4);//exclusive for the second parameter
            char part = map.get(current);
            output.append(part);
        }
        return output.toString();
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
    public String subtractTwoHexadecimal(String input1, String input2) {
        int decimal1 = hexadecimalToDecimal(input1);
        int decimal2 = hexadecimalToDecimal(input2);
        int sum = decimal1 - decimal2;
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

    public String makeObjectCodeFourHexadecimal(String inputHex) {
        if (inputHex == null || inputHex.isEmpty()) {
            throw new IllegalArgumentException("Input hexadecimal value cannot be null or empty");
        }
        int hexLength = inputHex.length();

        // Append zeros on the left to make it exactly 4 hex characters
        if (hexLength < 4) {
            int zerosToAppend = 4 - hexLength;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < zerosToAppend; i++) {
                sb.append("0");
            }
            sb.append(inputHex);
            return sb.toString().toUpperCase();
        } else {
            return inputHex.toUpperCase();
        }
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

    public String removeLastCharacter(String str) {
        if (str != null && str.length() > 0) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }

    public static String removeLastTwoCharacters(String str) {
        if (str == null || str.length() < 2) {
            throw new IllegalArgumentException("Input string must have at least two characters");
        }
        return str.substring(0, str.length() - 2);
    }

    public boolean isImmediateInstruction(String reference) {
        return reference.contains("#");
    }

    public String getImmediateInstructionOPCode(String opCode) {
        opCode = addTwoHexadecimal(opCode, "1");
        opCode = opCode.substring(2).toUpperCase();
        return opCode;
    }

    //send full immediate value including #
    public String getImmediateObjectCode(String opCode, String immediateValue) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getImmediateInstructionOPCode(opCode));
        //op code ready
        //append immediate value to it (decimal to hexadecimal then append)
        immediateValue = immediateValue.substring(1);
        int decimalValue = Integer.parseInt(immediateValue);
        immediateValue = decimalToHexadecimal(decimalValue);
        immediateValue = makeObjectCodeFourHexadecimal(immediateValue);
        stringBuilder.append(immediateValue);
        return stringBuilder.toString();
    }

    public boolean isCommaXInstruction(String reference) {
        return reference.contains(",X");
    }

    //send full reference including ",X"
    public String getCommaXObjectCode(String OPCode, String reference) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        reference = removeLastTwoCharacters(reference);
        String address = references.get(reference);
        String binary = hexadecimalToBinary(address);
        stringBuilder.append(1);
        stringBuilder.append(binary.substring(1));
        address = binaryToHexadecimal(stringBuilder.toString());
        stringBuilder1.append(OPCode);
        stringBuilder1.append(address);
        return stringBuilder1.toString();
    }

    //for jump instructions with forward referencing
    public String getObjectCodeAndAppendZeros(String OPCode) {
        StringBuilder stringBuilder = new StringBuilder(OPCode);
        stringBuilder.append("0000");
        return stringBuilder.toString();
    }

}
