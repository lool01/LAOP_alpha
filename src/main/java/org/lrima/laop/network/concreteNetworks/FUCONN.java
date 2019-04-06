package org.lrima.laop.network.concreteNetworks;

import org.lrima.laop.core.LAOP;
import org.lrima.laop.network.genetics.GeneticNeuralNetwork;
import org.lrima.laop.network.nn.NeuralNetwork;
import org.lrima.laop.physic.CarControls;
import org.lrima.laop.settings.LockedSetting;
import org.lrima.laop.utils.MathUtils;
import org.lrima.laop.utils.math.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An implementation of a neural network. This on is of type GeneticNeuralNetwork. It can be trained by a Genetic Learning.
 *
 * @author Léonard
 */
public class FUCONN implements GeneticNeuralNetwork {
    NeuralNetwork neuralNetwork;

    @Override
    public GeneticNeuralNetwork crossOver(GeneticNeuralNetwork otherGeneticNeuralNetwork) {
        FUCONN other = (FUCONN) otherGeneticNeuralNetwork;

        double[] weightOther = other.neuralNetwork.getAllWeights();
        double[] weightThis = this.neuralNetwork.getAllWeights();
        double[] newWeights = new double[weightOther.length];

        for (int i = 0; i < newWeights.length; i++) {
            newWeights[i] = RandomUtils.getBoolean()? weightOther[i] : weightThis[i];
        }

        FUCONN newNetwork = new FUCONN();
        newNetwork.neuralNetwork = new NeuralNetwork(this.neuralNetwork.getTopology(), newWeights);

        return newNetwork;
    }

    @Override
    public void init(LockedSetting settings) {
        neuralNetwork = new NeuralNetwork((Integer) settings.get(LAOP.KEY_NUMBER_OF_SENSORS));
        neuralNetwork.addDenseLayer(2, MathUtils.LOGISTIC);
        neuralNetwork.addDenseLayer(3, MathUtils.LOGISTIC);

    }

    @Override
    public CarControls control(double... captorValues) {
        double[] data = new double[]{RandomUtils.getDouble(0, 1), 0, RandomUtils.getDouble(-1, 1)};
//        double[] data = neuralNetwork.predict(captorValues);

        return new CarControls(data);
    }

    public static void main(String[] args){
        NeuralNetwork neuralNetwork = new NeuralNetwork(5);
        neuralNetwork.addDenseLayer(2, MathUtils.LOGISTIC);
        neuralNetwork.addDenseLayer(3, MathUtils.LOGISTIC);

        System.out.println(neuralNetwork.getAllWeights().length);
    }
}
