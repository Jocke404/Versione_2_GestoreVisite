package src;

import src.controller.MasterController;

public class MainVisite {
    public static void main(String[] args) {
        MasterController masterController = new MasterController().createApp();

        try {
            masterController.startApp();
        } finally {
            masterController.stopExecutorService();
        }
        
    }
}
