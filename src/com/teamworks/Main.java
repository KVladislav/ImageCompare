package com.teamworks;

public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Arguments missing, please use java -jar ImageCompare.jar image1.bmp image2.bmp");
            System.exit(1);
        }

        ImageComparator imageComparator = new ImageComparator();
        imageComparator.loadImages(args[0], args[1]);
        if (!imageComparator.isDataValid()) {
            System.exit(2);
        }
        imageComparator.compare();
        imageComparator.saveResult();
    }
}
