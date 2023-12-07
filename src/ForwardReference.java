import java.util.ArrayList;

public class ForwardReference {
    String refName;
    ArrayList<String> addresses = new ArrayList<>(); // for addresses referring to a forward reference
    static ArrayList<ForwardReference> forwardReferences = new ArrayList<>();

    public ForwardReference(){

    }

    public ForwardReference(String refName , String address){
        if(containsRef(refName)){
            int index = getRefIndex(refName);
            ForwardReference current = forwardReferences.get(index);
            current.addresses.add(address);
        }else{
            ForwardReference current = new ForwardReference();
            current.refName = refName;
            current.addresses.add(address);
            forwardReferences.add(current);
        }
    }
    public boolean containsRef(String refName){
        for(int i = 0 ; i < forwardReferences.size() ; i++){
            ForwardReference current = forwardReferences.get(i);
            if(refName.equals(current.refName))
                return true;
        }
        return false;
    }
    public int getRefIndex(String refName){
        for(int i = 0 ; i < forwardReferences.size() ; i++){
            ForwardReference current = forwardReferences.get(i);
            if(current.refName.equals(refName)){
                return i;
            }
        }
        return -1;
    }
    public static void printList(){
        for(int i = 0 ; i < forwardReferences.size() ; i++){
            ForwardReference current = forwardReferences.get(i);
            System.out.print(current.refName + " ");
            System.out.print(current.addresses);
            System.out.println();
        }
    }
}
