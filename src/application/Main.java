package application;

import smartcity.gtfs.*;
import smartcity.util.GPSCoordinate;

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

    // Destination coordinates.
    static Double destinationLatit = -30.062872;
    static Double destinationLongi = -51.178117;

    // Maximum distance to the bus stop.
    static int threshold = 500;

    public static void findRoute() throws FileNotFoundException {
        GPSCoordinate originCoord = new GPSCoordinate(originLatit, originLongi);
        GPSCoordinate destinationCoord = new GPSCoordinate(destinationLatit, destinationLongi);
        Router router = new Router(originCoord, destinationCoord, threshold);
        router.testPossibleStops(originCoord);
        System.out.println("=============");
        router.possibleTrips(router.possibleStops(originCoord));
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
