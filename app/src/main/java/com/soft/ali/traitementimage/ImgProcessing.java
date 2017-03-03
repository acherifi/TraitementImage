package com.soft.ali.traitementimage;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by ali on 27/01/2017.
 */

public class ImgProcessing {

    private static Img image;


    /**
     * This method changes the hue of an image.
     * The user can choose the hue he wants from a color picker and the hue is change
     * accordingly to the hue he choosed.
     * The image is converted to HSV first then the hue is changed.
     */
    public static void colorize(int chosencolor) {
        float hsv[] = new float[3];
        int pixels[] = image.getArraypixel();
        for (int i = 0; i < pixels.length; ++i) {

            Color.colorToHSV(pixels[i], hsv);
            hsv[Constants.HSV_HUE] = (float) chosencolor;
            pixels[i] = Color.HSVToColor(hsv);
        }
    }

    /**
     * This method extends the contrast of the image.
     * To avoid blending wrong colors by working on separated RGB channels, the algorithm works on
     * the V channel of the HSV colorspace.
     * First, an histogram and a cumulative histogram are generated.
     * Each pixel is converted into HSV. As the V component is between 0 and 1, the component is
     * rescaled to be between 0 and 255.
     * Then we get the value of the V component in the cumulative histogram. Then, this value is
     * divided by the number of pixels in order to rescale it between 0 and 1.
     */
    public static void histogramEqualization(){
        Histogram hist = new Histogram();
        int pixels[] = image.getArraypixel();

        int channel = Constants.HSV_VIBRANCE;
        hist.generateHSVHistogram(pixels, channel);
        int nbPixels = hist.getNbPixels();

        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++){
            Color.colorToHSV(pixels[i], hsv);
            int val = (int)(hsv[channel] * 255); //Rescaling the value
            hsv[channel] = ((float) hist.getCumulativeHistogramValueAt(val) / (float)nbPixels);
            pixels[i] = Color.HSVToColor(hsv);
        }
    }


    /**
     *extension dynamique via la lut
     */
    public static void extensionDynamique() {
        int channel = Constants.HSV_VIBRANCE;
        //La faire en HSV et ne pas toucher la teinte. Faire l'extension sur S puis repasser en RGB.
        int pixels[] = image.getArraypixel();
        float[] hsv = new float[3];
        LUT lut = new LUT();
        //initialisation de la LUT
        lut.generateHSV(image);
        //calcul de la transformation et application à l'image
        for (int i = 0; i <pixels.length; i++) {
            Color.colorToHSV(pixels[i],hsv);
            int value = (int)(hsv[channel] *255);
            float res = (lut.getValueAt(value)) / 255;
            hsv[channel] = res;
            pixels[i]= Color.HSVToColor(hsv);
        }
    }



    public static void toGray(){
        int red, green, blue;
        int rgb, total;
        int pixels[] = image.getArraypixel();

        for (int i = 0; i < pixels.length; i++) {
            rgb = pixels[i];
            red = ((Color.red(rgb)*3)/10);
            green = ((Color.green(rgb)*59)/100);
            blue = ((Color.blue(rgb)*11)/100);
            total = red + green + blue;
            rgb = Color.rgb(total, total, total);
            pixels[i]=rgb;
        }
    }

    public static void convolution(int n, int typeFilter) {
        /* Filtre de taille impaire toujours.
        Pose le filtre sur l'imagee, le filtre calcule la valeur du pixel central en mulitipliant la valeur des pixels par la valeur du masque 1 à1.
        premiere case pixel * premiere case masque etc..
         */

        Filter filter = new Filter(n);

        if(typeFilter == Constants.AVERAGE){
            filter.setAverage();
            calculConvolution(filter.getFilter(), filter.getsizefilter());
        }

        if(typeFilter == Constants.GAUSS){
            double sigma = 0.8;
            filter.setGauss(sigma);
            calculConvolution(filter.getFilter(), filter.getsizefilter());
        }

        if(typeFilter == Constants.SOBEL){
            filter.setSobelHorizontal();
            calculConvolution(filter.getFilter(), filter.getsizefilter());
            int[] pixelHorizontal= image.getArraypixel().clone();
            filter.setSobelVertical();
            calculConvolution(filter.getFilter(), filter.getsizefilter());
            int[] pixelVertical = image.getArraypixel().clone();

            int[]pixels = image.getArraypixel();
            int r,g,b;
            for (int i =0; i < pixelHorizontal.length; i++){
                r = (int) (Math.sqrt(Math.pow(Color.red(pixelHorizontal[i]),2) + Math.pow(Color.red(pixelVertical[i]), 2)));
                g = (int) (Math.sqrt(Math.pow(Color.green (pixelHorizontal[i]),2) + Math.pow(Color.green(pixelVertical[i]), 2)));
                b = (int) (Math.sqrt(Math.pow(Color.blue(pixelHorizontal[i]),2) + Math.pow(Color.blue(pixelVertical[i]), 2)));
                pixels[i] = Color.rgb(r,g,b);
            }

        }

        if(typeFilter == Constants.LAPLACE){
            filter.setLaplace();
            calculConvolution(filter.getFilter(), filter.getsizefilter());
        }

        if(typeFilter == Constants.LAPLACE2){
            filter.setLaplace2();
            calculConvolution(filter.getFilter(), filter.getsizefilter());
        }
    }

    private static void calculConvolution(float [][] filtermatrix, int sizefilter) {
        int pixels[] = image.getArraypixel();
        int originalpixels[] = pixels.clone();

        int width = image.getWidth();
        int height = image.getHeight();
        int index = width + 1;

        int r, g, b;

        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                r = (int) ((Color.red(originalpixels[index - 1 - width]) * filtermatrix[0][0])
                        + (Color.red(originalpixels[index - width]) * filtermatrix[0][1])
                        + (Color.red(originalpixels[index + 1 - width]) * filtermatrix[0][2])
                        + (Color.red(originalpixels[index - 1]) * filtermatrix[1][0])
                        + (Color.red(originalpixels[index]) * filtermatrix[1][1])
                        + (Color.red(originalpixels[index + 1]) * filtermatrix[1][2])
                        + (Color.red(originalpixels[index - 1 + width]) * filtermatrix[2][0])
                        + (Color.red(originalpixels[index + width]) * filtermatrix[2][1])
                        + (Color.red(originalpixels[index + 1 + width])) * filtermatrix[2][2]);
                g = (int) ((Color.green(originalpixels[index - 1 - width]) * filtermatrix[0][0])
                        + (Color.green(originalpixels[index - width]) * filtermatrix[0][1])
                        + (Color.green(originalpixels[index + 1 - width]) * filtermatrix[0][2])
                        + (Color.green(originalpixels[index - 1]) * filtermatrix[1][0])
                        + (Color.green(originalpixels[index]) * filtermatrix[1][1])
                        + (Color.green(originalpixels[index + 1]) * filtermatrix[1][2])
                        + (Color.green(originalpixels[index - 1 + width]) * filtermatrix[2][0])
                        + (Color.green(originalpixels[index + width]) * filtermatrix[2][1])
                        + (Color.green(originalpixels[index + 1 + width])) * filtermatrix[2][2]);
                b = (int) ((Color.blue(originalpixels[index - 1 - width]) * filtermatrix[0][0])
                        + (Color.blue(originalpixels[index - width]) * filtermatrix[0][1])
                        + (Color.blue(originalpixels[index + 1 - width]) * filtermatrix[0][2])
                        + (Color.blue(originalpixels[index - 1]) * filtermatrix[1][0])
                        + (Color.blue(originalpixels[index]) * filtermatrix[1][1])
                        + (Color.blue(originalpixels[index + 1]) * filtermatrix[1][2])
                        + (Color.blue(originalpixels[index - 1 + width]) * filtermatrix[2][0])
                        + (Color.blue(originalpixels[index + width]) * filtermatrix[2][1])
                        + (Color.blue(originalpixels[index + 1 + width])) * filtermatrix[2][2]);

                pixels[index] = Color.rgb(r, g, b);

                index++;
            }
            index += 2;
        }
    }

    public static void overexposure () {

        float hsv[] = new float[3];
        int pixels[] = image.getArraypixel();
        for(int i=0; i<pixels.length; i++){
            Color.colorToHSV(pixels[i], hsv);
            hsv[2] = (float) (hsv[2] + 0.20);
            pixels[i] = Color.HSVToColor(hsv);
        }
    }

    public static void isolate(int colorValue) {

        int limit = 150;

        int canalgrey;
        double valred = 0.3;
        double valgreen = 0.59;
        double valblue = 0.11;
        int distance;

        int pixels[] = image.getArraypixel();

        for (int i = 0; i < pixels.length; i++) {
            distance = (int) (Math.sqrt(Math.pow((Color.red(colorValue) - Color.red(pixels[i])), 2) + Math.pow((Color.green(colorValue) - Color.green(pixels[i])), 2) + Math.pow((Color.blue(colorValue) - Color.blue(pixels[i])), 2)));
            if (distance >= limit) {
                canalgrey = (int) ((Color.red(pixels[i]) * valred) + (Color.green(pixels[i]) * valgreen) + (Color.blue(pixels[i]) * valblue));
                pixels[i] = Color.rgb(canalgrey, canalgrey, canalgrey);
            }

        }
    }

    public static void sepia(){

        int pixels[] = image.getArraypixel();
        double valred;
        double valgreen;
        double valblue;
        int canalred;
        int canalgreen;
        int canalblue;

        for(int i=0; i<pixels.length; i++){

            valred = 0.393;
            valgreen = 0.769;
            valblue = 0.189;
            canalred = (int) Math.min(255, ((Color.red(pixels[i])*valred)+(Color.green(pixels[i])*valgreen)+(Color.blue(pixels[i])*valblue)));

            valred = 0.349;
            valgreen = 0.686;
            valblue = 0.168;
            canalgreen = (int) Math.min(255, ((Color.red(pixels[i])*valred)+(Color.green(pixels[i])*valgreen)+(Color.blue(pixels[i])*valblue)));

            valred = 0.272;
            valgreen = 0.534;
            valblue = 0.131;
            canalblue = (int) Math.min(255, ((Color.red(pixels[i])*valred)+(Color.green(pixels[i])*valgreen)+(Color.blue(pixels[i])*valblue)));

            pixels[i] = Color.rgb(canalred, canalgreen, canalblue);

        }

    }

    public static void fusion (Bitmap bitmapText) {

        int widthtext = bitmapText.getWidth();
        int heighttext = bitmapText.getHeight();
        int moyred, moygreen, moyblue;
        int pixels[] = image.getArraypixel();
        int pixelarraytext[] = new int[widthtext * heighttext];

        bitmapText.getPixels(pixelarraytext,0,widthtext,0,0,widthtext,heighttext);

        if (pixelarraytext.length < pixels.length) {
            for (int i = 0; i < pixelarraytext.length; i++) {
                if (pixelarraytext[i] == Color.BLACK) {
                    moyred = (Color.red(pixels[i]) + Color.red(pixelarraytext[i])) / 2;
                    moygreen = (Color.green(pixels[i]) + Color.green(pixelarraytext[i])) / 2;
                    moyblue = (Color.blue(pixels[i]) + Color.blue(pixelarraytext[i])) / 2;
                    pixels[i] = Color.rgb(moyred, moygreen, moyblue);
                }
            }
        }
    }

    public static void setImage(Img imagebase){
        image = imagebase;
    }

}




