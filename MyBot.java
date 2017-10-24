import hlt.*;

import java.util.*;

public class MyBot {
    final static int INITIAL_SHIPS_PER_BASE = 2;

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Netbot");

        final ArrayList<Move> moveList = new ArrayList<>();


        for (;;) {
            moveList.clear();
            gameMap.updateMap(Networking.readLineIntoMetadata());

            int myTotalShips = gameMap.getMyPlayer().getShips().values().size();
            int myShipsPerBase = INITIAL_SHIPS_PER_BASE;
            if(myTotalShips > 20) {
                // Once we reach 20 ships, start populating each of your planets with more ships.
                myShipsPerBase = myTotalShips/5;
            }
            Map<Integer, Planet> allPlanets = gameMap.getAllPlanets();
            int numAllPlanets = allPlanets.values().size();
            int numOwnedPlanets = 0;
            for(Planet planet : allPlanets.values()) {
                if(planet.isOwned()) numOwnedPlanets++;
            }
            Boolean isOpenPlanets = numOwnedPlanets < numAllPlanets;

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                Map<Double,Entity> entitiesByDistance = gameMap.nearbyEntitiesByDistance(ship);
                ArrayList<Planet> sortedPlanets = getNearestPlanets(ship, entitiesByDistance);
                for (final Planet planet : sortedPlanets) {
                    if (planet.isOwned()) {

                        if(planet.getOwner() == gameMap.getMyPlayerId()) {
                            // If owned by me, continue populating it til full.
                            if(planet.getDockedShips().size() >= myShipsPerBase || planet.isFull()) {
                                // Ignore if there are enough ships.
                                continue;
                            } else {

                            }
                        } else {
                            // If owned by someone else, ignore it, unless that's all that's left!
                            if(isOpenPlanets) {
                                continue;
                            } else {
                                // Attack! Find a docked ship and attack it.
                                List<Integer> dockedShipIds = planet.getDockedShips();
                                Ship target = getNearestTargetShip(gameMap, ship, planet.getOwner(), dockedShipIds);
                                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, target, Constants.MAX_SPEED);
                                if (newThrustMove != null) {
                                    moveList.add(newThrustMove);
                                    break;
                                }
                            }
                        }
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        break;
                    }

                }
            }
            Networking.sendMoves(moveList);
        }
    }

    static ArrayList<Planet> getNearestPlanets(Ship ship, Map<Double,Entity> entitiesByDistance) {
        ArrayList<Planet> planets = new ArrayList<Planet>();
        ArrayList < Double > sortedDistances = new ArrayList <Double> (entitiesByDistance.keySet());
        for(Double d : sortedDistances) {
            Entity e = entitiesByDistance.get(d);
            if(e instanceof Planet) {
                planets.add( (Planet) e );
            }
        }
        return planets;
    }

    static Ship getNearestTargetShip(GameMap gameMap, Ship myShip, int enemyId, List<Integer> shipIds) {
        Integer targetShipId = null;
        double minDistance = 1000;
        for(Integer shipId : shipIds) {
            Ship tempTargetShip = gameMap.getShip(enemyId, shipId);
            double d = myShip.getDistanceTo(tempTargetShip);
            if(d < minDistance) {
                minDistance = d;
                targetShipId = shipId;
            }
        }
        Ship targetShip = gameMap.getShip(enemyId, targetShipId);
        return targetShip;
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
