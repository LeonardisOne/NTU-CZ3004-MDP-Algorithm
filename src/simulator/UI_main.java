package simulator;

import simulator.UIlayout_v2;;

public class UI_main{
    public static void main(String[] args) {
        new UI_main();
    }

    public UI_main(){
        UIlayout_v2 ui = UIlayout_v2.getInstance();
        ui.start();
    }
}