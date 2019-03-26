package org.lrima.laop.physic.concreteObjects;

import org.lrima.laop.physic.staticobjects.StaticObject;
import org.lrima.laop.physic.staticobjects.StaticObjectType;
import org.lrima.laop.utils.math.Vector2d;
import org.lrima.laop.physic.objects.Box;

/**
 * Physic object representing the wheel of a car
 * @author Clement Bisaillon
 */
public class Wheel extends Box {
    private final double MAX_TRUST = 10;
    private final double MAX_ROTATION = Math.PI / 3;


    public enum WheelLocation {
        FRONT_LEFT, FRONT_RIGHT, BACK_LEFT, BACK_RIGHT;
    }

    private final static double WHEEL_WIDTH = 2;
    private final static double WHEEL_HEIGHT = 7;
    private final static double WHEEL_MASS = 200;
    public final static double ROLLING_RESISTANCE_COEF = 0.0001;

    private double thrust;
    private boolean canRotate;

    private WheelLocation location;
    private Car car;

    /**
     * Create a new wheel and attach it to the car at the specified location
     * @param car the car to attach the wheel to
     * @param location the location of the wheel on the car
     */
    public Wheel(Car car, WheelLocation location){
        super(WHEEL_MASS, WHEEL_WIDTH, WHEEL_HEIGHT);

        this.car = car;
        this.thrust = 0;
        this.location = location;
        this.canRotate = true;
    }

    /**
     * Get the thrust force of the wheel
     * @return the thrust force of the wheel
     */
    public Vector2d getThrustForce(){
        double x = -Math.sin(this.car.getFromWheelsRotation()) * this.thrust;
        double y = Math.cos(this.car.getFromWheelsRotation()) * this.thrust;

        return new Vector2d(x, y);
    }

    /**
     * Set the thrust force of this wheel
     * @param thrust the force
     */
    public void setThrust(double thrust) {
        this.thrust = thrust;
        if(this.thrust > MAX_TRUST){
            this.thrust = MAX_TRUST;
        }
    }

    /**
     * Get the resistance force of the wheel on the ground
     * @return the resistance force of the wheel
     */
    public Vector2d getVelocityResistance(){
        Vector2d resistance = this.car.getVelocity().multiply(this.getWeight() * this.ROLLING_RESISTANCE_COEF);
        
        //Flip the vector
        resistance = new Vector2d(resistance.getX() * -1 , resistance.getY() * -1);

        return resistance;
    }

    @Override
    protected void nextStep() {}

    public Vector2d getSumForces() {
        Vector2d sumOfForces = Vector2d.origin;

        //Add the thrust and the resistance to the forces
        sumOfForces = sumOfForces.add(this.getThrustForce());
        sumOfForces = sumOfForces.add(this.getVelocityResistance());

        return sumOfForces;
    }

    @Override
    public double getRotation() {
        double totalRotation = car.getRotation();

        //If the wheel is free to rotate, its rotation is not based on the car's rotation
        if(this.canRotate){
            totalRotation = super.getRotation();
        }

        return totalRotation;
    }


    @Override
    public Vector2d getPosition() {
        //Place the wheels correctly
        Vector2d position = Vector2d.origin;
        switch(location){
            case FRONT_LEFT:
                position = car.getBottomLeftPosition();
                break;
            case FRONT_RIGHT:
                position = car.getBottomRightPosition();
                break;
            case BACK_LEFT:
                position = car.getTopLeftPosition();
                break;
            case BACK_RIGHT:
                position = car.getTopRightPosition();
                break;
        }

        return position.add(new Vector2d(-WHEEL_WIDTH / 2, -WHEEL_HEIGHT / 2));
    }

    @Override
    public Vector2d getCenter() {
        return new Vector2d(this.getPosition().getX() + this.width / 2, this.getPosition().getY() + this.height / 2);
    }

    @Override
    public void collideWith(StaticObject object) {

    }


    public void setCanRotate(boolean canRotate) {
        this.canRotate = canRotate;
    }

    @Override
    public void rotate(double rotation) {
        super.rotate(rotation);

        //Maximum rotation of MAX_ROTATION in both directions
        if(Math.abs(this.car.getRotation() - this.getRotation()) > this.MAX_ROTATION){
            //Cancel the rotation
            this.rotation = this.rotation - rotation;
        }
    }

    /**
     * Add a thrust force to this wheel
     * @param thrust the force
     */
    public void addThrust(double thrust){
        this.thrust += thrust;
    }
}