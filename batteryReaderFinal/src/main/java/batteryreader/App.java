package batteryreader;

import batteryreaderpage.BatteryReaderPageCtrl;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {

        BatteryReaderPageCtrl batteryReaderPageCtrl=new BatteryReaderPageCtrl();
        batteryReaderPageCtrl.reader();

    }

}
