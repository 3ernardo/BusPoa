package application;

import smartcity.gtfs.*;
import smartcity.util.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Bernardo on 09/07/2017.
 */
public class Router{

    private GPSCoordinate originCoord;
    private GPSCoordinate destinationCoord;
    private int threshold;

    private Map<String,Stop> stops = GTFSReader.loadStops("src/source/stops.txt");
    private Map<String,Route> routes = GTFSReader.loadRoutes("src/source/routes.txt");
    private Map<String,Service> calendar = GTFSReader.loadServices("src/source/calendar.txt");
    private Map<String,Shape> shapes = GTFSReader.loadShapes("src/source/shapes.txt");
    private Map<String,Trip> trips = GTFSReader.loadTrips("src/source/trips.txt", routes, calendar, shapes);

    public Router(GPSCoordinate originCoord, GPSCoordinate destinationCoord, int threshold) throws FileNotFoundException {
        this.originCoord = originCoord;
        this.destinationCoord = destinationCoord;
        this.threshold = threshold;
        setup();
    }

    public void setup() throws FileNotFoundException {
        GTFSReader.loadStopTimes("src/source/stop_times.txt", trips, stops);
    }

    public /*ArrayList<Trip>*/ void possibleTrips(ArrayList<Stop> possibleStops) {
        ArrayList<Trip> viableTrips = new ArrayList<Trip>();
        for(Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
            Stop stopApplicant = stopEntry.getValue();
            for(Map.Entry<String, Trip> tripEntry : trips.entrySet()) {
                Trip tripApplicant = tripEntry.getValue();
                if (tripApplicant.hasStopNear(originCoord/*stopApplicant.getGPSCoordinate()*/, threshold)) {
                System.out.println("Testing: " + tripApplicant.getRoute().getLongName());
                }
            }
        }
    }

    /**
     * This method recieves a GPS coordinate and check for bus
     * stops in a radius equal to the threshold of the router.
     */
    public ArrayList<Stop> possibleStops(GPSCoordinate fromCoordinate) {
        ArrayList<Stop> closebyStops = new ArrayList<Stop>();
        for(Map.Entry<String, Stop> entry : stops.entrySet()) {
            Stop applicant = entry.getValue();
            Double distance = fromCoordinate.distance(applicant.getGPSCoordinate());
            if (distance<threshold) {
                closebyStops.add(applicant);
            }
        }
        return closebyStops;
    }

    public void testPossibleStops(GPSCoordinate toTest) {
        ArrayList<Stop> closebyStops = possibleStops(toTest);
        for(Stop stop : closebyStops) {
            System.out.println(stop);
        }
    }


}
