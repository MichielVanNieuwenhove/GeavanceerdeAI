public class Main {
    static String inputFilename = "Instances/umps12.txt";
    static int q1;  //An umpire crew must wait q1-1 rounds before revisiting a team's home {0-nUmpires}
    static int q2;  //An umpire crew must wait q2-1 rounds before officiating the same team again {0-floor(nUmpires/2)}

    public static void main(String[] args) {
        InputManager inputManager = new InputManager();
        inputManager.readInput(inputFilename);
        InputManager.print();
    }
}