package org.lrima.laop.physic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import org.lrima.laop.physic.staticobjects.FitnessWallObject;
import org.lrima.laop.physic.staticobjects.StaticLineObject;
import org.lrima.laop.simulation.LearningEngine;
import org.lrima.laop.simulation.map.LineCollidable;
import org.lrima.laop.simulation.sensors.Sensor;
import org.lrima.laop.simulation.sensors.SensorData;
import org.lrima.laop.utils.MathUtils;
import org.lrima.laop.utils.PhysicUtils;
import org.lrima.laop.utils.math.Vector2d;

/**
 * The car that tries to mimic a real car.
 *
 * @author Léonard
 */
public class SimpleCar implements LineCollidable {
    double wheelDirection;
    final double RANGE = 0.04;
    private ArrayList<Sensor> sensors;
    
    private final double CAR_WIDTH = 12;
    private final double CAR_HEIGHT = 37;
    private final double CAR_MASS = 2000;
    private boolean dead;
    
    private Vector2d position;
    private Vector2d velocity;
    private Vector2d acceleration;
    private double rotation;
    private double angularVelocity;
    private ArrayList<Vector2d> forces;

    private double distanceTraveled;

    private int fitnessWallCount;
    private int maxStep;

    private BiConsumer<Integer, SimpleCar> wallCountFunction;
    ArrayList<FitnessWallObject> currentTouching = new ArrayList<>();

    //OPTIMISATION
    private ArrayList<LineCollidable> collidableSensors;
    private float x1, x2, y1, y2;

    /**
     * Creates a new car with position <code>position</code>
     *
     * @param position the position of the car
     */
    private SimpleCar(Vector2d position) {
        this.position = position;
        this.forces = new ArrayList<>();
        this.sensors = new ArrayList<>();
        this.collidableSensors = new ArrayList<>();
        
        this.wheelDirection = 0;
        this.velocity = Vector2d.origin;
        this.acceleration = Vector2d.origin;
        this.rotation = 0;
        this.fitnessWallCount = 0;
        this.maxStep = 0;
    }

    /**
     * Creates a new car with position <code>position</code> and rotation <code>rotation</code>
     *
     * @param position
     * @param rotation
     */
    public SimpleCar(Vector2d position, double rotation){
        this(position);
        this.rotation = rotation;
    }


    /**
     * Makes a step in the simulation changing its position
     *
     * @param carControls the car control for that step
     */
    public void nextStep(CarControls carControls) {
        if(this.dead) return;
        
        this.forces = new ArrayList<>();


        double breakForce = Math.abs(Math.min(carControls.getAcceleration(), 0));
        double accelForce = Math.max(carControls.getAcceleration(), 0);

        this.forces.add(PhysicUtils.accelFromBackWeels(accelForce, rotation));
        this.forces.get(0).setTag("Accel from back");
        
        this.forces.add(PhysicUtils.breakForce(this.velocity, breakForce));
        this.forces.get(1).setTag("Break force");
        this.forces.add(PhysicUtils.airResistance(this.velocity));
        this.forces.get(2).setTag("Air resistance");
        this.forces.add(PhysicUtils.directionResistance(this.getDirection(), this.velocity));
        this.forces.get(3).setTag("Directionnal resistance");

        
        this.acceleration = PhysicUtils.accelFromForces(forces, this.CAR_MASS);

        this.wheelDirection = carControls.getRotation() * RANGE;

//        this.angularAccel = PhysicUtils.angularAccel(this.wheelDirection, this.velocity);
//        this.angularAccel = Math.min(Math.max(RANGE, angularAccel), -RANGE);
        this.angularVelocity = this.velocity.modulus()*this.wheelDirection* LearningEngine.DELTA_T;
        this.rotation += angularVelocity;

        this.velocity = this.velocity.add(acceleration.multiply(LearningEngine.DELTA_T));

        distanceTraveled += this.velocity.modulus();

        this.position = this.position.add(this.velocity.multiply(LearningEngine.DELTA_T));
    }
    
    /**
     * Get the direction of the physicable object
     * @return the direction
     */
    public Vector2d getDirection(){
        Vector2d v = new Vector2d(0, 1);

        return v.rotate(this.rotation, Vector2d.origin);
    }

    /**
     * Returns the array of sensors mounted on that car
     *
     * @return the sensors
     */
    public ArrayList<Sensor> getSensors() {
        return sensors;
    }

    /**
     * True if the car has hit a wall. False otherwise
     *
     * @return True if the car has hit a wall. False otherwise
     */
    public boolean isDead() {
        return dead;
    }


    /**
     * Get all the sensors that can collide with the map. For optimisation purposes.
     *
     * @return the array of all the sensors
     */
    public ArrayList<LineCollidable> getCollidableSensors() {
        return collidableSensors;
    }

    /**
     * Get an arrayList of sensor data. This is used while the simulation is running to see the captors
     *
     * @return the sensor data
     */
    public ArrayList<SensorData> getSensorsData() {
        ArrayList<SensorData> list = new ArrayList();

        for (Sensor sensor : sensors) {
            list.add(sensor.getData());
        }

        return list;
    }

    /**
     * Adds a sensor to the car. If this sensor is of instance LineCollidable (can collide with the map), puts it also in another list for optimisation purposes.
     * @param sensor the sensor to add
     */
    public void addSensor(Sensor sensor){
        this.sensors.add(sensor);
        if(sensor instanceof LineCollidable){
            this.collidableSensors.add((LineCollidable) sensor);
        }
    }


    /**
     *  Makes a collision between this and the line parsed in parameters
     *
     * @param line the line
     */
    public void collide(StaticLineObject line) {
        if(this.isDead()) return;

        if(MathUtils.rectSegmentIntersection(
                line.getX1(), line.getY1(),
                line.getX2(), line.getY2(),
                (float) getTopLeftPosition().getX(), (float) getTopLeftPosition().getY(),
                (float) getTopRightPosition().getX(), (float) getTopRightPosition().getY(),
                (float) getBottomRightPosition().getX(), (float) getBottomRightPosition().getY(),
                (float) getBottomLeftPosition().getX(), (float) getBottomLeftPosition().getY()))
            this.kill();
        
        getCollidableSensors().forEach(s -> s.collide(line));
    }

    /**
     * When the car collides with a fitness adder wall
     * @param line
     */
    @Override
    public void collideFitnessAdder(FitnessWallObject line) {
        //todo: Refactor this into a method to not duplicate code
        if(MathUtils.rectSegmentIntersection(
                line.getX1(), line.getY1(),
                line.getX2(), line.getY2(),
                (float) getTopLeftPosition().getX(), (float) getTopLeftPosition().getY(),
                (float) getTopRightPosition().getX(), (float) getTopRightPosition().getY(),
                (float) getBottomRightPosition().getX(), (float) getBottomRightPosition().getY(),
                (float) getBottomLeftPosition().getX(), (float) getBottomLeftPosition().getY())){



            if(!currentTouching.contains(line)){
                wallCountFunction.accept(line.addCollidableToCollided(this), this);
            }
            currentTouching.add(line);



            //todo: add fitness here
            //todo: and reset the car timer
        }
        else currentTouching.remove(line);

    }

    @Override
    public void bake() {
        getCollidableSensors().forEach(LineCollidable::bake);

        x1 = (float) (this.getCenter().getX() - this.CAR_HEIGHT);
        y1 = (float) (this.getCenter().getY() - this.CAR_HEIGHT);
        x2 = (float) (this.getCenter().getX() + this.CAR_HEIGHT);
        y2 = (float) (this.getCenter().getY() + this.CAR_HEIGHT);

        for (LineCollidable collidableSensor : collidableSensors) {
            x1 = Math.max(x1, collidableSensor.getX1());
            x1 = Math.max(x1, collidableSensor.getX2());
            x2 = Math.min(x2, collidableSensor.getX1());
            x2 = Math.min(x2, collidableSensor.getX2());
            y1 = Math.max(y1, collidableSensor.getY1());
            y1 = Math.max(y1, collidableSensor.getY2());
            y2 = Math.min(y2, collidableSensor.getY1());
            y2 = Math.min(y2, collidableSensor.getY2());
        }

    }
    
    /**
     * @return top-left corner's position in pixels
     */
    public Vector2d getTopLeftPosition(){
        double x = this.getPosition().getX();
        double y = this.getPosition().getY();
        return new Vector2d(x, y).rotate(this.getRotation(), this.getCenter());
    }

    /**
     * @return top-right corner's position in pixels
     */
    public Vector2d getTopRightPosition(){
        double x = this.getPosition().getX() + this.CAR_WIDTH;
        double y = this.getPosition().getY();
        return new Vector2d(x, y).rotate(this.getRotation(), this.getCenter());
    }

    /**
     * @return bottom-left corner's position in pixels
     */
    public Vector2d getBottomLeftPosition(){
        double x = this.getPosition().getX();
        double y = this.getPosition().getY() + this.CAR_HEIGHT;
        return (new Vector2d(x, y)).rotate(this.getRotation(), this.getCenter());
    }

    /**
     * @return top-right corner's position in pixels
     */
    public Vector2d getBottomRightPosition(){
        double x = this.getPosition().getX() + this.CAR_WIDTH;
        double y = this.getPosition().getY() + this.CAR_HEIGHT;
        return new Vector2d(x, y).rotate(this.getRotation(), this.getCenter());
    }
    
    /**
     * Gets the rotation of the car
     *
     * @return the rotation of the car
     */
    public double getRotation(){
        return this.rotation;
    }

    /**
     * Gets the position of the car
     *
     * @return the position of the car
     */
    public Vector2d getPosition() {
    	return this.position;
    }

    /**
     * Gets the center position of the car
     *
     * @return the center position of the car
     */
    public Vector2d getCenter(){
        return this.position.add(new Vector2d(this.CAR_WIDTH/2, this.CAR_HEIGHT/2));
    }

    @Override
    public float getX1() {
        return x1;
    }

    @Override
    public float getX2() {
        return x2;
    }

    @Override
    public float getY2() {
        return y2;
    }

    @Override
    public float getY1() {
        return y1;
    }
    
    public void kill(){
        dead = true;
    }

    /**
     * Get all the forces applied to the object
     * @return the forces applied to the object
     */
    public ArrayList<Vector2d> getForces(){
        return this.forces;
    }


    /**
     * @return The velocity of the object
     */
    public Vector2d getVelocity() {
        return velocity;
    }
    
    /**
     * @return The acceleration of the car
     */
    public Vector2d getAcceleration() {
    	return this.acceleration;
    }

    /**
     * @return The width of the car
     */
    public double getWidth() {
    	return this.CAR_WIDTH;
    }
    
    /**
     * @return The height of the car
     */
    public double getHeight() {
    	return this.CAR_HEIGHT;
    }

    /**
     * Keeps track of the distance traveled in pixels
     *
     * @return the distance traveled in pixels
     */
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    /**
     * Sets the orientation of the car
     * @param orientation the orientation
     */
    public void setOrientation(double orientation){
        this.rotation = orientation;
    }

    public int getFitnessWallCount() { return fitnessWallCount; }

    public int getMaxStep() { return maxStep; }


    public void setWallCountFunction(BiConsumer<Integer, SimpleCar> fitnessFunction) {
        this.wallCountFunction = fitnessFunction;
    }

    public void addFitness(int fitness) {
        this.fitnessWallCount += fitness;
    }

    public void addTime(int time){
        this.maxStep += time;
    }
}
