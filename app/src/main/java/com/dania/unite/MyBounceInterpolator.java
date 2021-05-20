package com.dania.unite;

import android.view.animation.Interpolator;

public class MyBounceInterpolator implements Interpolator {

    private double myAmplitude = 1;
    private double myFrequency = 10;

    public MyBounceInterpolator(double myAmplitude, double myFrequency) {
        this.myAmplitude = myAmplitude;
        this.myFrequency = myFrequency;
    }

    @Override
    public float getInterpolation(float time) {
        return (float)(-1*Math.pow(Math.E, -time/myAmplitude)*Math.cos(myFrequency*time)+1);
    }
}
