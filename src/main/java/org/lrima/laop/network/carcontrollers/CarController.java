package org.lrima.laop.network.carcontrollers;

import org.lrima.laop.physic.CarControls;

/**
 * Interface used by the car to know what to do in each point in time. Also used by the NN to tell how the car should be controlled.
 *
 * @author Léonard
 */
public interface CarController {

    /**
     * Fonction called by the car at each second. Take the sensors values and outputs the reaction of the car.
     *
     * See {@link org.lrima.laop.physic.concreteObjects.SimpleCar#nextStep()} to see usage.
     *
     * @param captorValues the values of all the captors on the car
     * @return how the car should be controlled
     */
    CarControls control(double ... captorValues);
}
