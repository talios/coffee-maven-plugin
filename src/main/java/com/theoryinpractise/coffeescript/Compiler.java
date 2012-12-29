/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.theoryinpractise.coffeescript;

import java.io.File;

/**
 *
 * @author thrykol
 */
public interface Compiler {
	public String compile(File source);

	public String[] commands();
}
