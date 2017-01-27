/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.eval;

/**
 *
 * @author amir
 */
public class Accuracy {

    final private double pr;
    final private double re;

    public Accuracy(double precision, double recall) {
        pr = precision;
        re = recall;
    }

    public double precision() {
        return pr;
    }

    public double recall() {
        return re;
    }

    public double fscore(double beta) {
        return (1 + beta * beta) * ((pr * re) / ((beta * beta * pr) + re));
    }
}
