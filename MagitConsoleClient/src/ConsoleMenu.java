import Models.User;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleMenu {
    private final int QUIT_OPERATION_CODE = 13;
    private MagitManager magitManager = new MagitManager();

    public void displayMenuAndReturnUserInput() {
        int userInput = 0;

        while (userInput != this.QUIT_OPERATION_CODE) {

            Scanner input = new Scanner(System.in);

            System.out.println("Please Choose Your Operation -");
            System.out.println("-------------------------\n");
            System.out.println("1 - Create An Empty Repository");
            System.out.println("2 - Create New Commit");
            System.out.println("3 - Create New Branch");
            System.out.println("4 - Checkout To Branch");
            System.out.println("5 - Change User");
            System.out.println("6 - Load Repository");
            System.out.println("7 - Load XML");
            System.out.println("8 - Show Current Commit");
            System.out.println("9 - Show Status");
            System.out.println("10 - Show Available Branches");
            System.out.println("11 - Delete Branch");
            System.out.println("12 - Show Active Branch History");
            System.out.println("13 - Show Status");
            System.out.println("14 - Quit");


            System.out.print("\nUser Input - ");
            userInput = input.nextInt();
            try {
                switch (userInput) {

                    //Create Empty Repository
                    case 1:
                        String path = ConsoleMenu.readPath();
                        magitManager.createEmptyRepository(path);

                        break;
                    //Create New Commit
                    case 2:
                        if (magitManager.currentRepo != null) {
                            String commitMsg = ConsoleMenu.displayMsgAndReturnInput("Please Enter Commit Msg");

                            magitManager.commit(commitMsg);
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;
                    //Create New Branch
                    case 3:
                        if (magitManager.currentRepo != null) {

                            String branchName = displayMsgAndReturnInput("Please Enter Branch Name");
                            if (magitManager.branchExists(branchName)) {
                                displayMsg("Branch Already Exists! Please Try Again");
                                break;
                            }

                            String checkout = ConsoleMenu.displayMsgAndReturnInput("Would You Wish To Checkout?\nPress Y / N");
                            magitManager.createBranch(branchName, (checkout.equals("Y") || checkout.equals("y")));
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;
                    //Checkout To Branch
                    case 4:
                        boolean forceCheckout = false;
                        if (magitManager.currentRepo != null) {
                            String branchName = displayMsgAndReturnInput("Enter Branch Name");
                            ArrayList<String> availableBranches = (ArrayList<String>) magitManager.getAvailableBranches().stream()
                                    .map(name -> name.replace(" (HEAD)", ""))
                                    .collect(Collectors.toList());

                            if (availableBranches.contains(branchName)) {

                                try {
                                    magitManager.checkoutBranch(branchName, forceCheckout);
                                } catch (RuntimeException e) {
                                    ConsoleMenu.displayMsg("Changes Detected!\n");
                                    forceCheckout = ConsoleMenu.displayMsgAndReturnInput("Please Enter 'revert' or  'cancel'").equals(
                                            "revert");

                                    if (forceCheckout) {
                                        magitManager.checkoutBranch(branchName, forceCheckout);
                                    }
                                }
                            } else {
                                System.out.println("\nOperation Not Available, Cannot Checkout To This Branch\n");
                            }
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;
                    //Change User
                    case 5:
                        this.magitManager.setCurrentUser(displayMsgAndReturnInput("Please Enter Your name"));

                        break;
                    //Load Repository
                    case 6:
                        try {
                            String newRepoPath = readPath();
                            magitManager.loadRepository(newRepoPath);
                        } catch (FileNotFoundException e) {
                            ConsoleMenu.displayMsg(e.getMessage());
                        }


                        break;
                    //Load XML
                    case 7:
                        magitManager.loadXml();

                        break;
                    //Show Current Commit
                    case 8:
                        magitManager.showCommit();
                        break;
                    //Show Status
                    case 9:
                        if (magitManager.currentRepo != null) {
                            System.out.println("\nRepository Set To Folder - " + new File(magitManager.currentRepo.get_path()).getName());
                            System.out.println("Active User Set To - " + User.getName());
                            Map<String, List<String>> statusMap = magitManager.showStatus();
                            for (String status : statusMap.keySet()) {
                                if (statusMap.get(status).size() != 0) {
                                    System.out.println(status);
                                    for (String changedFile : statusMap.get(status)) {
                                        System.out.println(changedFile);
                                    }
                                } else {
                                    System.out.println(status + " empty");
                                }
                            }
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;
                    //Show Available Branches
                    case 10:
                        if (magitManager.currentRepo != null) {

                            displayMsg("Available Branches -");
                            ArrayList<String> availableBranches = magitManager.getAvailableBranches();
                            availableBranches.forEach(name -> displayMsg("- " + name));
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;
                    //Delete Branch
                    case 11:
                        if (magitManager.currentRepo != null) {

                            String name = displayMsgAndReturnInput("Please Enter Branch Name");

                            if (magitManager.getCurrentBranch().getName().equals(name)) {
                                displayMsg("You Can't Remove The HEAD Branch!\nPlease Checkout To Another Branch First");
                            } else if (!magitManager.getAvailableBranches().contains(name)) {
                                displayMsg("You Can't Delete Branch That Not Exist");
                            } else {

                                magitManager.deleteBranch(name);
                            }
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }
                        break;
                    //Show active branch history
                    case 12:
                        if (magitManager.currentRepo != null) {
                            magitManager.showBranchCommitHistory();
                        } else {
                            System.out.println("\nOperation Not Available, Please Create Repo Or Load One\n");
                        }

                        break;

                    case QUIT_OPERATION_CODE:
                        System.out.println("See You Next Time :)");
                        break;

                    default:
                        System.out.println("Invalid Operation, Please Try Again\n");
                        break;
                }

            } catch (IOException | JAXBException e) {
                e.printStackTrace();
            }
        }

    }

    public static String readPath() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        boolean isOk = false;
        File f;
        do {
            System.out.println("Please enter a Path:");
            f = new File(bufferedReader.readLine());
            if (f.exists() && f.isDirectory()) {
                isOk = true;
            } else {
                System.err.println("Doesn't exist or is not a folder.");
            }
        } while (!isOk);
        return f.getAbsolutePath();
    }

    public static String readXml() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        boolean isOk = false;
        File f;
        do {
            System.out.println("Please enter a Path:");
            f = new File(bufferedReader.readLine());
            if (f.exists() && f.isFile() && f.getName().endsWith(".xml")) {
                isOk = true;
            } else {
                System.err.println("Not exist or not an xml file");
            }
        } while (!isOk);
        return f.getAbsolutePath();
    }

    public static String displayMsgAndReturnInput(String msg) {
        String userInput;
        Scanner input = new Scanner(System.in);

        System.out.println(msg);
        userInput = input.nextLine();
        return userInput;
    }

    public static void displayMsg(String msg) {
        System.out.println(msg);
    }

    public static String lxQustion() {
        String userInput;
        Scanner input = new Scanner(System.in);
        System.out.println("How should we proceed? (l/x)");
        userInput = input.nextLine();
        while (!userInput.equals("l") && !userInput.equals("x")) {
            System.out.println("Wrong input only 'l' or 'x'");
            userInput = input.nextLine();
        }
        return userInput;
    }
}