package application;

import smartcity.gtfs.Stop;
import smartcity.gtfs.Trip;

/**
 * Created by Bernardo on 11/07/2017.
 */
public class Bus {

    private Trip trip;
    private Stop[] stops;

    public Bus(Trip trip, Stop[] stops) {
        this.trip = trip;
        this.stops = stops;
    }

    public String getName() {
        return trip.getRoute().getLongName();
    }

    public Trip getRoute() {
        return trip;
    }

    public Stop[] getStops() {
        return stops;
    }

    public void printBusStops() {
        System.out.println(
                "===============================" + "\n" +
                getName() + "\n" +
                "==============================="
        );
        for (Stop stop : stops) {
            System.out.println("=> " + stop.getName());
        }
    }
}
