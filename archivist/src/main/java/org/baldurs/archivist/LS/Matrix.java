package org.baldurs.archivist.LS;

/**
 * Matrix class for matrix operations
 */
public class Matrix {
    public int rows;
    public int cols;
    public float[] data;
    
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new float[rows * cols];
    }
    
    public static Matrix parse(String str) {
        // This is a simplified implementation - you'd need to implement the actual matrix parsing logic
        String[] values = str.split(" ");
        int size = (int) Math.sqrt(values.length);
        Matrix mat = new Matrix(size, size);
        
        for (int i = 0; i < values.length; i++) {
            mat.data[i] = Float.parseFloat(values[i]);
        }
        
        return mat;
    }
    
    /**
     * Get value at specified row and column
     */
    public float get(int row, int col) {
        return data[row * cols + col];
    }
    
    /**
     * Set value at specified row and column
     */
    public void set(int row, int col, float value) {
        data[row * cols + col] = value;
    }
} 