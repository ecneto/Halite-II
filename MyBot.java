import hlt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Netbot");

        final ArrayList<Move> moveList = new ArrayList<>();

        for (;;) {
            moveList.clear();
            gameMap.updateMap(Networking.readLineIntoMetadata());

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                ArrayList<Planet> allPlanets = new ArrayList<Planet>(gameMap.getAllPlanets().values());
                ArrayList < Planet > sortedPlanets = getPlanetsSortedByDistance(ship, allPlanets);
                for (final Planet planet : allPlanets) {
                    if (planet.isOwned()) {
                        continue;
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED/2);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }

                    break;
                }
            }
            Networking.sendMoves(moveList);
        }
    }

    static ArrayList < Planet > getPlanetsSortedByDistance(Ship ship, ArrayList<Planet> planets) {
        Map < Double, Planet > planetsByDistance = getPlanetsByDistance(ship, planets);
        ArrayList < Planet > sortedPlanets = new ArrayList<Planet>();
        ArrayList < Double > sortedDistances = new ArrayList <Double> (planetsByDistance.keySet());
        for(Double d : sortedDistances) {
            sortedPlanets.add(planetsByDistance.get(d));
        }
        return sortedPlanets;
    }

    static Map < Double, Planet > getPlanetsByDistance(Ship ship, ArrayList<Planet> planets) {
        Map< Double, Planet > planetsByDistance = new HashMap< Double, Planet >();
        for(Planet planet : planets ) {
            double distance = planet.getDistanceTo(ship);
            planetsByDistance.put(distance, planet);
        }
        return planetsByDistance;
    }
}
