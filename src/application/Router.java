package application;

import smartcity.gtfs.*;
import smartcity.util.*;

import java.io.FileNotFoundException;
import java.util.*;

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

    private Trip[] tripsOrigin;
    private Trip[] tripsDestination;

    public Router(GPSCoordinate originCoord, GPSCoordinate destinationCoord, int threshold) throws FileNotFoundException {
        this.originCoord = originCoord;
        this.destinationCoord = destinationCoord;
        this.threshold = threshold;
        setup();
    }

    public void setup() throws FileNotFoundException {
        GTFSReader.loadStopTimes("src/source/stop_times.txt", trips, stops);
        tripsOrigin = possibleTrips(originCoord);
        tripsDestination = possibleTrips(destinationCoord);
    }

    public void findSimpeRoute() {
        Trip[] initialtripList = simpleRoute();
        if (viableSimpleRoute(initialtripList)) {
            System.out.println("This trip can be done with a single bus.");
        } else {
            System.out.println("This trip can not be done with a single bus.");
        }
    }



    ////////////////////////////////////////////////////////////

    /**
     * This method receives a GPS coordinate and check for bus routes
     * inside a radius defined via the "threshold" class attribute.
     *
     * @param   spot        The base GPS coordinate.
     * @return  tripList    An Array with viable trips.
     */
    public Trip[] possibleTrips(GPSCoordinate spot) {
        Set<Trip> viableTrips = new HashSet<>();
        Set<String> busRouteId = new HashSet<>();
        for(Map.Entry<String, Trip> tripEntry : trips.entrySet()) {
            Trip tripApplicant = tripEntry.getValue();
            if (tripApplicant.hasStopNear(spot, threshold) && !busRouteId.contains(tripApplicant.getRoute().getId())) {
                busRouteId.add(tripApplicant.getRoute().getId());
                viableTrips.add(tripApplicant);
            }
        }
        Trip[] tripList = viableTrips.toArray(new Trip[0]);
        return tripList;
    }

    /**
     * This method recieves a GPS coordinate and check for bus
     * stops in a radius equal to the threshold of the router.
     *
     * @param   spot        The base GPS coordinate.
     * @return  stopList    An Array with viable stops ordered by smaller distance.
     */
    public Stop[] possibleStops(GPSCoordinate spot) {
        Map<Double, Stop> viableBusStops = new TreeMap<>();

        int counter = 0;
        for (Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
            Stop applicant = stopEntry.getValue();
            Double distance = spot.distance(applicant.getGPSCoordinate());
            if (distance <= threshold) {
                viableBusStops.put(distance, applicant);
            }
        }
        Stop[] stopList = new Stop[viableBusStops.size()];
        for (Double key : viableBusStops.keySet()) {
            stopList[counter] = viableBusStops.get(key);
            counter++;
        }
        return stopList;
    }

    ////////////////////////////////////////////////////////////

    private Bus[] busList;

    public void mapBuses() {
        // Used route IDs
        Set<String> usedTrips = new HashSet<>();
        // Mapped bus holder
        Set<Bus> mappedBuses = new HashSet<>();
        // >>>> Just for testing <<<< For limiter
        //int i = 0;
        //int c = 0;
        // Sweeps all trips to create new buses
        for (Map.Entry<String, Trip> trip : trips.entrySet()) {
            // >>>> Just for testing <<<< For counter
            //i++;
            // Checks if the trip is already mapped into a bus
            if (!usedTrips.contains(trip.getValue().getRoute().getId())) {
                // Creates a bus
                Bus b = createBus(trip.getValue());
                // Add the new bus to the mapped bus list
                mappedBuses.add(b);
                // Add the new bus to the used trip list
                usedTrips.add(trip.getValue().getRoute().getId());
                // >>>> Just for testing <<<<
                //c++;
                //System.out.println("Buses saved: " + c + " Id: " + trip.getValue().getRoute().getId());
            }
            // >>>> Just for testing <<<< Breaks loop if the counter limit is reached
            //if (i>10) { break; }
            // >>>> Just for testing <<<< Prints the for iteration counter
            //System.out.println("For counts: " + i);
        }
        // Turns the mapped bus set into an array
        busList = mappedBuses.toArray(new Bus[0]);
        // >>>> Just for testing <<<< Prints the number of mapped buses
        System.out.println("Number of buses mapped: " + busList.length);
    }

    public Bus createBus(Trip trip) {
        Bus bus = new Bus(trip, checkTripForStops(trip));
        return bus;
    }

    /////////////---------

    private Station[] stationList;

    public void mapStations() {
        // Mapped stop holder
        Set<Station> mappedStops = new HashSet<>();
        // Sweeps all stops to create new stations
        for (Map.Entry<String, Stop> stop : stops.entrySet()) {

            mappedStops.add(createStation(stop.getValue()));
        }
        // Turns the mapped bus set into an array
        stationList = mappedStops.toArray(new Station[0]);
        // >>>> Just for testing <<<< Prints the number of mapped buses
        System.out.println("Number of bus stops mapped: " + stationList.length);
    }

    public Station createStation(Stop stopToCreate) {
        // Set<String> usedBuses = new HashSet<>();
        Set<Trip> mappedPassingBuses = new HashSet<>();

        for (Bus bus : busList) {
            Stop[] stops = bus.getStops();
            for(Stop stop : stops) {
                if (stopToCreate.equals(stop)) {
                    mappedPassingBuses.add(bus.getRoute());
                    // usedBuses.add(bus.getRoute().getId());
                }
            }
        }
        Trip[] passingBuses = mappedPassingBuses.toArray(new Trip[0]);
        Station station = new Station(stopToCreate, passingBuses);
        return station;
    }

    ////////////////////////////////////////////////////////////

    public Station stopToStation(Stop stop){
        for(Station station : stationList) {
            if (station.getStop().getId().equals(stop.getId())) {
                return station;
            }
        }
        System.out.println("Error. Station not found.");
        return null;
    }

    public Bus tripToBus(Trip trip){
        for(Bus bus : busList) {
            if (bus.getRoute().getRoute().getId().equals(trip.getRoute().getId())) {
                return bus;
            }
        }
        System.out.println("Error. Bus not found.");
        return null;
    }

    public void attempt() {
        boolean found = false;
        Set<Station> connectedStations = new HashSet<>();
        Set<Station> arrivalStations = new HashSet<>();
        Set<Bus> connectedBuses = new HashSet<>();

        Set<String> usedStations = new HashSet<>();
        Set<String> usedBuses = new HashSet<>();

        Stop[] departurePoints = possibleStops(originCoord);
        Stop[] arrivalPoints = possibleStops(destinationCoord);

        // Turn the departurePoints into Stations and add them to the connectedStations
        for(Stop stop : departurePoints) {
            connectedStations.add(stopToStation(stop));
            usedStations.add(stop.getId());
        }

        // Turn the trips connected to the connectedStations into Buses and add them to the connectedBuses
        for(Station station : connectedStations) {
            Trip[] trips = station.getTrips();
            for(Trip t : trips) {
                connectedBuses.add(tripToBus(t));
                usedBuses.add(t.getId());
            }
            System.out.println(station.getName());
        }

        System.out.println("----------");

        for(Bus bus : connectedBuses) {
            System.out.println(bus.getName()); //<<<<<<<<<<<<<<<
        }

        System.out.println("----------");

        for(Stop stop : arrivalPoints) {
            arrivalStations.add(stopToStation(stop));
            System.out.println(stop.getName());
        }

        System.out.println("----------");

        for(Station station : arrivalStations) {
            Trip[] trips = station.getTrips();
            for(Trip t : trips) {
                System.out.println(tripToBus(t).getName());
            }
        }

        System.out.println("----------");

        for(Station station : arrivalStations){
            Trip[] trips = station.getTrips();
            for(Trip t : trips) {
                if(connectedBuses.contains(tripToBus(t))) {
                    System.out.println(">>> This trip can be done with a single bus. <<<");
                    System.out.println(tripToBus(t).getName()); //<<<<<<<<<<<
                    found = true;
                }
            }
        }

        for (Bus bus : connectedBuses) {
            Stop[] stops = bus.getStops();
            for(Stop stop : stops) {
                if (!usedStations.contains(stop.getId())){
                    connectedStations.add(stopToStation(stop));
                    usedStations.add(stop.getId());
                }
            }
        }

        for (Station station : connectedStations) {
            Trip[] trips = station.getTrips();
            for(Trip trip : trips){
                if(!usedBuses.contains(trip.getId())) {
                    connectedBuses.add(tripToBus(trip));
                    usedBuses.add(trip.getId());
                }
            }
        }

        for(Station station : arrivalStations){
            Trip[] trips = station.getTrips();
            for(Trip t : trips) {
                if(connectedBuses.contains(tripToBus(t))) {
                    System.out.println(">>> This trip can be done with two buses. <<<");
                    System.out.println(tripToBus(t).getName()); //<<<<<<<<<<<
                    found = true;
                }
            }
        }

    }













    public void findRoute() {
        Trip[] tripOri = possibleTrips(originCoord);
        Set<String> accountedTrips = new HashSet<>();
        Set<Bus> mappedBuses = new HashSet<>();
        Set<Stop> reachedStops = new HashSet<>();
        Set<Trip> secondLevelTrips = new HashSet<>();
        for (Trip trip : tripOri) {
            accountedTrips.add(trip.getRoute().getId());
            Bus b = createBus(trip);
            mappedBuses.add(b);
            for(Stop s : b.getStops()) {
                reachedStops.add(s);
            }
        }
        for (Stop stop : reachedStops) {
            Trip[] tierTrip = possibleTrips(stop.getGPSCoordinate());
            for (Trip trip : tierTrip) {
                if (!accountedTrips.contains(trip.getRoute().getId())) {
                    accountedTrips.add(trip.getRoute().getId());
                    secondLevelTrips.add(trip);
                }
            }
        }
        for (Trip t : secondLevelTrips) {
            if (t.hasStopNear(destinationCoord, threshold)) {
                System.out.println(t.getRoute().getLongName());  //  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            }
        }
    }






    public Trip[] simpleRoute() {
        Trip[] tripOri = possibleTrips(originCoord);
        Trip[] tripDes =  possibleTrips(destinationCoord);
        Set<Trip> oneBusTrips = new HashSet<>();
        for (Trip origem : tripOri) {
            for (Trip destination : tripDes) {
                if (origem.getRoute().getId().equals(destination.getRoute().getId())) {
                    oneBusTrips.add(origem);
                }
            }
        }
        Trip[] tripList = oneBusTrips.toArray(new Trip[0]);
        return tripList;
    }

    public boolean viableSimpleRoute(Trip[] tripList) {
        if (tripList.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void connectOriginToDestination() {
        Map<Route, Route> connectedTrips = new HashMap<>();
        for (Trip to : tripsOrigin) {
            for (Trip td : tripsDestination) {
                for (Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
                    if (to.hasStopNear(stopEntry.getValue().getGPSCoordinate(), threshold) && td.hasStopNear(stopEntry.getValue().getGPSCoordinate(), threshold)) {
                        //System.out.println("Match found. " + to.getRoute().getLongName() + " and " + td.getRoute().getLongName());
                        connectedTrips.putIfAbsent(to.getRoute(), td.getRoute());
                    }
                }
            }
        }
        for (Map.Entry<Route, Route> entry : connectedTrips.entrySet())
        {
            System.out.println(entry.getKey().getLongName() + "  <--+-->  " + entry.getValue().getLongName());
        }
    }


    public Stop[] checkTripForStops(Trip trip) {
        Set<Stop> busStops = new HashSet<>();
        for (Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
            if (trip.hasStopNear(stopEntry.getValue().getGPSCoordinate(), 50)) {
                busStops.add(stopEntry.getValue());
            }
        }
        Stop[] stopList = busStops.toArray(new Stop[0]);
        return stopList;
    }



    public void turnTripsIntoStops() {
        Map<Trip, Stop> allRelatedStops = new HashMap<>();
        for (Trip to : tripsOrigin) {
            Stop[] stops = checkTripForStops(to);
            for (Stop st : stops) {
                allRelatedStops.put(to, st);
            }
        }
        for (Map.Entry<Trip, Stop> entry : allRelatedStops.entrySet())
        {
            System.out.println(entry.getKey().getRoute().getLongName() + "  +-->  " + entry.getValue().getName());
        }
    }

    public void TESTcheckTripForStops() {
        for (Trip to : tripsOrigin) {
            Stop[] stops = checkTripForStops(to);
            for (Stop stop : stops) {
                System.out.println(to.getRoute().getLongName() + "  +-->  " + stop.getName());
            }
        }
    }

    public void TESTcreateBus() {
        for (Trip to : tripsOrigin) {
            Bus b = createBus(to);
            b.printBusStops();
        }
    }



/*    public ArrayList<Stop> possiblesStops(GPSCoordinate fromCoordinate) {
        ArrayList<Stop> closebyStops = new ArrayList<Stop>();
        for(Map.Entry<String, Stop> entry : stops.entrySet()) {
            Stop applicant = entry.getValue();
            Double distance = fromCoordinate.distance(applicant.getGPSCoordinate());
            if (distance<threshold) {
                closebyStops.add(applicant);
            }
        }
        return closebyStops;
    }*/

    

    
    public void allPossibleTrips(ArrayList<Stop> possibleStops) {
        for(Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
            possibleTrips(stopEntry.getValue().getGPSCoordinate());
        }
    }

/*    public *//*ArrayList<Trip>*//* void possibleTrips(ArrayList<Stop> possibleStops) {
        //ArrayList<Trip> viableTrips = new ArrayList<Trip>();
        ArrayList<StopingBus> viableTrips = new ArrayList<StopingBus>();
        for(Map.Entry<String, Stop> stopEntry : stops.entrySet()) {
            Stop stopApplicant = stopEntry.getValue();
            for(Map.Entry<String, Trip> tripEntry : trips.entrySet()) {
                Trip tripApplicant = tripEntry.getValue();
                if (tripApplicant.hasStopNear(originCoord*//*stopApplicant.getGPSCoordinate()*//*, threshold)) {
                    StopingBus sb = new StopingBus(stopApplicant, tripApplicant.getRoute());
                    viableTrips.add(sb);
                }
            }
        }


        for(StopingBus stbs : viableTrips) {
            System.out.println("Bus stop: " + stbs.getStop().getName());
            System.out.println("Bus name: " + stbs.getRoute().getLongName());
            System.out.println("+++++++++++++++++++");
        }
    }*/



    ////////// TESTING //////////

    public void tripPrinter(Trip[] tripList) {
        for (Trip trip: tripList) {
            System.out.println(trip.getRoute().getLongName());
        }
    }

    public void testPossibleStops(GPSCoordinate toTest) {
        Stop[] closebyStops = possibleStops(toTest);
        for(Stop stop : closebyStops) {
            System.out.println(toTest.distance(stop.getGPSCoordinate()));
            System.out.println(stop);
            System.out.println("=======================");
        }
    }

    public void test() {
        Stop s = null;
        for (Map.Entry<String, Stop> entry : stops.entrySet()) {
            if(entry.getValue().getId().equalsIgnoreCase("687")) {
                s = entry.getValue();
            }
        }
        possibleTrips(s.getGPSCoordinate());
    }
}
