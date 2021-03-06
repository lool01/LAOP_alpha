package org.lrima.laop.settings.option;

import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Classe qui permet d'utiliser un Integer comme option
 *
 * @author Léonard
 */
public class OptionInt implements Option<Integer>{
    private Integer value = 0, max = Integer.MAX_VALUE, min = Integer.MIN_VALUE, step = 1;

    /**
     * Creates a new OptionInt
     *
     * @param defaultValue The default value of the OptionInt
     */
    public OptionInt(Integer defaultValue){
        this.value = defaultValue;
    }

    /**
     * Creates a new OptionInt
     *
     * @param defaultValue The default value of the OptionInt
     * @param max Constraint the value to this max
     * @param min Constraint the value to this min
     */
    public OptionInt(Integer defaultValue, Integer max, Integer min){
        this(defaultValue);
        this.max = max;
        this.min = min;
    }

    /**
     *
     * @param defaultValue The default value of the OptionInt
     * @param max Constraint the value to this max
     * @param min Constraint the value to this max
     * @param step The step that the JSpinner created with generateComponent must have
     */
    public OptionInt(Integer defaultValue, Integer max, Integer min, Integer step){
        this(defaultValue, max, min);
        this.step = step;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public boolean setValue(Integer value) {
        // Check if the value is inside the bounds. If it is, it returns false without changing the value
        if(max != null && value > max) return false;
        if(min != null && value < min) return false;

        this.value = value;

        return true;
    }

    @Override
    public Node generateComponent() {
        Spinner<Integer> spinner = new Spinner<>(min, max, value, step);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((obs, oldVal, newVal)->{
            value = newVal;
        });
        
        //DO NOT REMOVE or else the value is not updated when unfocusing the spinner
        spinner.focusedProperty().addListener((obs, oldValue, newValue) -> {
        	if (!newValue) {
        	    spinner.increment(0);
        	}
        });
        //END DO NOT REMOVE

        spinner.setMaxWidth(Integer.MAX_VALUE);

        return spinner;
    }
}
