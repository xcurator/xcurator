/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.toronto.cs.xcurator.utils;

import java.security.SecureRandom;
import java.math.BigInteger;

/**
 *
 * @author Amir
 */
public class StrUtils {

    private static final SecureRandom random = new SecureRandom();

    public static String nextRandString() {
        return new BigInteger(130, random).toString(32);
    }

    public static void main(String[] args) {
        for (int i = 1; i < 100; i++) {
            System.out.println(nextRandString());
        }
    }
}
