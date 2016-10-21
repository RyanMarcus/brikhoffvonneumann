// { begin copyright } 
// Copyright Ryan Marcus 2016
// 
// This file is part of brikhoffvonneumann.
// 
// brikhoffvonneumann is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// brikhoffvonneumann is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with brikhoffvonneumann.  If not, see <http://www.gnu.org/licenses/>.
// 
// { end copyright } 
package info.rmarcus.brikhoffvonneumann;

public class CoeffAndMatrix {
	public final double coeff;
	public final double[][] matrix;
	
	public CoeffAndMatrix(double coeff, double[][] matrix) {
		this.coeff = coeff;
		this.matrix = matrix;
	}
}