package com.company;
import edu.princeton.cs.algs4.Picture;
import java.awt.Color;
import edu.princeton.cs.algs4.IndexMinPQ;



{
    private int width;
    private int height;
    private double[][] energy;
    private int[][] picture;

    public SeamCarver(Picture picture)
    {
        if (picture == null)
            throw new IllegalArgumentException();
        this.width = picture.width();
        this.height = picture.height();
        this.picture = new int[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j< height; j++)
                this.picture[i][j] = picture.getRGB(i, j);

        energy = new double[width][height];
        calcEnergy();
    }
    private void calcEnergy()
    {
        energy = new double[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                energy[i][j] = calcEnergyElement(i, j);
    }
    private double calcEnergyElement(int x, int y)
    {
        if (x == 0 || x == width - 1)
            return 1000;
        if (y == 0 || y == height - 1)
            return 1000;
        int left = picture[x-1][y];
        int right = picture[x+1][y];
        int up = picture[x][y+1];
        int down = picture[x][y-1];

        int rxr = (right >> 16) & 0xFF;
        int rxl = (left >> 16) & 0xFF;
        int rx = rxr - rxl;
        int gxr = (right >> 8) & 0xFF;
        int gxl = (left >> 8) & 0xFF;
        int gx = gxr - gxl;
        int bxr = (right >> 0) & 0xFF;
        int bxl = (left >> 0) & 0xFF;
        int bx = bxr - bxl;
        double dx = rx*rx + gx*gx + bx*bx;

        int ryr = (up >> 16) & 0xFF;
        int ryl = (down >> 16) & 0xFF;
        int ry = ryr - ryl;
        int gyr = (up >> 8) & 0xFF;
        int gyl = (down >> 8) & 0xFF;
        int gy = gyr - gyl;
        int byr = (up >> 0) & 0xFF;
        int byl = (down >> 0) & 0xFF;
        int by = byr - byl;
        double dy = ry*ry + gy*gy + by*by;

        return Math.sqrt(dx + dy);
    }

    public Picture picture()
    {
        Picture pic = new Picture(width, height);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                pic.setRGB(i, j, picture[i][j]);
        return pic;
    }
    public int width()
    {
        return width;
    }
    public int height()
    {
        return height;
    }
    public double energy(int x, int y)
    {
        if (x < 0 || x >= width)
            throw new IllegalArgumentException();
        if (y < 0 || y >= height)
            throw new IllegalArgumentException();
        return energy[x][y];
    }

    private class verSeam
    {
        private double[][] energy;
        private int width;
        private int height;
        private double minTotEnergy;
        private int minEneElement;
        //EdgeTo[x][y] = x-1, x or x+1 of y-1 from where it came
        private int[] edgeTo;
        //DistTo = min current distance from top row
        private double[] distTo;
        private IndexMinPQ<Double> pq;

        public verSeam(double[][] energy, int width, int height)
        {
            this.energy = energy;
            this.width = width;
            this.height = height;
            minTotEnergy = Double.POSITIVE_INFINITY;
            int maxi = height*width;
            calc();
            for (int v = maxi - width; v < maxi; v++)
                if (distTo[v] < minTotEnergy)
                {
                    minTotEnergy = distTo[v];
                    minEneElement = v;
                }
        }
        private void calc()
        {
            pq = new IndexMinPQ<Double>(width*height);
            distTo = new double[width * height];
            edgeTo = new int[width * height];
            for (int i = 0; i < distTo.length; i++)
                distTo[i] = Double.POSITIVE_INFINITY;
            for (int i = 0; i < width; i++)
            {
                edgeTo[i] = -1;
                distTo[i] = 0;
                pq.insert(i, 0.0);
            }
            while (!pq.isEmpty())
            {
                int v = pq.delMin();
                int x = v % width;
                int y = v / width;
                if (y == height - 1)
                    continue;
                if (width == 1)
                    relax(x, y + 1, 0);
                else if (x == 0)
                    for (int i = 0; i <= 1; i++)
                        relax(x + i, y + 1, i);
                else if (x == width - 1)
                    for (int i = -1; i <= 0; i++)
                        relax(x + i, y + 1, i);
                else
                    for (int i = -1; i <= 1; i++)
                        relax(x + i, y + 1, i);
            }
        }
        private void relax(int x, int y, int i)
        {
            int v = x + y*width;
            if (distTo[v] > distTo[v - i - width] + energy[x][y])
            {
                distTo[v] = distTo[v - i - width] + energy[x][y];
                edgeTo[v] = v - i - width;
                if (pq.contains(v))
                        pq.decreaseKey(v, distTo[v]);
                else
                    pq.insert(v, distTo[v]);
            }
        }
        public int[] retSeam()
        {
            int[] val = new int[height];
            int next = minEneElement;
            for (int i = height-1; i >= 0; i--)
            {
                val[i] = next % width;
                next = edgeTo[next];
            }
            return val;
        }
    }


    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam()
    {
        double[][] transEnergy = new double[height][width];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                transEnergy[j][i] = energy[i][j];
        verSeam ver = new verSeam(transEnergy, height, width);
        return ver.retSeam();
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam()
    {
        verSeam ver = new verSeam(energy, width, height);
        return ver.retSeam();
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam)
    {
        if (height <= 1)
            throw new IllegalArgumentException();
        if (seam == null)
            throw new IllegalArgumentException();
        if (seam.length != width)
            throw new IllegalArgumentException();
        int o = seam[0];
        for (int i = 0; i < width; i++)
        {
            if (seam[i] < 0 || seam[i] > height - 1 || Math.abs(seam[i] - o) > 1)
                throw new IllegalArgumentException();
            o = seam[i];
        }
        for (int x = 0; x < width; x++)
            for (int y = seam[x]; y < height-1; y++)
                picture[x][y] = picture[x][y+1];
        height -= 1;

        calcEnergy();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam)
    {
        if (width <= 1)
            throw new IllegalArgumentException();
        if (seam == null)
            throw new IllegalArgumentException();
        if (seam.length != height)
            throw new IllegalArgumentException();
        int o = seam[0];
        for (int i = 0; i < height; i++)
        {
            if (seam[i] < 0 || seam[i] > width - 1 || Math.abs(seam[i] - o) > 1)
                throw new IllegalArgumentException();
            o = seam[i];
        }
        for (int y = 0; y < height; y++)
            for (int x = seam[y]; x < width - 1; x++)
                picture[x][y] = picture[x + 1][y];
        width -= 1;

        calcEnergy();
    }

    public static void main(String[] args)
    {

    }
}
