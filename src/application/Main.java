package application;

import smartcity.gtfs.*;
import smartcity.util.GPSCoordinate;
import util.Console;
import util.Timer;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by Bernardo on 09/07/2017.
 */
public class Main  {

    public static void main(String[] args) throws FileNotFoundException {
        findRoute();
    }

    // Origin coordinates.
    static Double originLatit = -30.135593;
    static Double originLongi = -51.223171;

    // Destination coordinates. Giuseppe
    static Double destinationLatit = -30.062872;
    static Double destinationLongi = -51.178117;


    // Destination coordinates. Praia de Belas
//    static Double destinationLatit = -30.048933;
//    static Double destinationLongi = -51.227854;

    // Destination coordinates. Senac
//    static Double destinationLatit = -30.035180;
//    static Double destinationLongi = -51.226698;

    // Maximum distance to the bus stop.
    static int threshold = 250;

    private static Console c = new Console();
    private static Timer t = new Timer();

    public static void findRoute() throws FileNotFoundException {
        t.timerOn(); // Start load timer.
        GPSCoordinate originCoord = new GPSCoordinate(originLatit, originLongi);
        GPSCoordinate destinationCoord = new GPSCoordinate(destinationLatit, destinationLongi);
        Router router = new Router(originCoord, destinationCoord, threshold);
        System.out.println("Load complete. (" + t.timerOff() + " sec)"); // Stop load timer.

        t.timerOn(); // Start mapping timer.
        router.mapBuses();
        System.out.println("Buses mapping complete. (" + t.timerOff() + " sec)"); // Stop mapping timer.

        t.timerOn(); // Start mapping timer.
        router.mapStations();
        System.out.println("Bus stops mapping complete. (" + t.timerOff() + " sec)"); // Stop mapping timer.


//        router.testPossibleStops(originCoord);
//        System.out.println("=============<>=============");
//        router.possibleStops(originCoord);
//        System.out.println("=============<>=============");
//        router.tripPrinter(router.possibleTrips(originCoord));
//        router.testPossibleStops(destinationCoord);
//        router.allPossibleTrips(router.possibleStops(originCoord));
//        router.simpleRoute();
        router.findSimpeRoute();
        System.out.println(t.timerOff());
        System.out.println("=============<>=============");
        router.attempt();
        System.out.println("=============<>=============");
    }

/*    public static void tester() throws FileNotFoundException {

        ////////////////////
        Map<String,Stop> stops = GTFSReader.loadStops("src/source/stops.txt");
        Map<String,Route> routes = GTFSReader.loadRoutes("src/source/routes.txt");
        Map<String,Service> calendar = GTFSReader.loadServices("src/source/calendar.txt");
        Map<String,Shape> shapes = GTFSReader.loadShapes("src/source/shapes.txt");
        Map<String,Trip> trips = GTFSReader.loadTrips("src/source/trips.txt", routes, calendar, shapes);
        GTFSReader.loadStopTimes("src/source/stop_times.txt", trips, stops);
        ////////////////////

        System.out.println("Start...");
        GPSCoordinate originCoord = new GPSCoordinate(-30.067852, -51.161605);
        for (Map.Entry<String, Trip> t : trips.entrySet()) {
            Trip trip = t.getValue();
            if (trip.hasStopNear(originCoord, 800.0)) {
                System.out.println("worked");
            }
        }
        System.out.println("Finished");
    }*/

}
