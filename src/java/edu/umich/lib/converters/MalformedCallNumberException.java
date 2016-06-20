/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umich.lib.normalizers;

/**
 *
 * @author dueberb
 */
public class MalformedCallNumberException extends Exception {

    /**
     * Creates a new instance of <code>MalformedCallNumberException</code> without detail message.
     */
    public MalformedCallNumberException() {
    }


    /**
     * Constructs an instance of <code>MalformedCallNumberException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MalformedCallNumberException(String msg) {
        super(msg);
    }
}
