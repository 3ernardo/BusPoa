package application;


import smartcity.gtfs.Route;
import smartcity.gtfs.Stop;

/**
 * Created by Bernardo on 10/07/2017.
 */
public class StopingBus{

    private Stop stop;
    private Route route;

    public StopingBus(Stop stop, Route route) {
        this.stop = stop;
        this.route = route;
    }

    public Stop getStop() {
        return stop;
    }

    public Route getRoute() {
        return route;
    }

}
