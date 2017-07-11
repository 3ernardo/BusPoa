package application;

import smartcity.gtfs.Stop;
import smartcity.gtfs.Trip;

/**
 * Created by Bernardo on 11/07/2017.
 */
public class Station {

    private Stop stop;
    private Trip[] trips;

    public Station(Stop stop, Trip[] trips) {
        this.stop = stop;
        this.trips = trips;
    }

    public String getName() {
        return stop.getName();
    }

    public Stop getStop() {
        return stop;
    }

    public Trip[] getTrips() {
        return trips;
    }

    public void printBusRoutes() {
        System.out.println(
                "===============================" + "\n" +
                getName() + "\n" +
                "==============================="
        );
        for (Trip trip : trips) {
            System.out.println("=> " + trip.getRoute().getLongName());
        }
    }
}
