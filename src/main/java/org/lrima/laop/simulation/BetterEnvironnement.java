package org.lrima.laop.simulation;

import javafx.scene.canvas.GraphicsContext;
import org.lrima.laop.physic.CarControls;
import org.lrima.laop.physic.SimpleCar;
import org.lrima.laop.simulation.buffer.SimulationBuffer;
import org.lrima.laop.simulation.buffer.SimulationSnapshot;
import org.lrima.laop.simulation.data.CarData;
import org.lrima.laop.simulation.map.MazeMap;
import org.lrima.laop.simulation.sensors.ProximityLineSensor;
import org.lrima.laop.utils.math.Vector2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class BetterEnvironnement implements MultiAgentEnvironnement {
    private MazeMap mazeMap;
    private SimulationBuffer buffer;
    private ArrayList<SimpleCar> simpleCars;
    private boolean finished;
    private int step = 0;

    @Override
    public ArrayList<Agent> step(ArrayList<CarControls> carControls) {
        if(this.isFinished())
            return this.reset(carControls.size());
        ArrayList<Agent> agents = new ArrayList<>();
        finished = true;
        for (int i = 0; i < simpleCars.size(); i++) {
            SimpleCar simpleCar = simpleCars.get(i);
            CarControls carControl = carControls.get(i);

            simpleCar.nextStep(carControl);
            mazeMap.collide(simpleCar);
            agents.add(new Agent(simpleCar.getSensors(), evalFitness(simpleCar)));
            if(!simpleCar.isDead()){
                finished = false;
            }
        }
        step++;
        if(step > 1000)
            finished = true;

        return agents;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public ArrayList<Agent> reset(int numberOfCars) {
        mazeMap = new MazeMap(10);
        mazeMap.bake();

        step = 0;
        buffer.clear();
        finished = false;
        ArrayList<Agent> agents = new ArrayList<>();
        simpleCars = generateCarObjects(numberOfCars);

        for (SimpleCar simpleCar : simpleCars) {
            mazeMap.collide(simpleCar);
            agents.add(new Agent(simpleCar.getSensors(), 0));
        }

        return agents;
    }

    public ArrayList<SimpleCar> generateCarObjects(int numberOfCars){
        int numberOfSensors = 5;

        ArrayList<SimpleCar> carObjects = new ArrayList<>();
        for(int i = 0 ; i < numberOfCars ; i++) {
            Point2D start = mazeMap.getStartPoint();
            SimpleCar car = new SimpleCar(new Vector2d(start.getX(), start.getY()), mazeMap.getOrientation());

            double orientationIncrement = Math.PI / numberOfSensors;
            //Create the sensors and assign them to the car
            for(int x = 0 ; x < numberOfSensors; x++) {
                ProximityLineSensor sensor = new ProximityLineSensor(car, (x * orientationIncrement) + orientationIncrement/2);
                car.addSensor(sensor);
            }

            carObjects.add(car);
        }

        return carObjects;
    }

    @Override
    public void render() {
        if(buffer != null) {
            SimulationSnapshot snapshot = new SimulationSnapshot();

            for(SimpleCar car : this.simpleCars) {
                snapshot.addCar(new CarData(car));
            }

            this.buffer.addSnapshot(snapshot);
        }
    }

    @Override
    public void init(LearningEngine learningEngine) {
        this.buffer = learningEngine.getBuffer();
    }

    @Override
    public void draw(GraphicsContext gc) {
        this.mazeMap.draw(gc);
    }


    private double evalFitness(SimpleCar car){
        return car.getDistanceTraveled() + Math.pow(mazeMap.distanceFromStart(car.getPosition().asPoint()), 2);
    }
}