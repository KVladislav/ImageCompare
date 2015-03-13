package com.teamworks;

public class Main {

    public static void main(String[] args) {
//        args = new String[2];
//        args[0] = "image1.bmp";
//        args[1] = "image2.bmp";

        if (args.length < 2) {
            System.out.println("Arguments missing, please use image1 image2");
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
