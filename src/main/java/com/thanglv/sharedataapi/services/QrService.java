package com.thanglv.sharedataapi.services;

import java.awt.image.BufferedImage;

public interface QrService {
    BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception;

    byte[] generateQRCodeImageBytes(String text, int width, int height) throws Exception;

    String generateQRCodeImageBase64(String text, int width, int height) throws Exception;
}
