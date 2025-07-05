package com.thanglv.sharedataapi.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.thanglv.sharedataapi.services.QrService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
class QrServiceImpl implements QrService {

    @Override
    public BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    @Override
    public byte[] generateQRCodeImageBytes(String text, int width, int height) throws Exception {
        BufferedImage image = generateQRCodeImage(text, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    @Override
    public String generateQRCodeImageBase64(String text, int width, int height) throws Exception {
        byte[] imageByte = generateQRCodeImageBytes(text, width, height);
        return Base64.getEncoder().encodeToString(imageByte);
    }
}
